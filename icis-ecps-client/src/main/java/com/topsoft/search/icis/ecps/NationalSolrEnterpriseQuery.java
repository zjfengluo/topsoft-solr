package com.topsoft.search.icis.ecps;

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
import org.apache.solr.common.SolrDocument;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 全国企业信用信息公示系统-企业信息查询
 *
 * @author weichao
 */
public class NationalSolrEnterpriseQuery implements INationalSolrEnterpriseQuery, SolrCoreQueryRequest {

  private final PagableSolrMultiCoreQuery query;

  private static final Function<SolrDocument, NationalEntBaseInfoBean> rowTransformer = new Function<SolrDocument, NationalEntBaseInfoBean>() {

    @Override
    @Nullable
    public NationalEntBaseInfoBean apply(@Nullable SolrDocument input) {
      String entName = stringify(input.getFieldValue("entName"));
      Long id = longify(input.getFieldValue("id"));
      String regNo = stringify(input.getFieldValue("regNo"));
      String leRep = stringify(input.getFieldValue("leRep"));
      String regOrgName = stringify(input.getFieldValue("regOrgName"));
      Date estDate = dateify(input.getFieldValue("estDate"));
      String entType = stringify(input.getFieldValue("entType"));
      String uuid = stringify(input.getFieldValue("uuid"));

      NationalEntBaseInfoBean bean = new NationalEntBaseInfoBean(id, entName, regNo, leRep, regOrgName, estDate, entType, uuid);
      return bean;
    }

    private String stringify(Object value) {
      return value == null ? null : value.toString();
    }

    private Long longify(Object value) {
      return value == null ? null : Long.valueOf(value.toString());
    }

    private Date dateify(Object value) {
      return value == null ? null : (Date) value;
    }

  };

  // 设置高亮参数
  private static final HighlightQueryPreProcessor preProcessor = new HighlightQueryPreProcessor(HighlightParameters.builder()
      .setFields("entName", "regNo")
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
      for (HighlightWrapper<NationalEntBaseInfoBean> info : page) {
        NationalEntBaseInfoBean bean = info.getBean();
        Map<String, String> highlights = info.getHighlights();

        if (highlights.containsKey("entName")) {
          bean.setEntName(highlights.get("entName"));
        }

        if (highlights.containsKey("regNo")){
          bean.setRegNo(highlights.get("regNo"));
        }

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
