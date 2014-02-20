package com.topsoft.search.icis.ecps;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.topsoft.search.HttpSolrServerCache;

/**
 * 测试国家信息公示平台企业查询接口
 * 
 * @author weichao
 * 
 */
public class NationalSolrEnterpriseQueryTest {
	
	private static final String baseURL = "http://192.168.3.40:9081/solr";
	
	private NationalSolrEnterpriseQuery query = new NationalSolrEnterpriseQuery(baseURL);

	public void testHighlight() {
		SolrQuery query = new SolrQuery("entName:" + "好运来");
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
			assert map.size() == 1 : "================== error when testing testHighlight ...";
			System.out.println("================== testHighlight testing  ok ...");
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void testFind() {
		List<NationalEntBaseInfo> list = query.find("410823620003885");
		assert list.size() == 1 : "================== error when testing testFind regNo ...";
		System.out.println("================== testFind regNo testing ok ...");
		list = query.find("好运来");
		assert list.size() > 1 : "================== error when testing testFind entName ...";
		System.out.println("================== testFind entName testing ok ...");
	}
}
