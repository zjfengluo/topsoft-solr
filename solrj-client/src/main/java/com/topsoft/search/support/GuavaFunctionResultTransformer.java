package com.topsoft.search.support;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.topsoft.search.ResultTransformer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class GuavaFunctionResultTransformer<E> implements ResultTransformer<List<E>> {
  private final Function<SolrDocument, ? extends E> delegate;

  public GuavaFunctionResultTransformer(Function<SolrDocument, ? extends E> delegate) {
    checkNotNull(delegate, "function delegate can not be null!");
    this.delegate = delegate;
  }

  @Override
  public List<E> transform(QueryResponse response) {
    return Lists.transform(response.getResults(), delegate);
  }
}
