package com.topsoft.search;

import java.util.Map;

/**
 * @author wangyg
 */
public interface SolrCoreQueryRequest {
  String getCoreName();

  String buildQueryString(Map<String, String> parameters);
}
