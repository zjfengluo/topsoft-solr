package com.topsoft.search;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public abstract class SolrQueryProfileDecorator<C2 extends Iterable, C extends Iterable> implements SolrQueryProfile<C> {
  private QueryPreProcessor preProcessor;
  private ResultTransformer<C2> resultTransformer;

  public SolrQueryProfileDecorator(SolrQueryProfile<C2> profile) {
    this(profile.getQueryPreProcessor(), profile.getResultTransformer());
  }

  public SolrQueryProfileDecorator(QueryPreProcessor preProcessor, ResultTransformer<C2> resultTransformer) {
    this.preProcessor = preProcessor;
    this.resultTransformer = checkNotNull(resultTransformer, "resultTransformer can not be null!");
  }

  @Override
  public QueryPreProcessor getQueryPreProcessor() {
    return this;
  }

  @Override
  public ResultTransformer<C> getResultTransformer() {
    return this;
  }

  @Override
  public void prepare(SolrQuery query) {
    if (preProcessor != null) {
      preProcessor.prepare(query);
    }

    selfPrepare(query);
  }

  @Override
  public C transform(QueryResponse response) {
    C2 prevResults = resultTransformer.transform(response);

    return selfTransform(response, prevResults);
  }

  protected abstract C selfTransform(QueryResponse response, C2 prevResults);

  protected abstract void selfPrepare(SolrQuery query);
}
