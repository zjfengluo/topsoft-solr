package com.topsoft.search.icis.ecps;

import java.util.Date;

/**
 * 拓普企业信用公示系统-查询结果记录
 * 
 * @author weichao
 *
 */
public interface TopEntBaseInfo {

	/**
	 * 获取企业信息记录Id
	 * 
	 * @return 企业信息记录Id
	 */
	Long getId();

	/**
	 * 获取企业名称
	 * 
	 * @return 企业名称
	 */
	String getEntName();
	
	/**
	 * 获取注册号
	 * 
	 * @return 注册号
	 */
	String getRegNo();
	
	/**
	 * 获取法定代表人
	 * 
	 * @return 法定代表人
	 */
	String getLeRep();
	
	/**
	 * 获取经营场所(住所)
	 * 
	 * @return 经营场所(住所)
	 */
	String getOpLocOrDom();
	
	/**
	 * 获取注册资本
	 * 
	 * @return 注册资本
	 */
	Float getRegCap();
	
	/**
	 * 获取注册资本币种名称
	 * 
	 * @return 注册资本币种名称
	 */
	String getRegCapCurName();
	
	/**
	 * 获取行业类型名称
	 * 
	 * @return 行业类型名称
	 */
	String getIndustryPhyName();
	
	/**
	 * 获取成立日期
	 * 
	 * @return 成立日期
	 */
	Date getEstDate();
}
