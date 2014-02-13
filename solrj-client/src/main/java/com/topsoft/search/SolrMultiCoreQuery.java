package com.topsoft.search;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.domain.Pages;
import com.topsoft.search.domain.Sort;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CoreAdminParams;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;

/**
 * solr的简易查询类
 * <p>
 * <p>主要用于slor的multicore查询，支持单core查询，也支持多core并行查询。
 * <p>查询参数为符合solr查询语法的字符串，支持分页及排序。
 * <p>
 * <p>创建查询实例
 * <pre>{@code
 * // 查询 http://192.168.3.17:9081/solr 上部署的multicore
 * SolrMultiCoreQuery query = new SolrMultiCoreQuery("http://192.168.3.17:9081/solr");
 *
 * List<SolrDocument> docs = query.findAll("entTra:拓普网络");
 * }</pre>
 * <p>
 * <p>使用 {@link com.google.common.base.Function} 将 {@link org.apache.solr.common.SolrDocument} 对象转换为业务对象
 * <pre>{@code
 * Function<SolrDocument, User> userTransform = new Function<SolrDocument, User>() {
 *   public User apply(SolrDocument doc) {
 *     User user = new User();
 *     user.setId((Long) doc.getFieldValue("id"));
 *     user.setName(doc.getFieldValue("enterpriseName"));
 *     ...
 *     return user;
 *   }
 * };
 * List<User> users = query.findAll("entTra:拓普网络", userTransform);
 *
 * // 针对core名称为"namestore"进行分页查询，并将结果转换为user对象
 * Page<User> page = query.findFrom("namestore", "entTra:拓普网络", Pageable.DEFAULT, userTransform);
 *
 * // 针对字段"id" 升序、"name"降序排序
 * Pageable pageRequest = PageRequest.builder(1, 30).orderBy(Order.by("id", Order.asc), Order.by("name", Order.desc)).build();
 * Page<User> page = query.findFrom("namestore", "entTra:拓普网络", pageRequest, userTransform);
 *
 * // 并行查询
 * ExecutorService executor = ...
 * Page<User> page = query.findFrom("namestore", "entTra:拓普网络", pageRequest, userTransform, executor);
 *
 * // 客户端有责任管理ExecutorServer对象资源的创建及回收
 * executor.shutdown();
 * executor.awaitTermination(10, TimeUnit.SECONDS);
 *
 * }</pre>
 *
 * @author wangyg
 */
public class SolrMultiCoreQuery {
  private static final Function<SolrDocument, SolrDocument> DO_NOTHING = new Function<SolrDocument, SolrDocument>() {

    @Nullable
    @Override
    public SolrDocument apply(@Nullable SolrDocument input) {
      return input;
    }
  };
  private final String baseUrl;

  public SolrMultiCoreQuery(String baseUrl) {
    this.baseUrl = normalizeBaseURL(baseUrl);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link Page}封装对象
   */
  public Map<String, Page<SolrDocument>> find(String queryString, Pageable pageRequest) {
    return find(queryString, pageRequest, MoreExecutors.sameThreadExecutor());
  }

  /**
   * 并行查询solr服务器上所有core
   * <p>使用客户端提供的 {@link java.util.concurrent.ExecutorService} 对象，用相同的查询条件并行查询所有core
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link Page}封装对象
   */
  public Map<String, Page<SolrDocument>> find(String queryString, Pageable pageRequest, ExecutorService executor) {
    return find(queryString, pageRequest, DO_NOTHING, executor);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link Page}封装对象
   */
  public <T> Map<String, Page<T>> find(final String queryString, final Pageable pageRequest, final Function<SolrDocument, T> function) {
    return find(queryString, pageRequest, function, MoreExecutors.sameThreadExecutor());
  }

