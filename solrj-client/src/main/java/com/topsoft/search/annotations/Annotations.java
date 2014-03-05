package com.topsoft.search.annotations;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Set;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class Annotations {
  private static final LoadingCache<Class, Set<AnnotatedField>> cache = CacheBuilder.newBuilder()
      .build(new CacheLoader<Class, Set<AnnotatedField>>() {
        @Override
        public Set<AnnotatedField> load(Class key) throws Exception {
          return collectAnnotationInfo(key);
        }
      });

  private static final Predicate<AnnotatedField> isHighlight = new Predicate<AnnotatedField>() {
    @Override
    public boolean apply(@Nullable AnnotatedField input) {
      return input.isHighlight();
    }
  };

  private static final Predicate<AnnotatedField> isDocumentId = new Predicate<AnnotatedField>() {
    @Override
    public boolean apply(@Nullable AnnotatedField input) {
      return input.isDocumentId();
    }
  };

  private static final Function<AnnotatedField, String> getName = new Function<AnnotatedField, String>() {
    @Nullable
    @Override
    public String apply(@Nullable AnnotatedField input) {
      return input.getName();
    }
  };

  public static final Set<String> getHighlightFieldNames(Class annotatedClass) {
    Set<AnnotatedField> fields = cache.getUnchecked(annotatedClass);
    return FluentIterable.from(fields).filter(isHighlight).transform(getName).toImmutableSet();
  }

  public static final String getDocumentIdName(Class annotatedClass) {
    Set<AnnotatedField> fields = cache.getUnchecked(annotatedClass);
    return Iterables.tryFind(fields, isDocumentId).or(AnnotatedField.NO_EXIST).getName();
  }

  public static final <T, T2 extends T> String getDocumentIdValue(Class<T> annotatedClass, T2 bean) throws IllegalAccessException {
    Field idField = Iterables.tryFind(cache.getUnchecked(annotatedClass), isDocumentId).or(AnnotatedField.NO_EXIST).getField();
    return idField == null ? null : String.valueOf(idField.get(bean));
  }

  private static Set<AnnotatedField> collectAnnotationInfo(Class annotatedClass) {
    Set<AnnotatedField> results = Sets.newHashSet();
    Class superClass = annotatedClass;

    int documentIdPresentNum = 0;
    while (superClass != null && superClass != Object.class) {
      Field[] declaredFields = superClass.getDeclaredFields();

      for (Field declaredField : declaredFields) {
        boolean documentIdPresent = declaredField.isAnnotationPresent(DocumentId.class);
        boolean highlightPresent = declaredField.isAnnotationPresent(Highlight.class);
        if (documentIdPresent) {
          documentIdPresentNum++;
        }
        if (highlightPresent || documentIdPresent) {
          declaredField.setAccessible(true);
          results.add(new AnnotatedField(declaredField));
        }
      }

      superClass = superClass.getSuperclass();
    }

    String errorMessageTemplate = "There are %s fields was annotated with @DocumentId, It must be only once!";
    checkState(documentIdPresentNum <= 1, errorMessageTemplate, documentIdPresentNum);

    return results;
  }

  private static class AnnotatedField {
    private String name;
    private Field field;
    private boolean highlight;
    private boolean documentId;


    public static final AnnotatedField NO_EXIST = new AnnotatedField();

    AnnotatedField() {

    }

    AnnotatedField(Field field) {
      this.field = field;
      this.highlight = field.isAnnotationPresent(Highlight.class);
      this.documentId = field.isAnnotationPresent(DocumentId.class);
      setName(field);
    }

    private void setName(Field field) {

      if (field.isAnnotationPresent(org.apache.solr.client.solrj.beans.Field.class)) {
        org.apache.solr.client.solrj.beans.Field annotation = field.getAnnotation(org.apache.solr.client.solrj.beans.Field.class);
        if (!org.apache.solr.client.solrj.beans.Field.DEFAULT.equals(annotation.value())) {
          // Field没采用默认值，说明设置了字段名，优先级最高，按照此值作为solr字段映射名
          this.name = annotation.value();
        }
      } else {
        // 没有被Field标注，或Field没有设置字段名，继续检查highlight及documentId标注是否设置了字段名
        String documentIdName = null;
        String highlightName = null;
        if (isDocumentId()) {
          documentIdName = getDocumentIdName();
          this.name = documentIdName;
        }

        if (isHighlight()) {
          highlightName = getHighlightName();
          this.name = highlightName;
        }

        if (isDocumentId() && isHighlight()) {
          String errorMessageTemplate = "@DocumentId[value=\"%s\"] and @Highlight[value=\"%s\"] must have same value!";
          checkArgument(documentIdName.equals(highlightName), errorMessageTemplate, documentIdName, highlightName);
        }

      }
    }

    private String getHighlightName() {
      Highlight annotation = field.getAnnotation(Highlight.class);
      return annotation.DEFAULT.equals(annotation.value()) ? field.getName() : annotation.value();
    }

    private String getDocumentIdName() {
      DocumentId annotation = field.getAnnotation(DocumentId.class);
      return annotation.DEFAULT.equals(annotation.value()) ? field.getName() : annotation.value();
    }

    public String getName() {
      return name;
    }

    public Field getField() {
      return field;
    }

    public String getBeanPropertyName() {
      return field == null ? null : field.getName();
    }

    public boolean isHighlight() {
      return highlight;
    }

    public boolean isDocumentId() {
      return documentId;
    }
  }
}
