package com.topsoft.search;

/**
 * @author wangyg
 */
public interface SolrQueryProfile<C extends Iterable> extends QueryPreProcessor, ResultTransformer<C> {
  QueryPreProcessor getQueryPreProcessor();

  ResultTransformer<C> getResultTransformer();
}
