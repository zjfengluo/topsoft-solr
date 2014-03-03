package com.topsoft.search.icis.ecps;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.PageRequest;
import com.topsoft.search.domain.Pageable;

/**
 * 拓普企业信用信息公示系统企业查询接口 - 单元测试
 * 
 * @author weichao
 * 
 */
@RunWith(JUnit4.class)
public class TopSolrEnterpriseQueryTest {

	private Pageable pageRequest = PageRequest.builder(1, 10).build();

	private TopSolrEnterpriseQuery query = new TopSolrEnterpriseQuery(
			NationalSolrEnterpriseQueryTest.baseURL);

	@BeforeClass
	public static void prepare() throws SolrServerException, IOException {
		NationalSolrEnterpriseQueryTest.addDoc();
	}
	
	@AfterClass
	public static void cleanup() throws SolrServerException, IOException {
		NationalSolrEnterpriseQueryTest.deleteDoc();
	}

	/**
	 * 测试根据企业名称进行模糊查询
	 */
	@Test
	public void testFindByEntName() {
		Page<TopEntBaseInfo> page = query.findByEntName(
				NationalSolrEnterpriseQueryTest.keyword, pageRequest);
		assertTrue(page.getNumberOfElements() > 0);
	}

	/**
	 * 测试根据注册号进行查询
	 */
	@Test
	public void testFindByRegNo() {
		List<TopEntBaseInfo> page = query.findByRegNo(NationalSolrEnterpriseQueryTest.regNo);
		assertTrue(page.size() == 1);
	}

	/**
	 * 测试根据法定代表人进行模糊查询
	 */
	@Test
	public void testFindByLeRep() {
		Page<TopEntBaseInfo> page = query.findByLeRep(
				NationalSolrEnterpriseQueryTest.keyword, pageRequest);
		assertTrue(page.getNumberOfElements() > 0);
	}

	/**
	 * 测试根据经营地址进行模糊查询
	 */
	@Test
	public void testFindByOpLoc() {
		Page<TopEntBaseInfo> page = query.findByOpLoc(
				NationalSolrEnterpriseQueryTest.keyword, pageRequest);
		assertTrue(page.getNumberOfElements() > 0);
	}

	/**
	 * 测试高级查询
	 */
	@Test
	public void testAdvancedFind() {
		Page<TopEntBaseInfo> page = query.advancedFind(
				NationalSolrEnterpriseQueryTest.keyword, null, null, null,
				null, pageRequest);
		assertTrue(page.getNumberOfElements() >= 1);
		page = query.advancedFind(NationalSolrEnterpriseQueryTest.keyword,
				NationalSolrEnterpriseQueryTest.opLocDistrict,
				NationalSolrEnterpriseQueryTest.industryPhy,
				RegCapLevel.LEVEL1,
				new String[] { NationalSolrEnterpriseQueryTest.entType },
				pageRequest);
		assertTrue(page.getNumberOfElements() >= 1);
	}

}
