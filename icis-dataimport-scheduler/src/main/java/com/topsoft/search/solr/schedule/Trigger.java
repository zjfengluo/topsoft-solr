package com.topsoft.search.solr.schedule;

/**
 * @author wangyg
 */
public interface Trigger {
  String getName();

  long getInitDelayMillis();

  long getRepeatMillis();

  boolean shouldSuspendNow();
}
