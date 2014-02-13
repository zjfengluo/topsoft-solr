package com.topsoft.search.solr.schedule;

import com.google.common.base.Strings;
import com.topsoft.search.HttpSolrServerCache;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.Map;

import static com.google.common.base.Preconditions.*;
import static com.topsoft.search.solr.schedule.DataImportConstants.DEFAULT_DATAIMPORT_PATH;

/**
 * @author wangyg
 */
public class SolrjDataImportAction implements Runnable {
  private final String baseUrl;
  private final String dataImportPath;
  private final Map<String, String> parameterMap;

  public SolrjDataImportAction(String baseUrl, String dataImportPath, Map<String, String> parameterMap) {
    this.baseUrl = checkNotNull(baseUrl);
    this.dataImportPath = Strings.isNullOrEmpty(dataImportPath) ?
        DEFAULT_DATAIMPORT_PATH : dataImportPath;
    this.parameterMap = checkNotNull(parameterMap);
  }

  @Override
  public void run() {

    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set(CommonParams.QT, dataImportPath);

    for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
      params.set(entry.getKey(), entry.getValue());
    }

    HttpSolrServerCache cache = HttpSolrServerCache.getInstance();
    HttpSolrServer solrServer = cache.getUnchecked(baseUrl);
    try {
      solrServer.query(params);
    } catch (SolrServerException e) {
      throw new DataImportException(e);
    }
  }
}
