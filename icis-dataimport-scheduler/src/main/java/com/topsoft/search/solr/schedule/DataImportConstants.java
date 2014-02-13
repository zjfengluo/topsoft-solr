package com.topsoft.search.solr.schedule;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author wangyg
 */
public class DataImportConstants {
  public static final String TRIGGERS_TAG = "triggers";
  public static final String TRIGGER_TAG = "trigger";
  public static final String SUSPENSIONS_TAG = "trigger-suspensions";
  public static final String SUSPENSION_TAG = "trigger-suspension";
  public static final String REQUESTS_TAG = "requests";
  public static final String REQUEST_TAG = "request";
  public static final String SERVERS_TAG = "servers";
  public static final String SERVER_TAG = "server";
  public static final String CORE_TAG = "core";
  public static final String START_TIME_TAG = "start-time";
  public static final String REPEAT_TAG = "repeat";
  public static final String TIME_RANGE_TAG = "time-range";
  public static final String MONTH_DAY_TAG = "month-day";
  public static final String MONTH_WEEKDAY_TAG = "month-weekday";
  public static final String WEEKDAY_TAG = "weekday";
  public static final String PARAMETER_TAG = "parameter";
  public static final String SCHEDULER_TAG = "scheduler";
  public static final String NAME_ATTR = "name";
  public static final String TIME_UNIT_ATTR = "time-unit";
  public static final String FROM_ATTR = "from";
  public static final String TO_ATTR = "to";
  public static final String MONTH_ATTR = "month";
  public static final String WEEK_ATTR = "week";
  public static final String TYPE_ATTR = "type";
  public static final String HOST_ATTR = "host";
  public static final String CONTEXT_ATTR = "context";
  public static final String PORT_ATTR = "port";
  public static final String PATH_ATTR = "dataimport-path";
  public static final String TRIGGER_ATTR = "trigger";
  public static final String SUSPEND_WITH_ATTR = "suspend-with";
  public static final String REQUEST_ATTR = "request";
  public static final String DEFAULT_DATAIMPORT_PATH = "/dataimport";
  public static final DateTimeFormatter TIME_PATTERN = DateTimeFormat.forPattern("HH:mm:ss");
  public static final String DEFAULT_CONF_FILE = "dataimport.xml";
}
