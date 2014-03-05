package com.topsoft.search;

import com.google.common.base.Function;
import com.google.common.util.concurrent.MoreExecutors;
import com.topsoft.search.support.GenericSolrQueryProfile;
import com.topsoft.search.support.GuavaFunctionResultTransformer;
import org.apache.solr.common.SolrDocument;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * solr的简易查询类
 * <p>
 * <p>主要用于slor的multicore查询，支持单core查询，也支持多core并行查询。
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
 *
 * }</pre>
 *
 * @author wangyg
 */
public class SolrMultiCoreQuery {

  protected final SolrMultiCoreQueryTemplate template;

  public SolrMultiCoreQuery(String baseUrl) {
    this.template = new SolrMultiCoreQueryTemplate(baseUrl);
  }

  /**
   * 并行查询solr服务器上所有core
   * <p>用同样的查询条件依次查询所有core，并使用{@link com.topsoft.search.support.GenericSolrQueryProfile<T>} 对象
   * 对{@link org.apache.solr.client.solrj.SolrQuery}参数进行预先设置和查询结果数据进行后期转换及封装
   *
   * @param queryString 符合solr查询语法的字符串
   * @param profile     对查询参数进行预先设置和对查询结果数据进行后期转换及封装
   * @param executor    用于执行并行查询的 {@link java.util.concurrent.ExecutorService} 的对象
   * @param <T>
   * @return 一个Map结构的结果集，key为core名称，value为该core下符合查询条件的{@link List} 对象
   */
  public <T> Map<String, List<T>> findAll(String queryString, GenericSolrQueryProfile<T> profile, ExecutorService executor) {
    return template.find(queryString, profile, executor);
  }

  /**
   * 查询solr服务器上的某个core
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @param profile     对查询参数进行预先设置和对查询结果数据进行后期转换及封装
   * @param <T>
   * @return 符合查询条件的结果集合，元素类型为T
   */
  public <T> List<T> findAllFrom(String coreName, String queryString, GenericSolrQueryProfile<T> profile) {
    return template.findFrom(coreName, queryString, profile);
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
    return findAll(queryString, GenericSolrQueryProfile.DEFAULT, executor);
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
  public <T> Map<String, List<T>> findAll(final String queryString, final Function<SolrDocument, ? extends T> function) {
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
  public <T> Map<String, List<T>> findAll(final String queryString, final Function<SolrDocument, ? extends T> function, ExecutorService executor) {
    GenericSolrQueryProfile<T> profile = new GenericSolrQueryProfile<T>(QueryPreProcessor.DEFAULT, new GuavaFunctionResultTransformer<T>(function));
    return findAll(queryString, profile, executor);
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @return 符合查询条件的结果集合，元素类型为{@link org.apache.solr.common.SolrDocument}
   */
  public List<SolrDocument> findAllFrom(String coreName, String queryString) {
    return findAllFrom(coreName, queryString, GenericSolrQueryProfile.DEFAULT);
  }

  /**
   * 查询solr服务器上的某个core
   * <p>针对特定的core进行查询，并使用{@link com.google.common.base.Function} 对象将查询结果转换为自定义的数据结构形式
   *
   * @param coreName    core名称
   * @param queryString 符合solr查询语法的字符串
   * @param function    用于将 {@link org.apache.solr.common.SolrDocument} 对象转换为 <T> 对象的 {@link com.google.common.base.Function} 函数
   * @param <T>         用于封装查询结果的数据结构
   * @return 符合查询条件的结果集合，元素类型为{@link org.apache.solr.common.SolrDocument}
   */
  public <T> List<T> findAllFrom(String coreName, String queryString, Function<SolrDocument, ? extends T> function) {
    GenericSolrQueryProfile<T> profile = new GenericSolrQueryProfile<T>(QueryPreProcessor.DEFAULT, new GuavaFunctionResultTransformer<T>(function));
    return findAllFrom(coreName, queryString, profile);
  }

}
