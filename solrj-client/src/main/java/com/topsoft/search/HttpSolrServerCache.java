package com.topsoft.search;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.ForwardingLoadingCache;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.*;

/**
 * <p>用于缓存 {@link HttpSolrServer} 实例的工具类
 * <p>主要目的是为了避免频繁的重复创建 {@link HttpSolrServer} 实例，可有效提高性能。
 * <p>缓存的key是server的url，缓存实例默认闲置一分钟失效。
 *
 * @author wangyg
 */
public class HttpSolrServerCache extends ForwardingLoadingCache<String, HttpSolrServer> {

  private static final RemovalListener<String, HttpSolrServer> REMOVAL_LISTENER = new RemovalListener<String, HttpSolrServer>() {
    @Override
    public void onRemoval(RemovalNotification<String, HttpSolrServer> notification) {
      HttpSolrServer server = notification.getValue();
      if (server != null) {
        server.shutdown();
      }
    }
  };
  private static HttpSolrServerCache instance;
  private final LoadingCache<String, HttpSolrServer> cache;
  private CacheLoader<String, HttpSolrServer> loader = new CacheLoader<String, HttpSolrServer>() {
    @Override
    public HttpSolrServer load(String key) throws Exception {
      return new HttpSolrServer(normalizeKey(key));
    }

    private String normalizeKey(String key) {
     checkArgument(key != null && key.length() > 0, "key must not be null or empty.");
      return key.trim().toLowerCase();
    }
  };

  private HttpSolrServerCache(long timeout, TimeUnit timeoutTimeUnit) {
    cache = CacheBuilder.newBuilder()
        .expireAfterAccess(timeout, timeoutTimeUnit) // default idle time: 1 minute
        .removalListener(REMOVAL_LISTENER)
        .build(loader);
  }

  public static final HttpSolrServerCache getInstance() {
    return getInstance(60, TimeUnit.SECONDS);
  }

  public static final HttpSolrServerCache getInstance(long timeout, TimeUnit timeoutTimeUnit) {
    checkArgument(timeout > 0, "timeout must greater than zero!");
    checkNotNull(timeoutTimeUnit, "timeoutTimeUnit must not be null!");

    if (instance == null) {
      synchronized (HttpSolrServerCache.class) {
        if (instance == null) {
          instance = new HttpSolrServerCache(timeout, timeoutTimeUnit);
        }
      }
    }

    return instance;
  }

  @Override
  protected LoadingCache<String, HttpSolrServer> delegate() {
    return cache;
  }

}
