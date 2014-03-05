package com.topsoft.search;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.params.CoreAdminParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;

/**
 * @author wangyg
 */
public class SolrMultiCoreQueryTemplate {
  protected final String baseUrl;

  public SolrMultiCoreQueryTemplate(String baseUrl) {
    this.baseUrl = normalizeBaseURL(baseUrl);
  }

  public <C extends Iterable> Map<String, C> find(final String queryString, final SolrQueryProfile<? extends C> profile, ExecutorService executor) {
    final HashMap<String, C> result = Maps.newHashMap();

    List<String> cores = getAllCoreNames();

    int size = cores.size();
    if (size > 0) {
      List<Callable<C>> querys = newArrayListWithCapacity(size);

      for (final String core : cores) {

        querys.add(new Callable<C>() {
          @Override
          public C call() throws Exception {
            return findFrom(core, queryString, profile);
          }
        });

      }

      try {
        List<Future<C>> queryResults = executor.invokeAll(querys);

        // queryResults里的元素顺序与query一致，即一一对应。
        for (int i = 0; i < queryResults.size(); i++) {
          Future<C> future = queryResults.get(i);
          result.put(cores.get(i), future.get());
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }
    return result;
  }

  public <C extends Iterable> C findFrom(String core, String queryString, SolrQueryProfile<? extends C> profile) {
    final SolrQuery solrQuery = new SolrQuery(queryString);
    profile.prepare(solrQuery);
    try {
      return profile.transform(getCore(core).query(solrQuery));
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

  protected String normalizeBaseURL(String baseURL) {
    checkArgument(!Strings.isNullOrEmpty(baseURL), "baseURL must not be null or empty!");

    if (baseURL.endsWith("/")) {
      baseURL = baseURL.substring(0, baseURL.length() - 1);
    }

    return baseURL.trim().toLowerCase();
  }
}
