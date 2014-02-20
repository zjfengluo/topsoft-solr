package com.topsoft.search.icis.ecps;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.solr.common.SolrDocument;

import com.google.common.base.Function;
import com.topsoft.search.SolrCoreQueryRequest;
import com.topsoft.search.SolrMultiCoreQuery;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.Pageable;

/**
 * 拓普企业信用公示系统-企业信息查询
 * 
 * TODO 高亮处理
 * @author weichao
 *
 */
public class TopSolrEnterpriseQuery implements ITopSolrEnterpriseQuery, SolrCoreQueryRequest {
	
	private static final RowTransformer transformer = new RowTransformer();

	private final SolrMultiCoreQuery query;
	
	public TopSolrEnterpriseQuery(final String baseURL) {
		super();
		this.query = new SolrMultiCoreQuery(baseURL);
	}

	@Override
	public Function<SolrDocument, TopEntBaseInfo> getRowTransformer() {
		return transformer;
	}

	@Override
	public Page<TopEntBaseInfo> findByEntName(String keyword,
			Pageable pageRequest) {
		return query.findFrom(getCoreName(), "entName:" + keyword, pageRequest, getRowTransformer());
	}

	@Override
	public List<TopEntBaseInfo> findByRegNo(String regNo) {
		return query.findAllFrom(getCoreName(), "regNo:" + regNo, getRowTransformer());
	}

	@Override
	public Page<TopEntBaseInfo> findByLeRep(String keyword, Pageable pageRequest) {
		return query.findFrom(getCoreName(), "leRep:" + keyword, pageRequest, getRowTransformer());
	}

	@Override
	public Page<TopEntBaseInfo> findByOpLoc(String keyword, Pageable pageRequest) {
		return query.findFrom(getCoreName(), "domOrOpLoc:" + keyword, pageRequest, getRowTransformer());
	}

	@Override
	public Page<TopEntBaseInfo> advancedFind(String keyword,
			String opLocDistrict, String industryPhy, RegCapLevel regCapLevel,
			String[] entTypes, Pageable pageRequest) {
		StringBuilder queryStr = new StringBuilder();
		if (keyword != null && !keyword.trim().isEmpty()) {
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
		return query.findFrom(getCoreName(), queryStr.toString(), pageRequest, getRowTransformer());
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
	
	private static class RowTransformer implements Function<SolrDocument, TopEntBaseInfo> {

		@Override
		@Nullable
		public TopEntBaseInfo apply(@Nullable SolrDocument input) {
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
		
	}

	private static class TopEntBaseInfoBean implements TopEntBaseInfo {

		private Long id;
		
		private String entName;

		private String regNo;
		
		private String leRep;
		
		private String opLocOrDom;
		
		private Float regCap;
		
		private String regCapCurName;
		
		private String industryPhyName;
		
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

		public String getOpLocOrDom() {
			return opLocOrDom;
		}

		public Float getRegCap() {
			return regCap;
		}

		public String getRegCapCurName() {
			return regCapCurName;
		}

		public String getIndustryPhyName() {
			return industryPhyName;
		}

		public Date getEstDate() {
			return estDate;
		}

		private TopEntBaseInfoBean(Long id, String entName, String regNo,
				String leRep, String opLocOrDom, Float regCap,
				String regCapCurName, String industryPhyName, Date estDate) {
			super();
			this.id = id;
			this.entName = entName;
			this.regNo = regNo;
			this.leRep = leRep;
			this.opLocOrDom = opLocOrDom;
			this.regCap = regCap;
			this.regCapCurName = regCapCurName;
			this.industryPhyName = industryPhyName;
			this.estDate = estDate;
		}
		
	}
	
}
