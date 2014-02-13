package com.topsoft.search.domain;

import com.google.common.base.Objects;
import com.topsoft.search.domain.Sort.Order;
import com.topsoft.search.domain.Sort.OrderBy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;

/**
 * 分页查询参数的默认实现, 该对象是不可变的线程安全对象。
 * <p/>
 * 除了通过构造函数静态构建以外，还提供builder模式的动态构建。
 *
 * @author wangyg
 * @see PageRequest#builder()
 */
public class PageRequest implements Pageable, Serializable {

  public static final int DEFAULT_PAGE = 1;
  public static final int DEFAULT_SIZE = 20;
  private static final long serialVersionUID = 4790905357827607563L;
  private final int page; // start with 1
  private final int size;
  private final Sort sort;

  public PageRequest(int page, int size, Sort sort) {
    checkArgument(page > 0, "Page index must not be less than or equal to zero!");
    checkArgument(size > 0, "Page size must not be less than or equal to zero!");
    this.page = page;
    this.size = size;
    this.sort = sort;
  }

  public static Builder builder() {
    return new Builder(DEFAULT_PAGE, DEFAULT_SIZE);
  }

  public static Builder builder(int page, int size) {
    return new Builder(page, size);
  }

  @Override
  public int getPageNumber() {
    return page;
  }

  @Override
  public int getPageSize() {
    return size;
  }

  @Override
  public int getOffset() {
    return (page - 1) * size;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(page, size, sort);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PageRequest other = (PageRequest) obj;

    return Objects.equal(page, other.page) && Objects.equal(size, other.size) && Objects.equal(sort, other.sort);
  }

  @Override
  public String toString() {
    return String.format("page:%d, size:%d, sort by:[%s]", page, size, sort);
  }

  public static final class Builder {
    private int page;
    private int size;
    private LinkedList<OrderBy> orders;

    public Builder() {
      this(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public Builder(int page, int size) {
      page(page).size(size);
      orders = newLinkedList();
    }

    public Builder page(int page) {
      checkArgument(page > 0, "Page index must not be less than or equal to zero!");
      this.page = page;
      return this;
    }

    public Builder size(int size) {
      checkArgument(size > 0, "Page size must not be less than or equal to zero!");
      this.size = size;
      return this;
    }

    public Builder orderBy(String property) {
      return orderBy(Order.by(property));
    }

    public Builder orderBy(String property, String orderString) {
      return orderBy(Order.by(property, orderString));
    }

    public Builder orderBy(String property, Order order) {
      return orderBy(Order.by(property, order));
    }

    public Builder orderBy(OrderBy order) {
      this.orders.add(checkNotNull(order, "OrderBy property must not be null!"));

      return this;
    }

    public Builder orderBy(OrderBy... orders) {
      if (orders != null) {
        return orderBy(Arrays.asList(orders));
      }

      return this;
    }

    public Builder orderBy(Iterable<OrderBy> orders) {
      if (orders != null) {
        for (OrderBy orderBy : orders) {
          orderBy(orderBy);
        }
      }

      return this;
    }

    public PageRequest build() {
      return new PageRequest(page, size, orders.isEmpty() ? null : Sort.of(orders));
    }

  }

}
