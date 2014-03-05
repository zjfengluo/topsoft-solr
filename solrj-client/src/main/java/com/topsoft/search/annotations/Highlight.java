package com.topsoft.search.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author wangyg
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Highlight {
  public static final String DEFAULT = "_FIELD_DECLARATION_NAME_";

  String value() default DEFAULT;
}
