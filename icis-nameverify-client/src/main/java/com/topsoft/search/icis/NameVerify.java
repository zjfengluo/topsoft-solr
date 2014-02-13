package com.topsoft.search.icis;


import com.google.common.base.Strings;
import com.google.common.util.concurrent.MoreExecutors;
import com.topsoft.search.SolrCoreQueryRequest;
import com.topsoft.search.SolrMultiCoreQuery;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.support.Ids;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newHashMap;

/**
 * 企业名称查重的简易查询类，提供以下查询方法:
 * <p/>
 * <ul>
 * <li>输入企业(字号)名称字符串，返回名称库/关键字库/商标名称库中与之相关的记录id列表。</li>
 * <li>输入企业(字号)名称字符串，返回上述三库中所有与之相关的记录id列表。</li>
 * </ul>
 * <p/>
 * <p>
 * <pre>     {@code
 *     // 创建名称查重实例，参数为solr服务器url
 *     NameVerify nameVerify = new NameVerify("http://192.168.3.17:9081/solr");
 *
 *     // 查询名称库，找出所有与"拓普网络工程有限公司"相关的,行政区划代码410302，并且行业代码是52的所有记录
 *     List<Long> ids = findAllIdFromNamestore("拓普网络", "410302", "52");
 *
 *     // 查询关键字库，找出所有与"拓普网络工程有限公司"相关的记录id列表
 *     List<Long> ids = nameVerify.findAllIdFrom(Core.keywords, "拓普网络工程有限公司");
 *
 *     // 同时查询名称库、关键字库、商标名称库，找出所有与"拓普网络工程有限公司"相关的记录id列表
 *     Map<String, List<Long>> ids = nameVerify.findAllId("拓普网络工程有限公司");
 *   }</pre>
 *
 * <p> 多线程并行查询
 * <pre>     {@code
 *     // 多线程并行查询名称库、关键字库、商标名称库，可有效提高查询性能
 *     ExecutorService executor = Executors.newFixedThreadPool(3);
 *     Map<String, List<Long>> ids = nameVerify.findAllId("拓普网络工程有限公司", executor);
 *     ...
 *     // NameVerify 不负责线程池的资源管理，需客户端代码自行管理
 *     executor.shutdown();
 *     executor.awaitTermination(10, TimeUnit.SECONDS);
 *   }</pre>
 *
 * <p>分页查询
 * <pre>     {@code
 *     // 支持分页方式的查询
 *     Page<Long> page = nameVerify.findIdFrom...
 *
 *     // 当前页数
 *     int pageNum = page.getNumber();
 *
 *     // 当前页的记录数
 *     int recordNum = page.getNumberOfElements();
 *
 *     // 总页数
 *     int totalPages = page.getTotalPages();
 *
 *     // 当前页的大小(每页的规定记录数大小)
 *     int pageSize = page.getSize();
 *
 *     // 是否有下一页
 *     boolean hasNextPage = page.hasNextPage();
 *
 *     // 构建一个分页查询请求, 查询第3页，每页20条记录
 *     PageRequest pageRequest = PageRequest.builder(3, 20).build();
 *     Page<Long> page = nameVerify.findIdFrom(Core.namestore, "拓普网络工程有限公司", pageRequest);
 *
 *     // Page实现了Iterable<T>, 无须取出记录列表，支持直接foreach迭代
 *     for(Long id: page) {
 *       // do some thing
 *     }
 *
 *     // 也可以如下方式直接构建SQL，进行数据库查询
 *     if (page.getTotalElements() > 0) {
 *       String sql = String.format("select * from namestore where id in (%s)",
 *       Joiner.on(',').join(page).toString());
 *
 *       NamestoreDao dao = ....
 *       List<Namestore> namestores = dao.findBySql(sql);
 *     } else {
 *       // 没有找到任何符合条件的记录
 *     }
 *   }</pre>
 * </p>
 *
 * @author wangyg
 */

public class NameVerify {
  private final SolrMultiCoreQuery query;

  public NameVerify(String baseURL) {
    this.query = new SolrMultiCoreQuery(baseURL);
  }

