package com.topsoft.search.support;

import com.topsoft.search.QueryPreProcessor;
import com.topsoft.search.ResultTransformer;
import com.topsoft.search.SolrQueryProfile;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class GenericSolrQueryProfile<T> implements SolrQueryProfile<List<T>> {

  public static final GenericSolrQueryProfile<SolrDocument> DEFAULT =
      new GenericSolrQueryProfile<SolrDocument>(QueryPreProcessor.DEFAULT, ResultTransformer.DEFAULT);

  private final QueryPreProcessor preProcessor;

  private final ResultTransformer<List<T>> resultTransformer;

  public GenericSolrQueryProfile(QueryPreProcessor preProcessor, ResultTransformer<List<T>> resultTransformer) {
    checkNotNull(resultTransformer, "resultTransformer must not be null!");
    this.preProcessor = preProcessor;
    this.resultTransformer = resultTransformer;
  }


  @Override
  public void prepare(SolrQuery query) {
    if (preProcessor != null) {
      preProcessor.prepare(query);
    }
  }

  @Override
  public List<T> transform(QueryResponse response) {
    return resultTransformer.transform(response);
  }

  @Override
  public QueryPreProcessor getQueryPreProcessor() {
    return this;
  }

  @Override
  public ResultTransformer<List<T>> getResultTransformer() {
    return this;
  }
}
