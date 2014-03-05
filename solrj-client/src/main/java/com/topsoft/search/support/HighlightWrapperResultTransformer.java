package com.topsoft.search.support;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.topsoft.search.ResultTransformer;
import com.topsoft.search.annotations.Annotations;
import com.topsoft.search.annotations.DocumentId;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class HighlightWrapperResultTransformer<T> implements ResultTransformer<List<HighlightWrapper<T>>> {
  private final Class<T> annotatedClass;
  private final Function<SolrDocument, T> transformer;
  private final String idName;

  public HighlightWrapperResultTransformer(Class<T> annotatedClass) {
    this.annotatedClass = checkNotNull(annotatedClass, "annotatedClass can not be null!");
    this.transformer = null;
    this.idName = null;
  }

  public HighlightWrapperResultTransformer(String idName, Function<SolrDocument, T> transformer) {
    this.idName = checkNotNull(idName, "idName can not be null!");
    this.transformer = checkNotNull(transformer, "transformer can not be null!");
    this.annotatedClass = null;
  }

  @Override
  public List<HighlightWrapper<T>> transform(QueryResponse response) {
    final SolrDocumentList docs = response.getResults();
    final Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();

    HighlightWrapperFunction<T> wrapper = annotatedClass != null ?
        new HighlightWrapperFunction<T>(annotatedClass, highlighting) :
        new HighlightWrapperFunction<T>(idName, transformer, highlighting);

    return Lists.transform(docs, wrapper);
  }

  private static class HighlightWrapperFunction<T> implements Function<SolrDocument, HighlightWrapper<T>> {
    private static final DocumentObjectBinder BINDER = new DocumentObjectBinder();
    private final Class<T> annotatedClass;
    private final Function<SolrDocument, T> transformer;
    private final String idName;
    private final Map<String, Map<String, List<String>>> highlighting;

    public HighlightWrapperFunction(Class<T> annotatedClass, Map<String, Map<String, List<String>>> highlighting) {
      this.annotatedClass = annotatedClass;
      this.idName = Annotations.getDocumentIdName(annotatedClass);
      checkState(idName != null, "Annotation[@%s] has not found in %s!",
          DocumentId.class.getName(), annotatedClass.getName());
      this.highlighting = checkNotNull(highlighting);
      this.transformer = null;
    }

    public HighlightWrapperFunction(String idName, Function<SolrDocument, T> transformer, Map<String, Map<String, List<String>>> highlighting) {
      this.idName = idName;
      this.transformer = transformer;
      this.highlighting = checkNotNull(highlighting);
      this.annotatedClass = null;
    }

    @Nullable
    @Override
    public HighlightWrapper<T> apply(@Nullable SolrDocument doc) {
      String idValue = String.valueOf(doc.getFieldValue(idName));
      T bean = annotatedClass != null ? BINDER.getBean(annotatedClass, doc) : transformer.apply(doc);
      return new HighlightWrapper<T>(bean, highlighting.get(idValue));
    }
  }

}