  /**
   * 查询solr服务器上所有core
   * <p>使用客户端提供的 {@link java.util.concurrent.ExecutorService} 对象，用相同的查询条件并行查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link Page}封装对象
   */
  public <T> Map<String, Page<T>> find(final String queryString, final Pageable pageRequest, final Function<SolrDocument, T> function, ExecutorService executor) {
    final HashMap<String, Page<T>> result = Maps.newHashMap();

    List<String> cores = getAllCoreNames();
    int size = cores.size();
    if (size > 0) {
      List<Callable<Page<T>>> querys = newArrayListWithCapacity(size);

      for (final String core : cores) {

        querys.add(new Callable<Page<T>>() {
          @Override
          public Page<T> call() throws Exception {
            return findFrom(core, queryString, pageRequest, function);
          }
        });

      }

      try {
        List<Future<Page<T>>> futures = executor.invokeAll(querys);

        // futures里的元素顺序与querys一致，即一一对应
        for (int i = 0; i < futures.size(); i++) {
          Future<Page<T>> future = futures.get(i);
          result.put(cores.get(i), future.get());
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }

    return result;
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core
   *
   * @param queryString 符合solr查询语法的字符串
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public Map<String, List<SolrDocument>> findAll(String queryString) {
    return findAll(queryString, MoreExecutors.sameThreadExecutor());
  }

  /**
   * 并行查询solr服务器上所有core
   * <p>使用客户端提供的 {@link java.util.concurrent.ExecutorService} 对象，用相同的查询条件并行查询所有core
   *
   * @param queryString 符合solr查询语法的字符串
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public Map<String, List<SolrDocument>> findAll(String queryString, ExecutorService executor) {
    return findAll(queryString, DO_NOTHING, executor);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public <T> Map<String, List<T>> findAll(final String queryString, final Function<SolrDocument, T> function) {
    return findAll(queryString, function, MoreExecutors.sameThreadExecutor());
  }

  /**
   * 查询solr服务器上所有core
   * <p>使用客户端提供的 {@link java.util.concurrent.ExecutorService} 对象，用相同的查询条件并行查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public <T> Map<String, List<T>> findAll(final String queryString, final Function<SolrDocument, T> function, ExecutorService executor) {
    final HashMap<String, List<T>> result = Maps.newHashMap();

    List<String> cores = getAllCoreNames();
    int size = cores.size();
    if (size > 0) {
      List<Callable<List<T>>> querys = newArrayListWithCapacity(size);

      for (final String core : cores) {

        querys.add(new Callable<List<T>>() {
          @Override
          public List<T> call() throws Exception {
            return findAllFrom(core, queryString, function);
          }
        });

      }

      try {
        List<Future<List<T>>> queryResults = executor.invokeAll(querys);

        // queryResults里的元素顺序与query一致，即一一对应。
        for (int i = 0; i < queryResults.size(); i++) {
          Future<List<T>> future = queryResults.get(i);
          result.put(cores.get(i), future.get());
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }

    return result;
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List}封装对象
   */
  public List<SolrDocument> findAllFrom(String coreName, String queryString) {
    return findAllFrom(coreName, queryString, DO_NOTHING);
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public <T> List<T> findAllFrom(String coreName, String queryString, Function<SolrDocument, T> function) {
    Preconditions.checkNotNull(function, "function must not be null!");

    SolrQuery query = new SolrQuery(queryString);
    query.setStart(0);
    query.setRows(Integer.MAX_VALUE);

    HttpSolrServer core = getCore(coreName);
    try {
      SolrDocumentList solrDocuments = core.query(query).getResults();

      return Lists.transform(solrDocuments, function);

    } catch (SolrServerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link Page}封装对象
   */
  public Page<SolrDocument> findFrom(String coreName, String queryString, Pageable pageRequest) {
    return findFrom(coreName, queryString, pageRequest, DO_NOTHING);
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link Page}封装对象
   */
  public <T> Page<T> findFrom(String coreName, String queryString, Pageable pageRequest, Function<SolrDocument, T> function) {
    Preconditions.checkNotNull(function, "function must not be null!");
    Preconditions.checkNotNull(pageRequest, "pageRequest must not be null!");

    SolrQuery query = new SolrQuery(queryString);
    query.setStart(pageRequest.getOffset());
    query.setRows(pageRequest.getPageSize());

    Sort sort = pageRequest.getSort();

    if (sort != null) {
      for (Sort.OrderBy orderBy : sort) {
        query.addSort(orderBy.getProperty(), orderBy.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
      }
    }

    HttpSolrServer core = getCore(coreName);
    try {
      SolrDocumentList docs = core.query(query).getResults();
      List<T> results = Lists.transform(docs, function);

      return Pages.of(results, pageRequest, docs.getNumFound());

    } catch (SolrServerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 查询该solr服务器下所有的core
   *
   * @return 所有的core名称列表
   */
  public List<String> getAllCoreNames() {
    CoreAdminRequest request = new CoreAdminRequest();
    request.setAction(CoreAdminParams.CoreAdminAction.STATUS);

    HttpSolrServer server = getCache().getUnchecked(baseUrl);
    try {
      CoreAdminResponse response = request.process(server);

      List<String> result = newArrayList();
      for (int i = 0; i < response.getCoreStatus().size(); i++) {
        result.add(response.getCoreStatus().getName(i));
      }
      return result;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  protected final HttpSolrServer getCore(String coreName) {
    return getCache().getUnchecked(baseUrl + "/" + coreName);
  }

  private HttpSolrServerCache getCache() {
    return HttpSolrServerCache.getInstance();
  }

  private String normalizeBaseURL(String baseURL) {
    checkArgument(!Strings.isNullOrEmpty(baseURL), "baseURL must not be null or empty!");

    if (baseURL.endsWith("/")) {
      baseURL = baseURL.substring(0, baseURL.length() - 1);
    }

    return baseURL.trim().toLowerCase();
  }

}
