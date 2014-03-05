package com.topsoft.search;

import com.google.common.util.concurrent.MoreExecutors;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.support.AnnotationBasedResultTransformer;
import com.topsoft.search.support.GenericSolrQueryProfile;
import com.topsoft.search.support.PagedSolrQueryProfile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * <p>扩展了{@link com.topsoft.search.PagableSolrMultiCoreQuery}, 增加了将{@link org.apache.solr.common.SolrDocument}对象基于
 * {@code Class}类型自动转换为相应对象的功能。前提是该对象类型内部使用了{@link org.apache.solr.client.solrj.beans.Field} annotation
 *
 * @author wangyg
 */
public class AnnotationableSolrMultiCoreQuery extends PagableSolrMultiCoreQuery {
  public AnnotationableSolrMultiCoreQuery(String baseUrl) {
    super(baseUrl);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param clazz       用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 {@code Class<T>} 类型的实例对象
   * @param <T>
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link java.util.List} 对象
   */
  public <T> Map<String, List<T>> findAll(final String queryString, final Class<T> clazz) {
    return findAll(queryString, clazz, MoreExecutors.sameThreadExecutor());
  }

  /**
   * 查询solr服务器上所有core
   * <p>使用客户端提供的 {@link java.util.concurrent.ExecutorService} 对象，用相同的查询条件并行查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param clazz       用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 {@code Class<T>} 类型的实例对象
   * @param <T>
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public <T> Map<String, List<T>> findAll(final String queryString, final Class<T> clazz, ExecutorService executor) {
    GenericSolrQueryProfile<T> profile = new GenericSolrQueryProfile<T>(QueryPreProcessor.DEFAULT, new AnnotationBasedResultTransformer<T>(clazz));
    return findAll(queryString, profile, executor);
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @param clazz       用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 {@code Class<T>} 类型的实例对象
   * @param <T>
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public <T> List<T> findAllFrom(String coreName, String queryString, Class<T> clazz) {
    GenericSolrQueryProfile<T> profile = new GenericSolrQueryProfile<T>(QueryPreProcessor.DEFAULT, new AnnotationBasedResultTransformer<T>(clazz));
    return findAllFrom(coreName, queryString, profile);
  }

  /**
   * 查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param clazz       用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 {@code Class<T>} 类型的实例对象
   * @param <T>
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page}封装对象
   */
  public <T> Map<String, Page<T>> find(final String queryString, final Pageable pageRequest, final Class<T> clazz) {
    return find(queryString, pageRequest, clazz, MoreExecutors.sameThreadExecutor());
  }

  /**
   * 查询solr服务器上所有core
   * <p>使用客户端提供的 {@link java.util.concurrent.ExecutorService} 对象，用相同的查询条件并行查询所有core，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param clazz       用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 {@code Class<T>} 类型的实例对象
   * @param <T>
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page}封装对象
   */
  public <T> Map<String, Page<T>> find(final String queryString, final Pageable pageRequest, final Class<T> clazz, ExecutorService executor) {
    return find(queryString, new PagedSolrQueryProfile<T>(new AnnotationBasedResultTransformer<T>(clazz), pageRequest), executor);
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param queryString 符合solr查询语法的字符串
   * @param pageRequest 分页信息
   * @param clazz       用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 {@code Class<T>} 类型的实例对象
   * @param <T>
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link com.topsoft.search.domain.Page}封装对象
   */
  public <T> Page<T> findFrom(String coreName, String queryString, Pageable pageRequest, final Class<T> clazz) {
    return findFrom(coreName, queryString, new PagedSolrQueryProfile<T>(new AnnotationBasedResultTransformer<T>(clazz), pageRequest));
  }
}
