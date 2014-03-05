package com.topsoft.search.support;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class HighlightWrapper<T> {
  private final T bean;
  private final Map<String, String> highlights;

  private static final Function<Iterable<String>, String> COMMA_JOINER_FUNC = new Function<Iterable<String>, String>() {

    private Joiner COMMA_JOINER = Joiner.on(", ").skipNulls();

    @Nullable
    @Override
    public String apply(@Nullable Iterable<String> input) {
      return COMMA_JOINER.join(input);
    }
  };

  public HighlightWrapper(T bean, Map<String, ? extends Iterable<String>> highlights) {
    this.bean = checkNotNull(bean, "bean must not be null!");

    if (highlights == null) {
      this.highlights = ImmutableMap.of();
    } else {
      this.highlights = Maps.transformValues(highlights, COMMA_JOINER_FUNC);
    }
  }

  public T getBean() {
    return bean;
  }

  public Map<String, String> getHighlights() {
    return highlights;
  }
}
