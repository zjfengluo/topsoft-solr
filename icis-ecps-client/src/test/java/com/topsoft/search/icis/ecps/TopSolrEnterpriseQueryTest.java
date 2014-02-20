package com.topsoft.search.icis.ecps;

import java.util.List;

import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.PageRequest;
import com.topsoft.search.domain.Pageable;

/**
 * 拓普企业信用信息公示系统企业查询接口 - 单元测试
 * 
 * @author weichao
 *
 */
public class TopSolrEnterpriseQueryTest {

	private static final String baseURL = "http://192.168.3.40:9081/solr";
	
	private Pageable pageRequest = PageRequest.builder(1, 10).build();
	
	private TopSolrEnterpriseQuery query = new TopSolrEnterpriseQuery(baseURL);
	
	private static final String keyword = "好运来";
	
	/**
	 * 测试根据企业名称进行模糊查询
	 */
	public void testFindByEntName() {
		Page<TopEntBaseInfo> page = query.findByEntName(keyword, pageRequest);
		assert page.getNumberOfElements() > 0 : "================== error when testing testFindByEntName ...";
		System.out.println("================== testFindByEntName testing  ok  ... totalDoc:" + page.getTotalElements());
	}
	
	/**
	 * 测试根据注册号进行查询
	 */
	public void testFindByRegNo() {
		List<TopEntBaseInfo> page = query.findByRegNo("410823620003885");
		assert page.size() == 1 : "================== error when testing testFindByRegNo ...";
		System.out.println("================== testFindByRegNo testing  ok  ...");
	}
	
	/**
	 * 测试根据法定代表人进行模糊查询
	 */
	public void testFindByLeRep() {
		Page<TopEntBaseInfo> page = query.findByLeRep(keyword, pageRequest);
		assert page.getNumberOfElements() > 0 : "================== error when testing testFindByLeRep ...";
		System.out.println("================== testFindByLeRep testing  ok  ... totalDoc:" + page.getTotalElements());
	}
	
	/**
	 * 测试根据经营地址进行模糊查询
	 */
	public void testFindByOpLoc() {
		Page<TopEntBaseInfo> page = query.findByOpLoc(keyword, pageRequest);
		assert page.getNumberOfElements() > 0 : "================== error when testing testFindByOpLoc ...";
		System.out.println("================== testFindByOpLoc testing  ok ... ... totalDoc:" + page.getTotalElements());
	}
	
	/**
	 * 测试高级查询
	 */
	public void testAdvancedFind() {
		Page<TopEntBaseInfo> page = query.advancedFind(keyword, null, null, null, null, pageRequest);
		assert page.getNumberOfElements() > 1 : "================== error when testing testFindByOpLoc ...";
		System.out.println("================== testAdvancedFind testing  ok ... totalDoc:" + page.getTotalElements());
		page = query.advancedFind(keyword, "410823", "C", RegCapLevel.LEVEL1, new String[]{"9600"}, pageRequest);
		assert page.getNumberOfElements() == 1 : "================== error when testing testFindByOpLoc ...";
		System.out.println("================== testAdvancedFind testing  ok ...");
	}
	
}
