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
import com.topsoft.search.SolrMultiCoreQuery;
import com.topsoft.search.domain.HighlightWrapper;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.PageRequest;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.domain.Pages;
import com.topsoft.search.domain.Sort.Order;
import com.topsoft.search.support.HighlightParameters;
import com.topsoft.search.support.HighlightQueryPreProcessor;
import com.topsoft.search.support.HighlightWrapperResultTransformer;
import com.topsoft.search.support.PagedSolrQueryProfile;

/**
 * 拓普企业信用公示系统-企业信息查询
 * 
 * @author weichao
 *
 */
public class TopSolrEnterpriseQuery implements ITopSolrEnterpriseQuery, SolrCoreQueryRequest {
	
	private final PagableSolrMultiCoreQuery pageQuery;
	
	private final SolrMultiCoreQuery query;
	
	private static final Function<SolrDocument, TopEntBaseInfoBean> rowTransformer = new Function<SolrDocument, TopEntBaseInfoBean>() {

		@Override
		@Nullable
		public TopEntBaseInfoBean apply(@Nullable SolrDocument input) {
			Object entNameO = input.getFieldValue("entName");
			String entName = entNameO == null ? null : entNameO.toString();
			Object idO = input.getFieldValue("id");
			Long id = idO == null ? null : Long.valueOf(idO.toString());
			Object regNoO = input.getFieldValue("regNo");
			String regNo = regNoO == null ? null : regNoO.toString();
			Object leRepO = input.getFieldValue("leRep");
			String leRep = leRepO == null ? null : leRepO.toString();
			Object opLocOrDomO = input.getFieldValue("opLocOrDom");
			String opLocOrDom = opLocOrDomO == null ? null : opLocOrDomO.toString();
			Object regCapO = input.getFieldValue("regCap");
			Float regCap = regCapO == null ? null : (Float) regCapO;
			Object regCapCurNameO = input.getFieldValue("regCapCurName");
			String regCapCurName = regCapCurNameO == null ? null : regCapCurNameO.toString();
			Object industryPhyNameO = input.getFieldValue("industryPhyName");
			String industryPhyName = industryPhyNameO == null ? null : industryPhyNameO.toString();
			Object estDateO = input.getFieldValue("estDate");
			Date estDate = estDateO == null ? null : (Date) estDateO;
			TopEntBaseInfoBean bean = new TopEntBaseInfoBean(id, entName, regNo, leRep, opLocOrDom, regCap, regCapCurName, industryPhyName, estDate);
			return bean;
		}
		
	};
	
	private static final HighlightWrapperResultTransformer<TopEntBaseInfoBean> highligthTransformer = new HighlightWrapperResultTransformer<TopEntBaseInfoBean>("id", rowTransformer);

	public TopSolrEnterpriseQuery(final String baseURL) {
		super();
		this.pageQuery = new PagableSolrMultiCoreQuery(baseURL);
		this.query = new SolrMultiCoreQuery(baseURL);
	}

	@Override
	public Function<SolrDocument, TopEntBaseInfoBean> getRowTransformer() {
		return rowTransformer;
	}

	@Override
	public Page<TopEntBaseInfo> findByEntName(String keyword,
			int currentPage, int pageSize) {
		Pageable pageRequest = QueryProfile.getPageRequest(currentPage,
				pageSize);
		PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>> profile = new PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>>(
				QueryProfile.BYNAME.getPreProcessor(), highligthTransformer,
				pageRequest);
		Page<HighlightWrapper<TopEntBaseInfoBean>> page = pageQuery.findFrom(
				getCoreName(),
				QueryProfile.BYNAME.buildQueryStr(new String[] { keyword }),
				profile);
		return Pages.of(QueryProfile.BYNAME.injectHighlightInfo(page),
				pageRequest, page.getTotalElements());
	}

	@Override
	public List<TopEntBaseInfo> findByRegNo(String regNo) {
		List<TopEntBaseInfoBean> list = query.findAllFrom(getCoreName(), "regNo:" + regNo, getRowTransformer());
		return new ArrayList<TopEntBaseInfo>(list);
	}

	@Override
	public Page<TopEntBaseInfo> findByLeRep(String keyword, int currentPage, int pageSize) {
		Pageable pageRequest = QueryProfile.getPageRequest(currentPage,
				pageSize);
		PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>> profile = new PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>>(
				QueryProfile.BYLEREP.getPreProcessor(), highligthTransformer,
				pageRequest);
		Page<HighlightWrapper<TopEntBaseInfoBean>> page = pageQuery.findFrom(
				getCoreName(),
				QueryProfile.BYLEREP.buildQueryStr(new String[] { keyword }),
				profile);
		return Pages.of(QueryProfile.BYLEREP.injectHighlightInfo(page),
				pageRequest, page.getTotalElements());
	}

