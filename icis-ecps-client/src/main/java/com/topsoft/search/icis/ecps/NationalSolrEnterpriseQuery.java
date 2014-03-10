package com.topsoft.search.icis.ecps;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.solr.common.SolrDocument;

import com.google.common.base.Function;
import com.topsoft.search.PagableSolrMultiCoreQuery;
import com.topsoft.search.SolrCoreQueryRequest;
import com.topsoft.search.domain.HighlightWrapper;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.PageRequest;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.domain.Sort.Order;
import com.topsoft.search.support.HighlightParameters;
import com.topsoft.search.support.HighlightQueryPreProcessor;
import com.topsoft.search.support.HighlightWrapperResultTransformer;
import com.topsoft.search.support.PagedSolrQueryProfile;

/**
 * 全国企业信用信息公示系统-企业信息查询
 * 
 * @author weichao
 *
 */
public class NationalSolrEnterpriseQuery implements INationalSolrEnterpriseQuery, SolrCoreQueryRequest{
	
	private final PagableSolrMultiCoreQuery query;
	
	private static final Function<SolrDocument,NationalEntBaseInfoBean> rowTransformer = new Function<SolrDocument,NationalEntBaseInfoBean>() {

		@Override
		@Nullable
		public NationalEntBaseInfoBean apply(@Nullable SolrDocument input) {
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
			Object entTypeO = input.getFieldValue("entType");
			String entType = entTypeO == null ? null : entTypeO.toString();
			NationalEntBaseInfoBean bean = new NationalEntBaseInfoBean(id, entName, regNo, leRep, regOrgName, estDate, entType);
			return bean;
		}
		
	};
	
	// 设置高亮参数
    private static final HighlightQueryPreProcessor preProcessor = new HighlightQueryPreProcessor(HighlightParameters.builder()
		      .setFields("entName")
		      .setFragsize(100)
		      .setSnippets(1)
		      .setRequireFieldMatch(true)
		      .setSimplePre("<span class=\"" + HIGHLIGHT_CSS_CLASSNAME + "\">")
		      .setSimplePost("</span>")
		      .build());
    
    // 根据highlight信息对结果集自动封装为HighlightWrapper<T>对象
    private static final HighlightWrapperResultTransformer<NationalEntBaseInfoBean> Htransformer = new HighlightWrapperResultTransformer<NationalEntBaseInfoBean>("id", rowTransformer);
	
	public NationalSolrEnterpriseQuery(final String baseURL) {
		this.query = new PagableSolrMultiCoreQuery(baseURL);
	}

	@Override
	public List<NationalEntBaseInfo> find(String keyword) {
		Pageable pageRequest = PageRequest.builder(PageRequest.DEFAULT_PAGE, 5).orderBy("entNameLength", Order.asc).orderBy("estDate", Order.desc).build();
	    // 分页查询
	    PagedSolrQueryProfile<HighlightWrapper<NationalEntBaseInfoBean>> profile = new PagedSolrQueryProfile<HighlightWrapper<NationalEntBaseInfoBean>>(preProcessor, Htransformer, pageRequest);
	    Page<HighlightWrapper<NationalEntBaseInfoBean>> page = query.findFrom(getCoreName(), buildQueryString(keyword), profile);
		List<NationalEntBaseInfo> list = new ArrayList<NationalEntBaseInfo>();
		if (page.getNumberOfElements() > 0) {
			for(HighlightWrapper<NationalEntBaseInfoBean> info : page) {
				NationalEntBaseInfoBean bean = info.getBean();
				bean.setEntName(info.getHighlights().get("entName"));
				list.add(bean);
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
	public Function<SolrDocument, NationalEntBaseInfoBean> getRowTransformer() {
		return rowTransformer;
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
	
}
