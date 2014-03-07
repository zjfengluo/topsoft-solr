package com.topsoft.search.icis.ecps;

import java.util.List;

/**
 * 全国企业信用信息公示系统-企业信息查询接口
 * 
 * @author weichao
 *
 */
public interface INationalSolrEnterpriseQuery extends ISolrEnterpriseQuery<NationalEntBaseInfoBean>{
	
	static final String coreName = "entbaseInfo";
	
	/**
	 * 根据用户输入关键词，检索企业信息
	 * <br/><p>
	 * <b>1</b>.该关键字可能是企业名称也可能是注册号，所以分别把该关键字跟这两个字段进行匹配。<br/>
	 *  该关键字跟注册号匹配时，进行精确匹配；该关键字跟企业名称进行匹配时，是模糊匹配。<br/>
	 *  返回结果是两次匹配的并集。<br/>
	 * </p><p>
	 * <b>2</b>.本查询提供命中结果的高亮展示。查询结果的企业名称中，跟关键字匹配的部分，<br/>
	 *  会加上一个&lt;span&gt;标签,使用自定义的css样式，样式名称为 ecps_solr_highlight，<br/>
	 *  本查询的调用者需要自己定义该样式，通过修改该样式，来控制高亮的效果。<br/>
	 * </p><p>
	 * <b>3</b>.查询结果按照企业成立日期倒序排列，且只返回前5条记录。
	 * </p>
	 * @param keyword 用户输入的查询关键字
	 * @return 符合条件的企业信息列表
	 */
	List<NationalEntBaseInfo> find(final String keyword);
}
