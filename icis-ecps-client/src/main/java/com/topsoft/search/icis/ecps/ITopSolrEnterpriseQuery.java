package com.topsoft.search.icis.ecps;

import java.util.List;

import com.topsoft.search.domain.Page;

/**
 * 拓普企业信用公示系统-企业信息查询接口
 * 
 * @author weichao
 *
 */
public interface ITopSolrEnterpriseQuery extends ISolrEnterpriseQuery<TopEntBaseInfoBean>{
	
	public static final String coreName = "entbaseinfo";

	/**
	 * 根据企业名称查询企业信息
	 * <br/><p>
	 * <b>1</b>.分页获取,模糊匹配<br/>
	 * </p><p>
	 * <b>2</b>.本查询提供命中结果的高亮展示。查询结果的企业名称中，跟关键字匹配的部分，<br/>
	 *  会加上一个&lt;span&gt;标签,使用自定义的css样式，样式名称为 ecps_solr_highlight，<br/>
	 *  本查询的调用者需要自己定义该样式，通过修改该样式，来控制高亮的效果。<br/>
	 * </p><p>
	 * <b>3</b>.查询结果默认按照企业成立日期倒序排列。
	 * </p>
	 * 
	 * @param keyword 用户输入的查询关键字
	 * @param currentPage 当前页号
	 * @param pageSize 每页条数
	 * @return 包含企业信息和分页信息的Page对象实例
	 */
	Page<TopEntBaseInfo> findByEntName(final String keyword, int currentPage, int pageSize);
	
	/**
	 * 根据注册号查询企业信息
	 * <br/>
	 * 精确匹配，若没有匹配记录，则返回空列表
	 * 
	 * @param regNo 用户输入的注册号
	 * @return 符合条件的企业信息
	 */
	List<TopEntBaseInfo> findByRegNo(final String regNo);
	
	/**
	 * 根据法定代表人姓名查询企业信息
	 * <br/><p>
	 * <b>1</b>.分页获取,模糊匹配<br/>
	 * </p><p>
	 * <b>2</b>.本查询提供命中结果的高亮展示。查询结果的法定代表人中，跟关键字匹配的部分，<br/>
	 *  会加上一个&lt;span&gt;标签,使用自定义的css样式，样式名称为 ecps_solr_highlight，<br/>
	 *  本查询的调用者需要自己定义该样式，通过修改该样式，来控制高亮的效果。<br/>
	 * </p><p>
	 * <b>3</b>.查询结果默认按照企业成立日期倒序排列。
	 * </p>
	 * 
	 * @param keyword 用户输入的查询关键字
	 * @param currentPage 当前页号
	 * @param pageSize 每页条数
	 * @return 包含企业信息和分页信息的Page对象实例
	 */
	Page<TopEntBaseInfo> findByLeRep(final String keyword, int currentPage, int pageSize);
	
	/**
	 * 根据经营地址查询企业信息
	 * <br/><p>
	 * <b>1</b>.分页获取,模糊匹配<br/>
	 * </p><p>
	 * <b>2</b>.本查询提供命中结果的高亮展示。查询结果的经营地址中，跟关键字匹配的部分，<br/>
	 *  会加上一个&lt;span&gt;标签,使用自定义的css样式，样式名称为 ecps_solr_highlight，<br/>
	 *  本查询的调用者需要自己定义该样式，通过修改该样式，来控制高亮的效果。<br/>
	 * </p><p>
	 * <b>3</b>.查询结果默认按照企业成立日期倒序排列。
	 * </p>
	 * 
	 * @param keyword 用户输入的查询关键字
	 * @param currentPage 当前页号
	 * @param pageSize 每页条数
	 * @return 包含企业信息和分页信息的Page对象实例
	 */
	Page<TopEntBaseInfo> findByOpLoc(final String keyword, int currentPage, int pageSize);
	
	/**
	 * 企业信息高级查询
	 * <br/>
	 * <p><b>1</b>.用户输入的keyword查询参数，可能是企业名称、法定代表人、经营地址，<br/>
	 *  &nbsp;也可能是注册号，所以分别把该关键字跟这四个字段进行匹配。该关键字跟注册号匹配时，<br/>
	 *  &nbsp;进行精确匹配；该关键字跟企业名称、法定代表人、经营地址分别进行匹配时，是模糊匹配。<br/>
	 *  &nbsp;返回结果是四次匹配的并集。<br/>
	 * </p><p>
	 * <b>2</b>.本查询提供命中结果的高亮展示。查询结果的企业名称、法定代表人、经营地址中，跟关键字匹配的部分，<br/>
	 *  &nbsp;会加上一个&lt;span&gt;标签,使用自定义的css样式，样式名称为 ecps_solr_highlight，<br/>
	 *  &nbsp;本查询的调用者需要自己定义该样式，通过修改该样式，来控制高亮的效果。<br/>
	 * </p><p>
	 * <b>3</b>.查询结果默认按照企业成立日期倒序排列。
	 * </p><p>
	 * <b>4</b>.注意使用{@link RegCapLevel}的valueOf(int level)方法。
	 * </p>
	 * 
	 * @param keyword 用户输入的查询关键字
	 * @param opLocDistrict 地区范围(行政区划代码)
	 * @param industryPhy 行业门类代码
	 * @param regCapLevel 注册资本等级，定义如下：<br/>
	 * 						LEVEL1-(10万元以下) <br/>
	 * 						LEVEL2-(10万  ~ 100万) <br/>
	 * 						LEVEL3-(100万  ~ 1000万) <br/>
	 * 						LEVEL4-(1000万以上)
	 * @param entTypes 企业类型数组，元素是真实的企业类型代码，该数组限定了用户所希望获取到的所有企业类型。
	 * @param currentPage 当前页号
	 * @param pageSize 每页条数
	 * @return 企业基本信息
	 */
	Page<TopEntBaseInfo> advancedFind(final String keyword,
			final String opLocDistrict, final String industryPhy,
			final RegCapLevel regCapLevel, final String[] entTypes,
			int currentPage, int pageSize);
}
