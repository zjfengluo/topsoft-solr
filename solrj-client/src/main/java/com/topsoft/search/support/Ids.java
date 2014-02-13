package com.topsoft.search.support;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.apache.solr.common.SolrDocument;

import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class Ids {
  public static final String DEFAULT_ID_NAME = "id";
  // ID类型选用Long最为普遍，为效率优化故，选作默认
  public static final Function<SolrDocument, Long> GET = new ExtractFieldFunction<Long>(DEFAULT_ID_NAME);

  private Ids() {
  }

  public static Iterable<Long> of(Iterable<SolrDocument> docs) {
    return Iterables.transform(docs, GET);
  }

  public static Iterable<Long> of(Iterable<SolrDocument> docs, String idName) {
    return of(docs, idName, Long.class);
  }

  public static <ID extends Serializable> Iterable<ID> of(Iterable<SolrDocument> docs, String idName, Class<ID> idType) {
    return Iterables.transform(docs, new ExtractFieldFunction<ID>(idName));
  }

  private static class ExtractFieldFunction<T> implements Function<SolrDocument, T> {
    private final String fieldName;

    public ExtractFieldFunction(String fieldName) {
      checkArgument(!Strings.isNullOrEmpty(fieldName), "fieldName must not be null or empty!");
      this.fieldName = fieldName;
    }

    @Nullable
    @Override
    public T apply(@Nullable SolrDocument doc) {
      return (T) doc.getFieldValue(fieldName);
    }
  }
}
