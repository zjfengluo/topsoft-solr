package com.topsoft.search.domain;

import java.util.Iterator;
import java.util.List;

/**
 * 分页查询的只读视图 (该对象是通用的, 不依赖于具体ORM实现, 页数从1开始。)
 *
 * @param <T> 当前页中的记录对象类型
 * @author wangyg
 */
public interface Page<T> extends Iterable<T> {

  /**
   * 返回当前页数，当前页数大于等于1，小于等于总页数
   *
   * @return 当前页数
   */
  int getNumber();

  /**
   * 返回页面大小(记录数容量)
   *
   * @return 页面大小(记录数容量)
   */
  int getSize();

  /**
   * 返回总页数
   *
   * @return 总页数
   */
  int getTotalPages();

  /**
   * 返回当前页的记录数
   *
   * @return 当前页的记录数
   */
  int getNumberOfElements();

  /**
   * 返回总记录数
   *
   * @return 总记录数
   */
  long getTotalElements();

  /**
   * 返回是否有前一页
   *
   * @return 是否有前一页
   */
  boolean hasPreviousPage();

  /**
   * 返回是否是第一页
   *
   * @return 是否是第一页
   */
  boolean isFirstPage();

  /**
   * 返回是否有下一页
   *
   * @return 是否有下一页
   */
  boolean hasNextPage();

  /**
   * 返回是否是最后一页
   *
   * @return 是否是最后一页
   */
  boolean isLastPage();

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Iterable#iterator()
   */
  @Override
  Iterator<T> iterator();

  /**
   * 以List<T>形式返回当前页的内容(记录列表)
   *
   * @return 当前页的记录(记录列表)
   */
  List<T> getContent();

  /**
   * 返回当前页内容是否为空(记录列表为空)
   *
   * @return 当前页的内容
   */
  boolean hasContent();

  /**
   * 返回排序参数
   *
   * @return 排序参数
   */
  Sort getSort();

  /**
   * 返回一个以当前页为中心的页号列表, 可实现如"首页, 10, 11, 12, 末页"效果
   *
   * @param count 要返回的列表大小
   * @return 页号列表
   */
  int[] getSlide(int count);

}
