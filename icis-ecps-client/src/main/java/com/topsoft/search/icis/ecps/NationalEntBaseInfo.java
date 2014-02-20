package com.topsoft.search.icis.ecps;

import java.util.Date;

/**
 * 全国企业信用信息公示系统-查询结果记录
 * 
 * @author weichao
 *
 */
public interface NationalEntBaseInfo {
	
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
	 * 获取登记机关名称
	 * 
	 * @return 登记机关名称
	 */
	String getRegOrgName();
	
	/**
	 * 获取成立日期
	 * 
	 * @return 成立日期
	 */
	Date getEstDate();
}