  public List<Long> findAllIdFromNamestore(String enterpriseName, String nameDistCode, String industryPhy) {
    String queryString = buildNamestoreQueryString(enterpriseName, nameDistCode, industryPhy);
    return query.findAllFrom(Core.namestore.getCoreName(), queryString, Ids.GET);
  }

  public Page<Long> findIdFromNamestore(String enterpriseName, String nameDistCode, String industryPhy, Pageable pageRequest) {
    String queryString = buildNamestoreQueryString(enterpriseName, nameDistCode, industryPhy);
    return query.findFrom(Core.namestore.getCoreName(), queryString, pageRequest, Ids.GET);
  }

  public Map<String, List<Long>> findAllId(final String enterpriseName) {
    return findAllId(enterpriseName, MoreExecutors.sameThreadExecutor());
  }

  public Map<String, List<Long>> findAllId(final String enterpriseName, ExecutorService executor) {
    final HashMap<String, List<Long>> result = newHashMap();
    Core[] cores = Core.values();
    int length = cores.length;
    if (length > 0) {
      List<Callable<List<Long>>> querys = newArrayListWithCapacity(length);

      for (final Core core : cores) {

        querys.add(new Callable<List<Long>>() {
          @Override
          public List<Long> call() throws Exception {
            return findAllIdFrom(core, enterpriseName);
          }
        });

      }

      try {
        List<Future<List<Long>>> futures = executor.invokeAll(querys);

        // futures里的元素循序与querys一致，即一一对应
        for (int i = 0; i < futures.size(); i++) {
          Future<List<Long>> future = futures.get(i);
          result.put(cores[i].getCoreName(), future.get());
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }

    return result;
  }

  public List<Long> findAllIdFrom(Core core, String enterpriseName) {
    return query.findAllFrom(core.getCoreName(), core.buildQueryString(enterpriseName), Ids.GET);
  }

  public Page<Long> findIdFrom(Core core, String enterpriseName, Pageable pageRequest) {
    return query.findFrom(core.getCoreName(), core.buildQueryString(enterpriseName), pageRequest, Ids.GET);
  }

  private String buildNamestoreQueryString(String enterpriseName, String nameDistCode, String industryPhy) {
    checkArgument(!Strings.isNullOrEmpty(enterpriseName), "enterpriseName can not be null or empty!");
    StringBuilder builder = new StringBuilder(Core.namestore.buildQueryString(enterpriseName));

    if (!Strings.isNullOrEmpty(nameDistCode)) {
      builder.append(" AND ").append("nameDistCode:").append(nameDistCode);
    }

    if (!Strings.isNullOrEmpty(industryPhy)) {
      builder.append(" AND ").append("industryPhy:").append(industryPhy);
    }
    return builder.toString();
  }


  /**
   * 数据索引源
   */
  public enum Core implements SolrCoreQueryRequest {
    /**
     * 企业名称库
     */
    namestore {
      @Override
      protected String enterpriseNameQueryTemplate() {
        return "entTra:%s AND savePerTo:[NOW TO *]";
      }
    },

    /**
     * 关键字库
     */
    keywords {
      @Override
      protected String enterpriseNameQueryTemplate() {
        return "banLetter:%s AND banTo:[NOW TO *]";
      }
    },

    /**
     * 商标名称库
     */
    trademark {
      @Override
      protected String enterpriseNameQueryTemplate() {
        return "tmName:%s";
      }
    };

    @Override
    public String getCoreName() {
      return name();
    }

    @Override
    public String buildQueryString(Map<String, String> parameters) {
      StringBuilder builder = new StringBuilder();

      Set<Map.Entry<String, String>> entries = parameters.entrySet();
      for (Map.Entry<String, String> entry : entries) {
        if (builder.length() > 0) builder.append(" AND ");
        builder.append(entry.getKey()).append(":").append(entry.getValue());
      }

      return builder.toString();
    }

    public String buildQueryString(String enterpriseName) {
      return String.format(enterpriseNameQueryTemplate(), enterpriseName);
    }

    abstract protected String enterpriseNameQueryTemplate();
  }

}