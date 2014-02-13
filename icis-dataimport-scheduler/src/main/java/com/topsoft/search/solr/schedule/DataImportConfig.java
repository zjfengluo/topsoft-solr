package com.topsoft.search.solr.schedule;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Constraint;
import com.google.common.collect.Constraints;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapConstraint;
import com.google.common.collect.MapConstraints;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.topsoft.search.solr.schedule.DataImportConstants.*;

/**
 * @author wangyg
 */
public class DataImportConfig {

  private List<TriggerElement> triggers = makeNotDuplicatedList();
  private List<SuspensionElement> suspensions = makeNotDuplicatedList();
  private List<RequestElement> requests = makeNotDuplicatedList();
  private List<ServerElement> servers = newLinkedList();

  public DataImportConfig(InputStream configInputStream) throws IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      Document doc = factory.newDocumentBuilder().parse(configInputStream);

      init(doc);

    } catch (Exception e) {
      throw new DataImportParseException(e);
    } finally {
      if (configInputStream != null) {
        configInputStream.close();
      }
    }
  }

  private static <E extends HasName> List<E> makeNotDuplicatedList() {
    List<E> delegate = newLinkedList();
    return Constraints.constrainedList(delegate, new NotDuplicatedConstraint<E>(delegate));
  }

  private static String normalizePathPart(String pathPart) {
    if (Strings.isNullOrEmpty(pathPart)) {
      return "";
    }

    if (!pathPart.startsWith("/")) {
      pathPart = "/" + pathPart;
    }

    return truncateSlashSuffix(pathPart);

  }

  private static String truncateSlashSuffix(String pathPart) {
    if (pathPart != null && pathPart.endsWith("/")) {
      pathPart = pathPart.substring(0, pathPart.length() - 1);
    }

    return pathPart;
  }

  public List<ScheduledFuture<?>> scheduleWith(DataImportScheduler scheduler) {
    final List<ScheduledFuture<?>> futures = newLinkedList();
    for (ServerElement server : servers) {
      List<SchedulerElement> schedulerElements = server.getSchedulers();

      for (SchedulerElement schedulerElement : schedulerElements) {
        TriggerElement triggerElement = schedulerElement.getTrigger();
        SuspensionElement suspensionElement = schedulerElement.getTriggerSuspension();

        String requestBaseUrl = schedulerElement.getRequestBaseUrl();
        String path = schedulerElement.getDataImportRequestPath();
        Map<String, String> parameterMap = schedulerElement.getRequestParameterMap();

        Suspension suspension = null;
        if (suspensionElement != null) {
          suspension = new Suspension(suspensionElement.timeRanges, suspensionElement.monthDays,
              suspensionElement.monthWeekDays, suspensionElement.weekdays);
        }

        DefaultTrigger trigger = new DefaultTrigger(triggerElement.getName(), triggerElement.getInitDelayMillis(),
            triggerElement.getRepeatIntervalMillis(), suspension);

        ScheduledFuture<?> future = scheduler.schedule(trigger,
            new SolrjDataImportAction(requestBaseUrl, path, parameterMap));
        futures.add(future);
      }

    }
    return futures;
  }

  private void init(Document document) {
    Element rootElement = document.getDocumentElement();

    registerTriggers(rootElement);
    registerTriggerSuspensions(rootElement);
    registerRequests(rootElement);
    registerServers(rootElement);
  }

  private void registerTriggers(Element rootElement) {
    List<TriggerElement> result = parseSingleParentWithChildren(rootElement, TRIGGERS_TAG, TRIGGER_TAG,
        new ElementMapper<TriggerElement>() {

          public TriggerElement map(Element triggerElement) {
            return new TriggerElement(triggerElement);
          }
        });

    for (TriggerElement trigger : result) {
      triggers.add(trigger);
    }
  }

  private void registerTriggerSuspensions(Element rootElement) {
    List<SuspensionElement> result = parseSingleParentWithChildren(rootElement, SUSPENSIONS_TAG, SUSPENSION_TAG,
        new ElementMapper<SuspensionElement>() {
          @Override
          public SuspensionElement map(Element suspensionElement) {
            return new SuspensionElement(suspensionElement);
          }
        });

    for (SuspensionElement suspension : result) {
      suspensions.add(suspension);
    }
  }

  private void registerRequests(Element rootElement) {
    List<RequestElement> result = parseSingleParentWithChildren(rootElement, REQUESTS_TAG, REQUEST_TAG,
        new ElementMapper<RequestElement>() {

          public RequestElement map(Element requestElement) {
            return new RequestElement(requestElement);
          }
        });

    for (RequestElement request : result) {
      requests.add(request);
    }
  }

  private void registerServers(Element rootElement) {
    List<ServerElement> result = parseSingleParentWithChildren(rootElement, SERVERS_TAG, SERVER_TAG,
        new ElementMapper<ServerElement>() {

          public ServerElement map(Element serverElement) {
            return new ServerElement(serverElement);
          }
        });

    servers.addAll(result);
  }

  private <T> List<T> parseSingleParentWithChildren(Element rootElement, String elementTagName,
                                                    String childElementTagName, ElementMapper<T> mapper) {
    NodeList nodeList = rootElement.getElementsByTagName(elementTagName);

    if (nodeList.getLength() != 1) {
      throw new DataImportParseException(String.format("The element '%s' must be present and can appear only once!",
          elementTagName));
    }
    Element parent = (Element) nodeList.item(0);

    NodeList childNodeList = parent.getElementsByTagName(childElementTagName);
    int length = childNodeList.getLength();
    if (length == 0) {
      throw new DataImportParseException(String.format("The element '%s' must be present!", childElementTagName));
    }

    List<T> result = newLinkedList();

    for (int i = 0; i < length; i++) {
      Element child = (Element) childNodeList.item(i);
      result.add(mapper.map(child));
    }
    return result;
  }

  private static interface ElementMapper<T> {
    T map(Element input);
  }

  private static interface HasName {
    String getName();
  }

  private static interface HasSchedulers {
    List<SchedulerElement> getSchedulers();
  }

  private static interface DataImportEnable {
    String getBaseUrl();

    String getPath();
  }

  private static class TriggerElement implements HasName {

    String name;
    StartTimeType startTimeType;
    TimeUnit startTimeUnit;
    String startTimeStringify;
    boolean foundStartTimeElement = false;
    long repeatIntervalMillis;

    public TriggerElement(Element triggerElement) {
      String nameValue = triggerElement.getAttribute(NAME_ATTR);
      if (Strings.isNullOrEmpty(nameValue)) {
        throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can " +
            "not be empty!", "name", TRIGGER_TAG));
      }
      name = nameValue;

      boolean foundRepeatElement = false;

      NodeList childNodes = triggerElement.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node item = childNodes.item(i);
        if (item.getNodeName().equals(START_TIME_TAG)) {
          foundStartTimeElement = true;
          Element startTimeElement = (Element) item;

          String typeStringify = startTimeElement.getAttribute(TYPE_ATTR);
          startTimeType = Strings.isNullOrEmpty(typeStringify) ?
              StartTimeType.time : StartTimeType.valueOf(typeStringify);

          startTimeStringify = startTimeElement.getTextContent();
          if (Strings.isNullOrEmpty(startTimeStringify)) {
            throw new DataImportParseException(String.format("The value of element '%s' must be present!",
                START_TIME_TAG));
          }

          String startTimeUnitValueStringify = startTimeElement.getAttribute(TIME_UNIT_ATTR);
          startTimeUnit = Strings.isNullOrEmpty(startTimeUnitValueStringify) ?
              TimeUnit.MILLISECONDS : TimeUnit.valueOf(startTimeUnitValueStringify.toUpperCase());

        } else if (item.getNodeName().equals(REPEAT_TAG)) {
          foundRepeatElement = true;
          Element repeatElement = (Element) item;

          String repeatValueStringify = repeatElement.getTextContent();
          if (Strings.isNullOrEmpty(repeatValueStringify)) {
            throw new DataImportParseException(String.format("The value of element '%s' must be specified!",
                START_TIME_TAG));
          }

          long repeatValue = Long.valueOf(repeatValueStringify);
          if (repeatValue <= 0) {
            throw new DataImportParseException(String.format("The text content of element '%s' must be a number that " +
                "greater than zero!", START_TIME_TAG));
          }

          String timeUnitValueStringify = repeatElement.getAttribute(TIME_UNIT_ATTR);

          // time-unit default is TimeUnit.MILLISECONDS
          repeatIntervalMillis = Strings.isNullOrEmpty(timeUnitValueStringify) ?
              repeatValue : TimeUnit.valueOf(timeUnitValueStringify.toUpperCase()).toMillis(repeatValue);
        }

      }

      if (!foundRepeatElement) {
        throw new DataImportParseException(String.format("The element '%s' must be present!", REPEAT_TAG));
      }
    }

    public long getInitDelayMillis() {
      if (!foundStartTimeElement) {
        return 0L;
      }

      return startTimeType.getInitDelayMillis(startTimeStringify, startTimeUnit);
    }

    public long getRepeatIntervalMillis() {
      return repeatIntervalMillis;
    }

    @Override
    public String getName() {
      return name;
    }

    enum StartTimeType {
      delay {
        @Override
        public long getInitDelayMillis(String startTimeStringify, TimeUnit startTimeUnit) {
          long delay = Long.valueOf(startTimeStringify);
          if (delay <= 0) {
            throw new DataImportParseException(String.format("The text content of element '%s' must be a number that " +
                "greater than zero!", START_TIME_TAG));
          }
          return startTimeUnit.toMillis(delay);
        }
      },

      time {
        @Override
        public long getInitDelayMillis(String startTimeStringify, TimeUnit startTimeUnit) {
          DateTime now = DateTime.now();
          DateTime startTime = LocalTime.parse(startTimeStringify, TIME_PATTERN).toDateTimeToday();
          if (startTime.isBefore(now)) {
            startTime = startTime.plusDays(1);
          }

          return startTime.getMillis() - now.getMillis();
        }
      };

      public abstract long getInitDelayMillis(String startTimeStringify, TimeUnit startTimeUnit);
    }
  }

  private static class SuspensionElement implements HasName {
    String name;
    List<Suspension.TimeRange> timeRanges = newLinkedList();
    List<MonthDay> monthDays = newLinkedList();
    List<Suspension.WeekdayOfWeekOfMonth> monthWeekDays = newLinkedList();
    List<Integer> weekdays = newLinkedList();

    private SuspensionElement(Element suspensionElement) {
      name = suspensionElement.getAttribute(NAME_ATTR);
      if (Strings.isNullOrEmpty(name)) {
        throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can " +
            "not be empty!", NAME_ATTR, SUSPENSION_TAG));
      }

      // parse time ranges
      NodeList timeRangeElements = suspensionElement.getElementsByTagName(TIME_RANGE_TAG);
      for (int i = 0; i < timeRangeElements.getLength(); i++) {
        Node node = timeRangeElements.item(i);
        Element timeRangeElement = (Element) node;

        String fromValue = timeRangeElement.getAttribute(FROM_ATTR);
        if (Strings.isNullOrEmpty(fromValue)) {
          throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can "
              + "not be empty!", FROM_ATTR, TIME_RANGE_TAG));
        }

        String toValue = timeRangeElement.getAttribute(TO_ATTR);
        if (Strings.isNullOrEmpty(toValue)) {
          throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can "
              + "not be empty!", TO_ATTR, TIME_RANGE_TAG));
        }

        timeRanges.add(new Suspension.TimeRange(LocalTime.parse(fromValue, TIME_PATTERN),
            LocalTime.parse(toValue, TIME_PATTERN)));
      }

      // parse monthDays
      NodeList monthDayElements = suspensionElement.getElementsByTagName(MONTH_DAY_TAG);
      for (int i = 0; i < monthDayElements.getLength(); i++) {
        Node node = monthDayElements.item(i);
        Element monthDayElement = (Element) node;

        String monthValue = monthDayElement.getAttribute(MONTH_ATTR);
        if (Strings.isNullOrEmpty(monthValue)) {
          throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can "
              + "not be empty!", MONTH_ATTR, MONTH_DAY_TAG));
        }

        String dayValue = monthDayElement.getTextContent();
        if (Strings.isNullOrEmpty(dayValue)) {
          throw new DataImportParseException(String.format("The value of element '%s' must be specified!",
              MONTH_DAY_TAG));
        }

        monthDays.add(new MonthDay(Integer.valueOf(monthValue), Integer.valueOf(dayValue)));


      }

      // parse month weekdays
      NodeList monthWeekDayElements = suspensionElement.getElementsByTagName(MONTH_WEEKDAY_TAG);
      for (int i = 0; i < monthWeekDayElements.getLength(); i++) {
        Node node = monthWeekDayElements.item(i);
        Element monthWeekDayElement = (Element) node;

        String monthValue = monthWeekDayElement.getAttribute(MONTH_ATTR);
        if (Strings.isNullOrEmpty(monthValue)) {
          throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can "
              + "not be empty!", MONTH_ATTR, MONTH_WEEKDAY_TAG));
        }

        String weekValue = monthWeekDayElement.getAttribute(WEEK_ATTR);
        if (Strings.isNullOrEmpty(weekValue)) {
          throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can "
              + "not be empty!", WEEK_ATTR, MONTH_WEEKDAY_TAG));
        }

        String dayValue = monthWeekDayElement.getTextContent();
        if (Strings.isNullOrEmpty(dayValue)) {
          throw new DataImportParseException(String.format("The value of element '%s' must be specified!",
              MONTH_WEEKDAY_TAG));
        }

        monthWeekDays.add(new Suspension.WeekdayOfWeekOfMonth(Integer.valueOf(dayValue), Integer.valueOf(weekValue),
            Integer.valueOf(monthValue)));
      }

      // parse weekdays
      NodeList weekdayElements = suspensionElement.getElementsByTagName(WEEKDAY_TAG);
      for (int i = 0; i < weekdayElements.getLength(); i++) {
        Node node = weekdayElements.item(i);
        Element weekdayElement = (Element) node;

        String weekdayValue = weekdayElement.getTextContent();
        if (Strings.isNullOrEmpty(weekdayValue)) {
          throw new DataImportParseException(String.format("The value of element '%s' must be specified!",
              WEEKDAY_TAG));
        }

        weekdays.add(Integer.valueOf(weekdayValue));
      }
    }

    @Override
    public String getName() {
      return name;
    }

  }

  private static class RequestElement implements HasName {
    String name;
    Map<String, String> parameters;

    public RequestElement(Element requestElement) {
      parameters = makeConstraintMap();

      String nameValue = requestElement.getAttribute(NAME_ATTR);
      if (Strings.isNullOrEmpty(nameValue)) {
        throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present and can " +
            "not be empty!", "name", REQUEST_TAG));
      }
      name = nameValue;
      NodeList childNodes = requestElement.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (PARAMETER_TAG.equals(node.getNodeName()) && node.getNodeType() == Node.ELEMENT_NODE) {
          Element parameterElement = (Element) node;

          String paramName = parameterElement.getAttribute(NAME_ATTR);
          if (Strings.isNullOrEmpty(paramName)) {
            throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present!",
                NAME_ATTR, DataImportConstants.PARAMETER_TAG));
          }

          String paramValue = parameterElement.getTextContent();
          if (Strings.isNullOrEmpty(paramValue)) {
            throw new DataImportParseException(String.format("The text content of element '%s' must not be empty!",
                PARAMETER_TAG));
          }

          parameters.put(paramName, paramValue);
        }
      }

    }

    private Map<String, String> makeConstraintMap() {
      Map<String, String> delegate = newLinkedHashMap();
      return MapConstraints.constrainedMap(delegate, new NotDuplicatedKeyMapConstraint(delegate));
    }

    @Override
    public String getName() {
      return name;
    }

    public boolean hasParameters() {
      return parameters.size() > 0;
    }

    public Map<String, String> getParameterMap() {
      return ImmutableMap.copyOf(parameters);
    }

    public String getQueryUrl() {
      StringBuilder builder = new StringBuilder();
      for (Map.Entry<String, String> entry : parameters.entrySet()) {
        if (builder.length() > 0) {
          builder.append('&');
        } else {
          builder.append('?');
        }
        String encodedValue = null;
        try {
          encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
          // impossible go to here
        }
        builder.append(entry.getKey()).append("=").append(encodedValue);
      }

      return builder.toString();
    }

  }

  private static class NotDuplicatedKeyMapConstraint implements MapConstraint<String, String> {
    private final Map<String, String> map;

    NotDuplicatedKeyMapConstraint(Map<String, String> map) {
      this.map = map;
    }

    @Override
    public void checkKeyValue(@Nullable String key, @Nullable String value) {
      checkState(!map.containsKey(key), "The parameter '%s' has been existed.", key);
    }

  }

  private static class NotDuplicatedConstraint<E extends HasName> implements Constraint<E> {
    private final List<E> list;

    private NotDuplicatedConstraint(List<E> list) {
      this.list = list;
    }

    @Override
    public E checkElement(E element) {
      for (E e : list) {
        if (e.getName().equals(element.getName())) {
          throw new IllegalStateException(String.format("The element '%s' has been existed.", element.getName()));
        }
      }
      return element;
    }
  }

  private static class NameEqualsTo<T extends HasName> implements Predicate<T> {
    private final String name;

    private NameEqualsTo(String name) {
      this.name = checkNotNull(name);
    }

    @Override
    public boolean apply(@Nullable T input) {
      return name.equals(input != null ? input.getName() : null);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private class ServerElement implements DataImportEnable, HasSchedulers {
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_CONTEXT = "solr";
    public static final int DEFAULT_PORT = 80;
    private final String host, context;
    private final int port;
    private String dataImportPath;
    private List<SchedulerElement> schedulers;
    private List<CoreElement> cores;

    public ServerElement(Element serverElement) {
      String hostValue = serverElement.getAttribute(HOST_ATTR);
      String contextValue = serverElement.getAttribute(CONTEXT_ATTR);
      contextValue = truncateSlashSuffix(contextValue);
      String portStringify = serverElement.getAttribute(PORT_ATTR);

      this.host = Strings.isNullOrEmpty(hostValue) ? DEFAULT_HOST : hostValue;
      this.context = Strings.isNullOrEmpty(contextValue) ? DEFAULT_CONTEXT : contextValue;
      this.port = Strings.isNullOrEmpty(portStringify) ? DEFAULT_PORT : Integer.valueOf(portStringify);

      schedulers = newLinkedList();
      cores = makeNotDuplicatedList();

      NodeList childNodeList = serverElement.getChildNodes();

      boolean isSingleCore = false;
      boolean isMultiCore = false;
      final String singleMultiCoreBothPresentErrMsg = String.format("The child elements of element '%s' " +
          "must be '%s' or '%s', can't be both present", SERVER_TAG, SCHEDULER_TAG, CORE_TAG);

      final String singleMultiCoreBothAbsentErrMsg = String.format("The child elements of element '%s' " +
          "must be '%s' or '%s', can't be both absent", SERVER_TAG, SCHEDULER_TAG, CORE_TAG);

      for (int i = 0; i < childNodeList.getLength(); i++) {
        Node child = childNodeList.item(i);

        // 该server是single core
        if (SCHEDULER_TAG.equals(child.getNodeName()) && child.getNodeType() == Node.ELEMENT_NODE) {
          isSingleCore = true;
          if (isMultiCore) {
            throw new DataImportParseException(singleMultiCoreBothPresentErrMsg);
          }

          Element schedulerElement = (Element) child;

          String triggerName = schedulerElement.getAttribute(TRIGGER_ATTR);
          if (Strings.isNullOrEmpty(triggerName)) {
            throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present",
                TRIGGER_ATTR, DataImportConstants.SCHEDULER_TAG));
          }

          String suspensionName = schedulerElement.getAttribute(SUSPEND_WITH_ATTR);

          String requestName = schedulerElement.getAttribute(REQUEST_ATTR);
          if (Strings.isNullOrEmpty(requestName)) {
            throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present",
                REQUEST_ATTR, DataImportConstants.SCHEDULER_TAG));
          }

          schedulers.add(new SchedulerElement(this, triggerName, suspensionName, requestName));
          // 该server是multi core
        } else if (CORE_TAG.equals(child.getNodeName()) && child.getNodeType() == Node.ELEMENT_NODE) {
          isMultiCore = true;
          if (isSingleCore) {
            throw new DataImportParseException(singleMultiCoreBothPresentErrMsg);
          }

          Element coreElement = (Element) child;
          cores.add(new CoreElement(this, coreElement));
        }
      }

      if ((!isSingleCore) && (!isMultiCore)) {
        throw new DataImportParseException(singleMultiCoreBothAbsentErrMsg);
      }

      // process dataimport path logic
      if (isSingleCore) {
        String pathValue = serverElement.getAttribute(PATH_ATTR);
        dataImportPath = Strings.isNullOrEmpty(pathValue) ? DEFAULT_DATAIMPORT_PATH : pathValue;

      } else {
        //the server must be multicore structure, dataimport path config inside core,
        // don't need append to here
        dataImportPath = "";
      }

    }

    @Override
    public String getBaseUrl() {
      StringBuilder builder = new StringBuilder("http://");
      builder.append(host);
      if (port != DEFAULT_PORT) {
        builder.append(":").append(port);
      }

      // add solr web application root context to requestBase
      builder.append(normalizePathPart(context));

      return builder.toString();
    }

    @Override
    public String getPath() {
      return normalizePathPart(dataImportPath);
    }

    @Override
    public List<SchedulerElement> getSchedulers() {
      List<SchedulerElement> result = newLinkedList();

      result.addAll(schedulers);

      for (CoreElement core : cores) {
        result.addAll(core.getSchedulers());
      }

      return result;
    }
  }

  private class CoreElement implements DataImportEnable, HasSchedulers, HasName {
    private final ServerElement server;
    private final String name;
    private final List<SchedulerElement> schedulers = newLinkedList();
    private String dataImportPath;

    public CoreElement(ServerElement server, Element coreElement) {
      this.server = server;

      String nameValue = coreElement.getAttribute(NAME_ATTR);
      if (Strings.isNullOrEmpty(nameValue)) {
        throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present!",
            NAME_ATTR, DataImportConstants.CORE_TAG));
      }
      name = nameValue;

      String pathValue = coreElement.getAttribute(PATH_ATTR);
      dataImportPath = Strings.isNullOrEmpty(pathValue) ? DEFAULT_DATAIMPORT_PATH : pathValue;

      NodeList schedulerNodeList = coreElement.getElementsByTagName(SCHEDULER_TAG);
      for (int i = 0; i < schedulerNodeList.getLength(); i++) {
        Node node = schedulerNodeList.item(i);
        if (SCHEDULER_TAG.equals(node.getNodeName()) && node.getNodeType() == Node.ELEMENT_NODE) {
          Element schedulerElement = (Element) node;

          String triggerName = schedulerElement.getAttribute(TRIGGER_ATTR);
          if (Strings.isNullOrEmpty(triggerName)) {
            throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present",
                TRIGGER_ATTR, DataImportConstants.SCHEDULER_TAG));
          }

          String suspensionName = schedulerElement.getAttribute(SUSPEND_WITH_ATTR);

          String requestName = schedulerElement.getAttribute(REQUEST_ATTR);
          if (Strings.isNullOrEmpty(requestName)) {
            throw new DataImportParseException(String.format("The attribute '%s' of element '%s' must be present",
                REQUEST_ATTR, DataImportConstants.SCHEDULER_TAG));
          }

          schedulers.add(new SchedulerElement(this, triggerName, suspensionName, requestName));
        }
      }
    }

    @Override
    public String getBaseUrl() {
      return server.getBaseUrl() + normalizePathPart(name);
    }

    @Override
    public String getPath() {
      return normalizePathPart(dataImportPath);
    }

    @Override
    public List<SchedulerElement> getSchedulers() {
      return schedulers;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  private class SchedulerElement {
    private final DataImportEnable owner;
    private final NameEqualsTo<TriggerElement> triggerName;
    private final NameEqualsTo<SuspensionElement> suspensionName;
    private final NameEqualsTo<RequestElement> requestName;

    public SchedulerElement(DataImportEnable owner, String triggerName, String suspensionName, String requestName) {
      this.owner = checkNotNull(owner);
      this.triggerName = new NameEqualsTo<TriggerElement>(triggerName);
      this.suspensionName = Strings.isNullOrEmpty(suspensionName) ?
          null : new NameEqualsTo<SuspensionElement>(suspensionName);

      this.requestName = new NameEqualsTo<RequestElement>(requestName);
    }

    public TriggerElement getTrigger() {
      return Iterables.find(triggers, triggerName);
    }

    public RequestElement getRequest() {
      return Iterables.find(requests, requestName);
    }

    public SuspensionElement getTriggerSuspension() {
      return suspensionName == null ? null : Iterables.find(suspensions, suspensionName);
    }

    public String getRequestBaseUrl() {
      return owner.getBaseUrl();
    }

    public String getDataImportRequestPath() {
      return owner.getPath();
    }

    public Map<String, String> getRequestParameterMap() {
      return getRequest().getParameterMap();
    }

    public String getDataImportRequestUrl() {
      return getRequestBaseUrl() + getDataImportRequestPath() + getRequest().getQueryUrl();
    }

    public long getInitDelayMillis() {
      return getTrigger().getInitDelayMillis();
    }

    public long getRepeatIntervalMillis() {
      return getTrigger().repeatIntervalMillis;
    }
  }

  public static void main(String[] args) throws IOException {
    InputStream in = DataImportConfig.class.getResourceAsStream("/dataimport.xml");
    DataImportConfig config = new DataImportConfig(in);
    config.scheduleWith(Schedulers.newDataImportScheduler());
  }


}
