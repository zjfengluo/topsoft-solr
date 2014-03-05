package com.topsoft.search.support;

import com.topsoft.search.QueryPreProcessor;
import com.topsoft.search.ResultTransformer;
import com.topsoft.search.SolrQueryProfile;
import com.topsoft.search.SolrQueryProfileDecorator;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.domain.Pages;
import com.topsoft.search.domain.Sort;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
public class PagedSolrQueryProfile<T> extends SolrQueryProfileDecorator<List<T>, Page<T>> {
  private final Pageable pageRequest;

  public PagedSolrQueryProfile(ResultTransformer<List<T>> transformer, Pageable pageRequest) {
    this(null, transformer, pageRequest);
  }

  public PagedSolrQueryProfile(SolrQueryProfile<List<T>> profile, Pageable pageRequest) {
    super(profile);
    this.pageRequest = checkNotNull(pageRequest, "pageRequest must not be null!");
  }

  public PagedSolrQueryProfile(QueryPreProcessor preProcessor, ResultTransformer<List<T>> transformer, Pageable pageRequest) {
    super(preProcessor, transformer);
    this.pageRequest = checkNotNull(pageRequest, "pageRequest must not be null!");
  }

  @Override
  protected void selfPrepare(SolrQuery query) {
    query.setStart(pageRequest.getOffset());
    query.setRows(pageRequest.getPageSize());

    Sort sort = pageRequest.getSort();

    if (sort != null) {
      for (Sort.OrderBy orderBy : sort) {
        query.addSort(orderBy.getProperty(), orderBy.isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
      }
    }
  }

  @Override
  protected Page<T> selfTransform(QueryResponse response, List<T> prevResults) {
    SolrDocumentList docs = response.getResults();
    return Pages.of(prevResults, pageRequest, docs.getNumFound());
  }

}
