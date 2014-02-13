package com.topsoft.search.solr.schedule;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangyg
 */
public class Schedulers {

  /**
   * Cannot instantiate
   */
  private Schedulers() {
  }

  public static DataImportScheduler newSingleThreadDataImportScheduler() {
    return newDataImportScheduler(1);
  }

  public static DataImportScheduler newSingleThreadDataImportScheduler(ThreadFactory threadFactory) {
    return newDataImportScheduler(1, threadFactory);
  }

  public static DataImportScheduler newDataImportScheduler() {
    int processors = Runtime.getRuntime().availableProcessors();
    return newDataImportScheduler(processors);
  }

  public static DataImportScheduler newDataImportScheduler(int corePoolSize) {
    return newDataImportScheduler(corePoolSize, new DefaultThreadFactory("dataimport-scheduler"));
  }

  public static DataImportScheduler newDataImportScheduler(int corePoolSize, ThreadFactory threadFactory) {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    return new DefaultDataImportScheduler(executor);
  }

  public static Scheduler newSingleThreadScheduler() {
    return newScheduler(1);
  }

  public static Scheduler newSingleThreadScheduler(ThreadFactory threadFactory) {
    return newScheduler(1, threadFactory);
  }

  public static Scheduler newScheduler() {
    int processors = Runtime.getRuntime().availableProcessors();
    return newScheduler(processors);
  }

  public static Scheduler newScheduler(int corePoolSize) {
    return newScheduler(corePoolSize, new DefaultThreadFactory("scheduler"));
  }

  public static Scheduler newScheduler(int corePoolSize, ThreadFactory threadFactory) {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    return new DefaultScheduler(executor);
  }

  private static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefix;

    private DefaultThreadFactory(String namePrefix) {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      prefix = namePrefix + "-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r, prefix + threadNumber.getAndIncrement(), 0);

      t.setDaemon(false);

      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY);
      }

      return t;
    }
  }

  private static class DefaultDataImportScheduler extends DefaultScheduler implements DataImportScheduler {

    DefaultDataImportScheduler(ScheduledExecutorService service) {
      super(service);
    }

    @Override
    public List<ScheduledFuture<?>> schedule(DataImportConfig config) {
      return config.scheduleWith(this);
    }
  }
}
