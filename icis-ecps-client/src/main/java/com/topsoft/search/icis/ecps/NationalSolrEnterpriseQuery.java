package com.topsoft.search.icis.ecps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.topsoft.search.HttpSolrServerCache;
import com.topsoft.search.SolrCoreQueryRequest;
import com.topsoft.search.SolrMultiCoreQuery;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.PageRequest;
import com.topsoft.search.domain.Pageable;

/**
 * 全国企业信用信息公示系统-企业信息查询
 * 
 * TODO 关键字高亮展示
 * 
 * @author weichao
 *
 */
public class NationalSolrEnterpriseQuery implements INationalSolrEnterpriseQuery, SolrCoreQueryRequest{
	
	private final SolrMultiCoreQuery query;
	
	private static final RowTransformer transformer = new RowTransformer();
	
	public NationalSolrEnterpriseQuery(final String baseURL) {
		this.query = new SolrMultiCoreQuery(baseURL);
	}

	@Override
	public List<NationalEntBaseInfo> find(String keyword) {
		Pageable pageRequest = PageRequest.builder(PageRequest.DEFAULT_PAGE, 5).build();
		Page<NationalEntBaseInfo> page= (Page<NationalEntBaseInfo>) query.findFrom(getCoreName(), buildQueryString(keyword), pageRequest, getRowTransformer());
		List<NationalEntBaseInfo> list = new ArrayList<NationalEntBaseInfo>();
		if (page.getNumberOfElements() > 0) {
			for(NationalEntBaseInfo info : page) {
				list.add(info);
			}
		}
		return list;
	}
	
	private String buildQueryString(String keyword) {
		return "regNo:" + keyword + " OR entName:" + keyword;
	}

	@Override
	public String getCoreName() {
		return coreName;
	}
	
	@Override
	public Function<SolrDocument, NationalEntBaseInfo> getRowTransformer() {
		return transformer;
	}

	@Override
	public String buildQueryString(Map<String, String> parameters) {
		StringBuilder builder = new StringBuilder();

		Set<Map.Entry<String, String>> entries = parameters.entrySet();
		for (Map.Entry<String, String> entry : entries) {
			if (builder.length() > 0)
				builder.append(" AND ");
			builder.append(entry.getKey()).append(":").append(entry.getValue());
		}

		return builder.toString();
	}
	
	//临时测试一下，查看高亮信息部分的数据结构
	public static void main(String[] we) {
		//NationalSolrEnterpriseQuery q  = new NationalSolrEnterpriseQuery("http://192.168.3.40:9081/solr");
		SolrQuery query = new SolrQuery("entName:" + "好运来");
	    query.setStart(0);
	    query.setRows(5);
	    query.setHighlight(true);
	    query.set("hl.fl", "entName");
	    query.setHighlightSnippets(3);
	    

	    HttpSolrServer core = HttpSolrServerCache.getInstance().getUnchecked("http://192.168.3.40:9081/solr" + "/" + coreName);
	    try {
	    	QueryResponse res = core.query(query);
	      SolrDocumentList solrDocuments = res.getResults();
	      SolrDocument sd = solrDocuments.iterator().next();
	      String id = sd.getFieldValue("id").toString();
	      System.out.println("--------- entName: 【" + sd.getFieldValue("entName") + "】 id:" + id);
	      Object estDateO = sd.getFieldValue("estDate");
			Date estDate = estDateO == null ? null : (Date) estDateO;
			Object regCapO = sd.getFieldValue("regCap");
			Float regCap = regCapO == null ? null : (Float) regCapO;
			System.out.println(estDate.toString() +" ** "+ regCap);
	      Map<String, List<String>> map = res.getHighlighting().get(id);
	      for (String s : map.get("entName")) {
	    	  System.out.println(s);
	      }
	      Lists.transform(solrDocuments, transformer);

	    } catch (SolrServerException e) {
	      throw new RuntimeException(e);
	    }
	}
	
	private static class RowTransformer implements Function<SolrDocument,NationalEntBaseInfo>{

		@Override
		@Nullable
		public NationalEntBaseInfo apply(@Nullable SolrDocument input) {
			Object entNameO = input.getFieldValue("entName");
			String entName = entNameO == null ? null : entNameO.toString();
			Object idO = input.getFieldValue("id");
			Long id = idO == null ? null : Long.valueOf(idO.toString());
			Object regNoO = input.getFieldValue("regNo");
			String regNo = regNoO == null ? null : regNoO.toString();
			Object leRepO = input.getFieldValue("leRep");
			String leRep = leRepO == null ? null : leRepO.toString();
			Object regOrgNameO = input.getFieldValue("regOrgName");
			String regOrgName = regOrgNameO == null ? null : regOrgNameO.toString();
			Object estDateO = input.getFieldValue("estDate");
			Date estDate = estDateO == null ? null : (Date) estDateO;
			NationalEntBaseInfoBean bean = new NationalEntBaseInfoBean(id,entName,regNo,leRep,regOrgName,estDate);
			return bean;
		}
		
	}
	
	private static class NationalEntBaseInfoBean implements NationalEntBaseInfo {
		
		private NationalEntBaseInfoBean(Long id, String entName, String regNo,
				String leRep, String regOrgName, Date estDate) {
			super();
			this.id = id;
			this.entName = entName;
			this.regNo = regNo;
			this.leRep = leRep;
			this.regOrgName = regOrgName;
			this.estDate = estDate;
		}

		private Long id;
		
		private String entName;
		
		private String regNo;
		
		private String leRep;
		
		private String regOrgName;
		
		private Date estDate;

		public Long getId() {
			return id;
		}

		public String getEntName() {
			return entName;
		}

		public String getRegNo() {
			return regNo;
		}

		public String getLeRep() {
			return leRep;
		}

		public String getRegOrgName() {
			return regOrgName;
		}

		public Date getEstDate() {
			return estDate;
		}		
	}
	
}
