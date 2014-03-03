package com.topsoft.search.icis.ecps;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.topsoft.search.HttpSolrServerCache;

/**
 * 测试国家信息公示平台企业查询接口
 * 
 * @author weichao
 * 
 */
@RunWith(JUnit4.class)
public class NationalSolrEnterpriseQueryTest {
	
	public static final String baseURL = "http://192.168.3.40:8983/solr";
	
	private NationalSolrEnterpriseQuery query = new NationalSolrEnterpriseQuery(baseURL);
	
	public static final String keyword = "好运来";
	
	public static final String regNo = "9999999";
	
	public static final String opLocDistrict = "410000";
	
	public static final String entType = "9600";
	
	public static final String industryPhy = "C";

	@Test
	public void testHighlight() {
		SolrQuery query = new SolrQuery("entName:" + keyword);
		query.setStart(0);
		query.setRows(5);
		query.setHighlight(true);
		query.set("hl.fl", "entName");
		query.setHighlightSnippets(3);

		HttpSolrServer core = HttpSolrServerCache.getInstance().getUnchecked(
				baseURL + "/entbaseinfo");
		try {
			QueryResponse res = core.query(query);
			SolrDocumentList solrDocuments = res.getResults();
			SolrDocument sd = solrDocuments.iterator().next();
			String id = sd.getFieldValue("id").toString();
			Map<String, List<String>> map = res.getHighlighting().get(id);
			assertTrue(map.size() == 1);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testFind() {
		List<NationalEntBaseInfo> list = query.find(regNo);
		assertTrue(list.size() == 1);
		list = query.find(keyword);
		assertTrue(list.size() >= 1);
	}
	
	@BeforeClass	
	public static void prepare() throws SolrServerException, IOException {
		addDoc();
	}
	
	@AfterClass
	public static void cleanup() throws SolrServerException, IOException {
		deleteDoc();
	}
	
	public static void addDoc() throws SolrServerException, IOException {
		SolrServer solr = new HttpSolrServer(baseURL+"/entbaseinfo");
		SolrInputDocument document = new SolrInputDocument();
		document.addField("id", "5521991");
		document.addField("entName", keyword);
		document.addField("regCap", "9.99");
		document.addField("regCapLevel", "1");
		document.addField("domOrOpLoc", keyword);
		document.addField("leRep", keyword);
		document.addField("regNo", regNo);
		document.addField("opLocDistrict", opLocDistrict);
		document.addField("entType", entType);
		document.addField("industryPhy", industryPhy);
		solr.add(document);
		solr.commit();
	}
	
	public static void deleteDoc() throws SolrServerException, IOException {
		SolrServer solr = new HttpSolrServer(baseURL+"/entbaseinfo");
		solr.deleteById("5521991");
		solr.commit();
	}
}
