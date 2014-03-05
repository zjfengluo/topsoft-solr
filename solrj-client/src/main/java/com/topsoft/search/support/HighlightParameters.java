package com.topsoft.search.support;

import com.google.common.collect.Sets;
import com.topsoft.search.annotations.Annotations;

import java.util.Collections;
import java.util.Set;

/**
 * @author wangyg
 */
public class HighlightParameters {
  private Set<String> fields;
  private String simplePre;
  private String simplePost;
  private int fragsize;
  private int snippets;
  private boolean requireFieldMatch;

  private HighlightParameters(Set<String> fields, String simplePre, String simplePost, int fragsize, int snippets, boolean requireFieldMatch) {
    this.fields = fields;
    this.simplePre = simplePre;
    this.simplePost = simplePost;
    this.fragsize = fragsize;
    this.snippets = snippets;
    this.requireFieldMatch = requireFieldMatch;
  }

  public Set<String> getFields() {
    return fields;
  }

  public String getSimplePre() {
    return simplePre;
  }

  public String getSimplePost() {
    return simplePost;
  }

  public int getFragsize() {
    return fragsize;
  }

  public int getSnippets() {
    return snippets;
  }

  public boolean isRequireFieldMatch() {
    return requireFieldMatch;
  }

  public static final Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Set<String> fields = Collections.emptySet();
    private String simplePre;
    private String simplePost;
    private int fragsize;
    private int snippets;
    private boolean requireFieldMatch;

    public HighlightParameters build() {
      return new HighlightParameters(fields, simplePre, simplePost, fragsize, snippets, requireFieldMatch);
    }

    public Builder setFields(Class annotatedClass){
      Set<String> fieldNames = Annotations.getHighlightFieldNames(annotatedClass);
      if (fieldNames.size() > 0) {
        this.fields = fieldNames;
      }
      return this;
    }

    public Builder setFields(String... fields) {
      if (fields.length > 0) {
        this.fields = Sets.newHashSet(fields);
      }
      return this;
    }

    public Builder addFields(String... fields) {
      for (String field : fields) {
        this.fields.add(field);
      }
      return this;
    }

    public Builder setSimplePre(String simplePre) {
      this.simplePre = simplePre;
      return this;
    }

    public Builder setSimplePost(String simplePost) {
      this.simplePost = simplePost;
      return this;
    }

    public Builder setFragsize(int fragsize) {
      this.fragsize = fragsize;
      return this;
    }

    public Builder setSnippets(int snippets) {
      this.snippets = snippets;
      return this;
    }

    public Builder setRequireFieldMatch(boolean requireFieldMatch) {
      this.requireFieldMatch = requireFieldMatch;
      return this;
    }
  }
}
