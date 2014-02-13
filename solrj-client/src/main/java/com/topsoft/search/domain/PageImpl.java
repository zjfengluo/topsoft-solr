package com.topsoft.search.domain;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * 分页查询视图的默认实现,前台程序员不用关心。该对象是不可变的线程安全对象。
 *
 * @param <T>
 * @author wangyg
 */
class PageImpl<T> implements Page<T>, Serializable {

  private static final long serialVersionUID = 5304141347243843434L;

  private final ImmutableList<T> content;
  private final Pageable pageable;
  private final long total;

  PageImpl(List<? extends T> content) {
    this(content, null, (null == content) ? 0 : content.size());
  }

  PageImpl(List<? extends T> content, Pageable pageable, long total) {
    checkArgument(total >= 0, "The number of total elements must not be less than zero!");

    this.content = ImmutableList.copyOf(checkNotNull(content));
    this.pageable = pageable;
    this.total = total;
  }

  @Override
  public int getNumber() {
    return pageable == null ? 1 : pageable.getPageNumber();
  }

  @Override
  public int getSize() {
    return pageable == null ? 0 : pageable.getPageSize();
  }

  @Override
  public int getTotalPages() {
    return getSize() == 0 ? 0 : (int) Math.ceil((double) total / (double) getSize());
  }

  @Override
  public int getNumberOfElements() {
    return content.size();
  }

  @Override
  public long getTotalElements() {
    return total;
  }

  @Override
  public boolean hasPreviousPage() {
    return getNumber() > 1;
  }

  @Override
  public boolean isFirstPage() {
    return !hasPreviousPage();
  }

  @Override
  public boolean hasNextPage() {
    return getNumber() + 1 <= getTotalPages();
  }

  @Override
  public boolean isLastPage() {
    return !hasNextPage();
  }

  @Override
  public Iterator<T> iterator() {
    return content.iterator();
  }

  @Override
  public List<T> getContent() {
    return content;
  }

  @Override
  public boolean hasContent() {
    return !content.isEmpty();
  }

  @Override
  public Sort getSort() {
    return pageable == null ? null : pageable.getSort();
  }

  @Override
  public int[] getSlide(int count) {
    checkArgument(count > 0, "count must not be less than or equal to zero!");

    if (pageable == null) {
      return new int[]{getNumber()};
    }

    int half = count / 2;
    int start = Math.max(getNumber() - half, 1);
    int end = Math.min(start + count - 1, getTotalPages());
    int slidePages = end - start + 1;

    int[] result = new int[slidePages];
    for (int i = 0, j = start; j <= end; i++, j++) {
      result[i] = j;
    }

    return result;
  }

  @Override
  public String toString() {
    if (getNumberOfElements() > 0) {

      return String.format("Page %d of %d has %d '%s'.", getNumber(), getTotalPages(), getNumberOfElements(), content
          .get(0).getClass().getName());
    }

    return String.format("Page %d of %d is empty.", getNumber(), getTotalPages());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(total, content, pageable);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PageImpl<?> other = (PageImpl<?>) obj;

    return Objects.equal(total, other.total) && Objects.equal(content, other.content)
        && Objects.equal(pageable, other.pageable);
  }

}
