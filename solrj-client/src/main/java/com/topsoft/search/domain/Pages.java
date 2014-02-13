package com.topsoft.search.domain;

import java.util.List;

/**
 * 构造{@link Page}以及{@link Pageable}的一些工厂方法
 *
 * @author wangyg
 */
public class Pages {
  private static final Pageable DEFAULT_PAGEABLE = new PageRequest(PageRequest.DEFAULT_PAGE, PageRequest.DEFAULT_SIZE,
      null);

  public static final <T> Page<T> of(List<? extends T> content) {
    return new PageImpl<T>(content);
  }

  public static final <T> Page<T> of(List<? extends T> content, Pageable pageable, long total) {
    return new PageImpl<T>(content, pageable, total);
  }

  public static final Pageable defaultPageable() {
    return DEFAULT_PAGEABLE;
  }


}
