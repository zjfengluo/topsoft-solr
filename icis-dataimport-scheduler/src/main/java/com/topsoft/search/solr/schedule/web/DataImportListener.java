package com.topsoft.search.solr.schedule.web;

import com.topsoft.search.solr.schedule.DataImportConfig;
import com.topsoft.search.solr.schedule.DataImportConstants;
import com.topsoft.search.solr.schedule.DataImportException;
import com.topsoft.search.solr.schedule.DataImportScheduler;
import com.topsoft.search.solr.schedule.Schedulers;
import com.topsoft.search.solr.schedule.SolrHomeLocator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * <p>控制<tt>solr</tt>索引同步的<tt>ServletContextListener</tt>，servlet容器启动时开始，容器关闭后停止。
 * <p>将如下片段加入<code>web.xml</code>
 * <pre>
 * &lt;!-- 自定义dataimport.xml的路径，可不配置此参数，默认会自动寻找dataimport.xml --&gt;
 * &lt;context-param&gt;
 *   &lt;param-name&gt;dataimportConfigFilePath&lt;/param-name&gt;
 *   &lt;param-value&gt;/usr/local/solr&lt;/param-value&gt;
 * &lt;/context-param&gt;
 *
 * &lt;listener&gt;
 *   &lt;listener-class&gt;com.topsoft.search.solr.schedule.web.DataImportListener&lt;/listener-class&gt;
 * &lt;/listener&gt;
 * </pre>
 *
 * @author wangyg
 */
public class DataImportListener implements ServletContextListener {
  public static final String DIH_SCHEDULER = "__dataimport_scheduler";
  public static final String DATAIMPORT_CONFIG_FILE_PATH = "dataimportConfigFilePath";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext context = servletContextEvent.getServletContext();

    String configFilePath = context.getInitParameter(DATAIMPORT_CONFIG_FILE_PATH);
    SolrHomeLocator locator = new SolrHomeLocator(configFilePath);

    File configFile = new File(locator.getConfigDir(), DataImportConstants.DEFAULT_CONF_FILE);
    try {
      final DataImportConfig dataImportConfig = new DataImportConfig(new FileInputStream(configFile));
      final DataImportScheduler scheduler = Schedulers.newDataImportScheduler();

      scheduler.schedule(dataImportConfig);

      context.setAttribute(DIH_SCHEDULER, scheduler);

    } catch (IOException e) {
      throw new DataImportException(e);
    }

  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ServletContext context = servletContextEvent.getServletContext();

    DataImportScheduler scheduler = (DataImportScheduler) context.getAttribute(DIH_SCHEDULER);

    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
      try {
        scheduler.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
      }
    }
    // clean up servlet context
    context.removeAttribute(DIH_SCHEDULER);
  }
}
