package com.topsoft.search;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * @author wangyg
 */
public interface QueryPreProcessor {
  public static final QueryPreProcessor DEFAULT = new QueryPreProcessor() {
    @Override
    public void prepare(SolrQuery query) {
      query.setStart(0);
      query.setRows(Integer.MAX_VALUE);
    }
  };

  /**
   * 对 {@link org.apache.solr.client.solrj.SolrQuery} 进行预先属性设置，可实现分页、高亮匹配、拼写检查等功能
   *
   * @param query
   */
  void prepare(SolrQuery query);
}
