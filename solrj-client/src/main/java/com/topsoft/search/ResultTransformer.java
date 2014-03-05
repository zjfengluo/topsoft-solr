package com.topsoft.search;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.util.List;

/**
 * @author wangyg
 */
public interface ResultTransformer<C extends Iterable> {

  public static final ResultTransformer<List<SolrDocument>> DEFAULT = new ResultTransformer<List<SolrDocument>>() {
    @Override
    public List<SolrDocument> transform(QueryResponse response) {
      return response.getResults();
    }
  };

  /**
   * 对查询的结果 {@link org.apache.solr.client.solrj.response.QueryResponse} 进行后期处理，可进行数据转换及封装
   *
   * @param response
   * @return
   */
  C transform(QueryResponse response);
}
