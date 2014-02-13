package com.topsoft.search.domain;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.topsoft.search.domain.Sort.OrderBy;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;
import static com.topsoft.search.domain.Sort.Order.asc;

/**
 * 分页查询中的排序, 该对象是不可变的线程安全对象。
 * <p/>
 * 除了提供静态工厂方法构造以外，还提供builder模式的动态构建。
 *
 * @author wangyg
 * @see Sort#builder()
 */
public class Sort implements Iterable<OrderBy>, Serializable {

  private static final long serialVersionUID = -3711118288266769269L;
  private final ImmutableList<OrderBy> orderBys;

  private Sort(List<OrderBy> orderBys) {
    this.orderBys = ImmutableList.copyOf(orderBys);
  }

  private Sort(Order order, List<String> properties) {
    com.google.common.collect.ImmutableList.Builder<OrderBy> builder = ImmutableList.builder();
    for (String property : properties) {
      builder.add(Order.by(property, order));
    }

    this.orderBys = builder.build();
  }

  public static Sort of(String p1, String o1) {
    return of(Order.by(p1, o1));
  }

  public static Sort of(String p1, String o1, String p2, String o2) {
    return of(Order.by(p1, o1), Order.by(p2, o2));
  }

  public static Sort of(String p1, String o1, String p2, String o2, String p3, String o3) {
    return of(Order.by(p1, o1), Order.by(p2, o2), Order.by(p3, o3));
  }

  public static Sort of(String p1, String o1, String p2, String o2, String p3, String o3, String p4, String o4) {
    return of(Order.by(p1, o1), Order.by(p2, o2), Order.by(p3, o3), Order.by(p4, o4));
  }

  public static Sort of(String p1, String o1, String p2, String o2, String p3, String o3, String p4, String o4,
                        String p5, String o5) {
    return of(Order.by(p1, o1), Order.by(p2, o2), Order.by(p3, o3), Order.by(p4, o4), Order.by(p5, o5));
  }

  public static Sort of(String p1, Order o1) {
    return of(Order.by(p1, o1));
  }

  public static Sort of(String p1, Order o1, String p2, Order o2) {
    return of(Order.by(p1, o1), Order.by(p2, o2));
  }

  public static Sort of(String p1, Order o1, String p2, Order o2, String p3, Order o3) {
    return of(Order.by(p1, o1), Order.by(p2, o2), Order.by(p3, o3));
  }

  public static Sort of(String p1, Order o1, String p2, Order o2, String p3, Order o3, String p4, Order o4) {
    return of(Order.by(p1, o1), Order.by(p2, o2), Order.by(p3, o3), Order.by(p4, o4));
  }

  public static Sort of(String p1, Order o1, String p2, Order o2, String p3, Order o3, String p4, Order o4, String p5,
                        Order o5) {
    return of(Order.by(p1, o1), Order.by(p2, o2), Order.by(p3, o3), Order.by(p4, o4), Order.by(p5, o5));
  }

  public static Sort of(OrderBy requiredOrder, OrderBy... orderBys) {
    checkNotNull(requiredOrder, "You have to provide at least one sort property to sort by!");

    List<OrderBy> orderByList = orderBys == null ? new ArrayList<OrderBy>(1) : new ArrayList<OrderBy>(
        1 + orderBys.length);

    orderByList.add(requiredOrder);

    if (orderBys != null) {
      for (OrderBy orderBy : orderBys) {
        orderByList.add(orderBy);
      }
    }

    return of(orderByList);
  }

  public static Sort of(List<OrderBy> orderBys) {
    checkArgument(orderBys != null && !orderBys.isEmpty(), "You have to provide at least one sort property to sort by!");

    return new Sort(orderBys);
  }

  public static Sort with(Order order, String requireProperty, String... properties) {
    checkNotNull(requireProperty, "You have to provide at least one sort property to sort by!");

    List<String> propertyList = properties == null ? new ArrayList<String>(1) : new ArrayList<String>(
        1 + properties.length);

    propertyList.add(requireProperty);

    if (properties != null) {
      for (String property : properties) {
        propertyList.add(property);
      }
    }

    return with(order, propertyList);
  }

