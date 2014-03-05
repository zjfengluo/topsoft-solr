package com.topsoft.search.support;

import com.google.common.base.Joiner;
import com.topsoft.search.QueryPreProcessor;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.HighlightParams;

import java.util.Set;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class HighlightQueryPreProcessor implements QueryPreProcessor {
  private static Joiner COMMA_JOINER = Joiner.on(',').skipNulls();
  private final HighlightParameters parameters;

  public HighlightQueryPreProcessor(HighlightParameters parameters) {
    this.parameters = checkNotNull(parameters, "parameters must not be null!");
  }

  @Override
  public void prepare(SolrQuery query) {
    Set<String> highlightFields = parameters.getFields();

    if (highlightFields.size() > 0) {
      query.setHighlight(true);
      query.setParam(HighlightParams.FIELDS, COMMA_JOINER.join(highlightFields));
    }

    String simplePre = parameters.getSimplePre();
    if (simplePre != null) {
      query.setHighlightSimplePre(simplePre);
    }

    String simplePost = parameters.getSimplePost();
    if (simplePost != null) {
      query.setHighlightSimplePost(simplePost);
    }

    int fragsize = parameters.getFragsize();
    if (fragsize > 0) {
      query.setHighlightFragsize(fragsize);
    }

    int snippets = parameters.getSnippets();
    if (snippets > 0) {
      query.setHighlightSnippets(snippets);
    }

    if (parameters.isRequireFieldMatch()) {
      query.setHighlightRequireFieldMatch(true);
    }
  }
}
