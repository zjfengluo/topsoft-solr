package com.topsoft.search.icis.ecps;

import org.apache.solr.common.SolrDocument;

import com.google.common.base.Function;

/**
 * 企业信息查询接口
 * 
 * @author weichao
 *
 */
public interface ISolrEnterpriseQuery<T> {
	
	static final String HIGHLIGHT_CSS_CLASSNAME="ecps_solr_highlight";
	
	/**
	 * 获取转换器，用来把SolrDocument转为业务对象
	 * 
	 * @return
	 */
	Function<SolrDocument,T> getRowTransformer();

}