  public static Sort with(Order order, List<String> properties) {
    checkNotNull(order, "The order of sort property must not be null!");
    checkArgument(properties != null && !properties.isEmpty(),
        "You have to provide at least one sort property to sort by!");

    return new Sort(order, properties);
  }

  public static Builder builder() {
    return new Builder();
  }

  public OrderBy find(final String property) {
    return Iterables.find(orderBys, new Predicate<OrderBy>() {

      @Override
      public boolean apply(@Nullable OrderBy input) {
        return input != null ? input.getProperty().equals(property) : false;
      }
    }, null);
  }

  public boolean contain(String property) {
    return find(property) != null;
  }

  public Sort concat(Sort sort) {
    if (sort == null) {
      return this;
    }

    ArrayList<OrderBy> other = newArrayList(this.orderBys);

    for (OrderBy orderBy : sort) {
      other.add(orderBy);
    }

    return new Sort(other);
  }

  @Override
  public Iterator<OrderBy> iterator() {
    return orderBys.iterator();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(orderBys);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Sort other = (Sort) obj;

    return Objects.equal(orderBys, other.orderBys);
  }

  @Override
  public String toString() {
    return Joiner.on(", ").join(Iterables.transform(orderBys, Functions.toStringFunction()));
  }

  public static enum Order {
    asc, desc;

    public static Order of(String value) {
      checkArgument(!Strings.isNullOrEmpty(value), "The value must to be either 'desc' or 'asc'(case insensitive).");
      try {
        return Order.valueOf(value.toLowerCase(Locale.US));
      } catch (Exception e) {
        throw new IllegalArgumentException(String.format(
            "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
      }
    }

    public static OrderBy by(String property) {
      return new OrderBy(property, Order.asc);
    }

    public static OrderBy by(String property, String orderStrify) {
      return new OrderBy(property, of(orderStrify));
    }

    public static OrderBy by(String property, Order order) {
      return new OrderBy(property, order);
    }
  }

  public static class Builder {
    private LinkedList<OrderBy> orderBys;

    public Builder() {
      orderBys = newLinkedList();
    }

    public Sort build() {
      return Sort.of(orderBys);
    }

    public Builder orderBy(String property) {
      return orderBy(Order.by(property));
    }

    public Builder orderBy(String property, String orderStrify) {
      return orderBy(Order.by(property, orderStrify));
    }

    public Builder orderBy(String property, Order order) {
      return orderBy(Order.by(property, order));
    }

    public Builder orderBy(OrderBy order) {
      this.orderBys.add(checkNotNull(order, "Order property must not be null!"));

      return this;
    }

    public Builder orderBy(OrderBy... orderBys) {
      if (orderBys != null) {
        return orderBy(Arrays.asList(orderBys));
      }

      return this;
    }

    public Builder orderBy(Iterable<OrderBy> orderBys) {
      if (orderBys != null) {
        for (OrderBy orderBy : orderBys) {
          orderBy(orderBy);
        }
      }

      return this;
    }

  }

  public static class OrderBy implements Serializable {
    private static final long serialVersionUID = -6243085038918765715L;
    private final String property;
    private final Order order;

    private OrderBy(String property, Order order) {
      checkArgument(!Strings.isNullOrEmpty(property), "property can not be null or empty!");
      this.property = property;
      this.order = checkNotNull(order, "order can not be null!");
    }

    public Order getOrder() {
      return order;
    }

    public String getProperty() {
      return property;
    }

    public boolean isAscending() {
      return asc == order;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(property, order);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Sort.OrderBy other = (Sort.OrderBy) obj;

      return Objects.equal(property, other.property) && Objects.equal(order, other.order);
    }

    @Override
    public String toString() {
      return String.format("%s %s", property, order);
    }

  }

}
