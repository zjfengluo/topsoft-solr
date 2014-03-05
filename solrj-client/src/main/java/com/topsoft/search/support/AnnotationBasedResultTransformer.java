package com.topsoft.search.support;

import com.topsoft.search.ResultTransformer;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class AnnotationBasedResultTransformer<E> implements ResultTransformer<List<E>> {
  private final Class<E> annotatedClass;

  public AnnotationBasedResultTransformer(Class<E> annotatedClass) {
    checkNotNull(annotatedClass, "annotatedClass can not be null!");
    this.annotatedClass = annotatedClass;
  }

  @Override
  public List<E> transform(QueryResponse response) {
    return response.getBeans(annotatedClass);
  }
}