	@Override
	public Page<TopEntBaseInfo> findByOpLoc(String keyword, int currentPage, int pageSize) {
		Pageable pageRequest = QueryProfile.getPageRequest(currentPage,
				pageSize);
		PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>> profile = new PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>>(
				QueryProfile.BYOPLOC.getPreProcessor(), highligthTransformer,
				pageRequest);
		Page<HighlightWrapper<TopEntBaseInfoBean>> page = pageQuery.findFrom(
				getCoreName(),
				QueryProfile.BYOPLOC.buildQueryStr(new String[] { keyword }),
				profile);
		return Pages.of(QueryProfile.BYOPLOC.injectHighlightInfo(page),
				pageRequest, page.getTotalElements());
	}

	@Override
	public Page<TopEntBaseInfo> advancedFind(String keyword,
			String opLocDistrict, String industryPhy, RegCapLevel regCapLevel,
			String[] entTypes, int currentPage, int pageSize) {
		StringBuilder queryStr = new StringBuilder();
		if (keyword != null && !keyword.trim().isEmpty()) {
			keyword = keyword.trim();
			queryStr.append("+(regNo:" + keyword + " OR entName:" + keyword + " OR leRep:" + keyword +" OR domOrOpLoc:" + keyword +")");
		}
		if (opLocDistrict != null && !opLocDistrict.trim().isEmpty()) {
			queryStr.append(" +opLocDistrict:" + opLocDistrict);
		}
		if (industryPhy != null && !industryPhy.trim().isEmpty()) {
			queryStr.append(" +industryPhy:" + industryPhy);
		}
		if (regCapLevel != null) {
			queryStr.append(" +regCapLevel:" + regCapLevel.getLevel());
		}
		if (entTypes != null && entTypes.length > 0) {
			for(int i = 0, j = entTypes.length; i < j; i++) {
				if (i == 0) {
					queryStr.append(" +(entType:" + entTypes[i]);
				} else {
					queryStr.append(" OR entType:" + entTypes[i]);
				}
			}
			queryStr.append(")");
		}
		Pageable pageRequest = QueryProfile.getPageRequest(currentPage,
				pageSize);
		PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>> profile = new PagedSolrQueryProfile<HighlightWrapper<TopEntBaseInfoBean>>(
				QueryProfile.ADVANCED.getPreProcessor(), highligthTransformer,
				pageRequest);
		Page<HighlightWrapper<TopEntBaseInfoBean>> page = pageQuery.findFrom(
				getCoreName(), queryStr.toString(),	profile);
		return Pages.of(QueryProfile.ADVANCED.injectHighlightInfo(page),
				pageRequest, page.getTotalElements());
	}

