package com.topsoft.search;

import com.google.common.base.Function;
import com.google.common.util.concurrent.MoreExecutors;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.support.GuavaFunctionResultTransformer;
import com.topsoft.search.support.PagedSolrQueryProfile;
import org.apache.solr.common.SolrDocument;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 扩展了{@link com.topsoft.search.SolrMultiCoreQuery}, 增加了分页支持
 * <p>查询参数为符合solr查询语法的字符串，支持分页及排序。
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
 * @author wangyg
 */
public class PagableSolrMultiCoreQuery extends SolrMultiCoreQuery {
  public PagableSolrMultiCoreQuery(String baseUrl) {
    super(baseUrl);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core，并使用{@link com.topsoft.search.support.PagedSolrQueryProfile<T>} 对象
   * 对{@link org.apache.solr.client.solrj.SolrQuery}参数进行预先设置和查询结果数据进行后期转换及封装、分页
   *
   * @param queryString 符合solr查询语法的字符串
   * @param profile     对查询参数进行预先设置和对查询结果数据进行后期转换及封装
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @param <T>
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page} 对象
   */
  public <T> Map<String, Page<T>> find(String queryString, PagedSolrQueryProfile<T> profile, ExecutorService executor) {
    return template.find(queryString, profile, executor);
  }

  /**
   * <p>查询特定core，并使用{@link com.topsoft.search.support.PagedSolrQueryProfile<T>} 对象
   * 对{@link org.apache.solr.client.solrj.SolrQuery}参数进行预先设置和查询结果数据进行后期转换及封装、分页
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @param profile     对查询参数进行预先设置和对查询结果数据进行后期转换及封装
   * @param <T>
   * @return 符合查询条件的{@link com.topsoft.search.domain.Page}分页对象，内部结果集合类型为 <T> 的对象
   */
  public <T> Page<T> findFrom(String coreName, String queryString, PagedSolrQueryProfile<T> profile) {
    return template.findFrom(coreName, queryString, profile);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page}封装对象
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
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page}封装对象
   */
  public Map<String, Page<SolrDocument>> find(String queryString, Pageable pageRequest, ExecutorService executor) {
    return find(queryString, new PagedSolrQueryProfile<SolrDocument>(ResultTransformer.DEFAULT, pageRequest), executor);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page}封装对象
   */
  public <T> Map<String, Page<T>> find(final String queryString, final Pageable pageRequest, final Function<SolrDocument, ? extends T> function) {
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
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page}封装对象
   */
  public <T> Map<String, Page<T>> find(final String queryString, final Pageable pageRequest, final Function<SolrDocument, ? extends T> function, ExecutorService executor) {
    return find(queryString, new PagedSolrQueryProfile<T>(new GuavaFunctionResultTransformer<T>(function), pageRequest), executor);
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @return 符合查询条件的{@link com.topsoft.search.domain.Page}分页对象，内部结果集合类型为{@link org.apache.solr.common.SolrDocument}
   */
  public Page<SolrDocument> findFrom(String coreName, String queryString, Pageable pageRequest) {
    return findFrom(coreName, queryString, new PagedSolrQueryProfile<SolrDocument>(ResultTransformer.DEFAULT, pageRequest));
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @return 符合查询条件的{@link com.topsoft.search.domain.Page}分页对象，内部结果集合类型为 <T> 的对象
   */
  public <T> Page<T> findFrom(String coreName, String queryString, Pageable pageRequest, Function<SolrDocument, ? extends T> function) {
    return findFrom(coreName, queryString, new PagedSolrQueryProfile<T>(new GuavaFunctionResultTransformer<T>(function), pageRequest));
  }
}
