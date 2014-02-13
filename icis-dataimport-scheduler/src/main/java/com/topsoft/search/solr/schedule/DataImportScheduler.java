package com.topsoft.search.solr.schedule;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author wangyg
 */
public interface DataImportScheduler extends Scheduler {
  List<ScheduledFuture<?>> schedule(DataImportConfig config);
}