	@Override
	public String getCoreName() {
		return coreName;
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
	
	private enum QueryProfile {
		
		BYNAME(new HighlightQueryPreProcessor(HighlightParameters.builder()
			      .setFields("entName")
			      .setFragsize(100)
			      .setSnippets(1)
			      .setRequireFieldMatch(true)
			      .setSimplePre("<span class=\"" + HIGHLIGHT_CSS_CLASSNAME + "\">")
			      .setSimplePost("</span>")
			      .build()),
			   new Function<Page<HighlightWrapper<TopEntBaseInfoBean>>, List<TopEntBaseInfo>>() {
			
					@Override
					@Nullable
					public List<TopEntBaseInfo> apply(
							@Nullable Page<HighlightWrapper<TopEntBaseInfoBean>> input) {
						List<TopEntBaseInfo> list = new ArrayList<TopEntBaseInfo>();
					    for (HighlightWrapper<TopEntBaseInfoBean> info : input) {
					    	TopEntBaseInfoBean bean = info.getBean();
					    	bean.setEntName(info.getHighlights().get("entName"));
					    	list.add(bean);
					    }
					    return list;
					}		
					
				},
				new Function<String[], String> () {

					@Override
					@Nullable
					public String apply(@Nullable String[] input) {
						return "entName:" + input[0];
					}
					
				}
		),
		BYLEREP(new HighlightQueryPreProcessor(HighlightParameters.builder()
			      .setFields("leRep")
			      .setFragsize(100)
			      .setSnippets(1)
			      .setRequireFieldMatch(true)
			      .setSimplePre("<span class=\"" + HIGHLIGHT_CSS_CLASSNAME + "\">")
			      .setSimplePost("</span>")
			      .build()),
			   new Function<Page<HighlightWrapper<TopEntBaseInfoBean>>, List<TopEntBaseInfo>>() {
			
					@Override
					@Nullable
					public List<TopEntBaseInfo> apply(
							@Nullable Page<HighlightWrapper<TopEntBaseInfoBean>> input) {
						List<TopEntBaseInfo> list = new ArrayList<TopEntBaseInfo>();
					    for (HighlightWrapper<TopEntBaseInfoBean> info : input) {
					    	TopEntBaseInfoBean bean = info.getBean();
					    	bean.setLeRep(info.getHighlights().get("leRep"));
					    	list.add(bean);
					    }
					    return list;
					}		
					
				},
				new Function<String[], String> () {

					@Override
					@Nullable
					public String apply(@Nullable String[] input) {
						return "leRep:" + input[0];
					}
					
				}
		),
		BYOPLOC(new HighlightQueryPreProcessor(HighlightParameters.builder()
			      .setFields("domOrOpLoc")
			      .setFragsize(100)
			      .setSnippets(1)
			      .setRequireFieldMatch(true)
			      .setSimplePre("<span class=\"" + HIGHLIGHT_CSS_CLASSNAME + "\">")
			      .setSimplePost("</span>")
			      .build()),
			   new Function<Page<HighlightWrapper<TopEntBaseInfoBean>>, List<TopEntBaseInfo>>() {
			
					@Override
					@Nullable
					public List<TopEntBaseInfo> apply(
							@Nullable Page<HighlightWrapper<TopEntBaseInfoBean>> input) {
						List<TopEntBaseInfo> list = new ArrayList<TopEntBaseInfo>();
					    for (HighlightWrapper<TopEntBaseInfoBean> info : input) {
					    	TopEntBaseInfoBean bean = info.getBean();
					    	bean.setOpLocOrDom(info.getHighlights().get("domOrOpLoc"));
					    	list.add(bean);
					    }
					    return list;
					}		
					
				},
				new Function<String[], String> () {

					@Override
					@Nullable
					public String apply(@Nullable String[] input) {
						return "domOrOpLoc:" + input[0];
					}
					
				}
		),
		ADVANCED(new HighlightQueryPreProcessor(HighlightParameters.builder()
			      .setFields("entName", "leRep", "domOrOpLoc")
			      .setFragsize(100)
			      .setSnippets(1)
			      .setRequireFieldMatch(true)
			      .setSimplePre("<span class=\"" + HIGHLIGHT_CSS_CLASSNAME + "\">")
			      .setSimplePost("</span>")
			      .build()),
			   new Function<Page<HighlightWrapper<TopEntBaseInfoBean>>, List<TopEntBaseInfo>>() {
			
					@Override
					@Nullable
					public List<TopEntBaseInfo> apply(
							@Nullable Page<HighlightWrapper<TopEntBaseInfoBean>> input) {
						List<TopEntBaseInfo> list = new ArrayList<TopEntBaseInfo>();
					    for (HighlightWrapper<TopEntBaseInfoBean> info : input) {
					    	TopEntBaseInfoBean bean = info.getBean();
					    	bean.setEntName(info.getHighlights().get("entName"));
					    	bean.setLeRep(info.getHighlights().get("leRep"));
					    	bean.setOpLocOrDom(info.getHighlights().get("domOrOpLoc"));
					    	list.add(bean);
					    }
					    return list;
					}		
					
				},
				null
		);
		
		private HighlightQueryPreProcessor highlightPreProcessor;
		
		private Function<Page<HighlightWrapper<TopEntBaseInfoBean>>, List<TopEntBaseInfo>> highlightInjector;
		
		private Function<String[], String> queryStrBuilder;

		public HighlightQueryPreProcessor getPreProcessor() {
			return highlightPreProcessor;
		}
		
		public List<TopEntBaseInfo> injectHighlightInfo(Page<HighlightWrapper<TopEntBaseInfoBean>> page) {
			return highlightInjector.apply(page);
		}
		
		public String buildQueryStr(String[] input) {
			return queryStrBuilder.apply(input);
		}		

		static Pageable getPageRequest(int currentPage, int pageSize) {
			return PageRequest.builder(currentPage, pageSize).orderBy("entNameLength", Order.asc).orderBy("estDate", Order.desc).build();
		}

		private QueryProfile(
				HighlightQueryPreProcessor highlightPreProcessor, Function<Page<HighlightWrapper<TopEntBaseInfoBean>>, List<TopEntBaseInfo>> highlightInjector, Function<String[], String> queryStrBuilder) {
			this.highlightPreProcessor = highlightPreProcessor;
			this.highlightInjector = highlightInjector;
			this.queryStrBuilder = queryStrBuilder;
		}
		
	}
	
}
