package com.topsoft.search.domain;

/**
 * 分页查询的参数
 *
 * @author wangyg
 */
public interface Pageable {

  public static final Pageable DEFAULT = new PageRequest(PageRequest.DEFAULT_PAGE, PageRequest.DEFAULT_SIZE, null);

  /**
   * 返回要查询的页数
   *
   * @return 查询页数
   */
  int getPageNumber();

  /**
   * 返回要查询的页面大小(记录数容量)
   *
   * @return 页面大小(记录数容量)
   */
  int getPageSize();

  /**
   * 返回根据查询页数和页面大小所计算出的记录数偏移量
   *
   * @return 记录数偏移量
   */
  int getOffset();

  /**
   * 返回排序参数
   *
   * @return 排序参数
   */
  Sort getSort();
}
