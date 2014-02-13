package com.topsoft.search.solr.schedule;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
class DefaultScheduler implements Scheduler {
  private static final Logger logger = LoggerFactory.getLogger(DefaultScheduler.class);
  private final ScheduledExecutorService executor;

  DefaultScheduler(ScheduledExecutorService service) {
    this.executor = checkNotNull(service);
  }

  @Override
  public ScheduledFuture<?> schedule(Trigger trigger, Runnable command) {
    TriggerRunnableAdapter runnableAdapter = new TriggerRunnableAdapter(checkNotNull(trigger), checkNotNull(command));

    return executor.scheduleAtFixedRate(runnableAdapter, trigger.getInitDelayMillis(), trigger.getRepeatMillis(),
        TimeUnit.MILLISECONDS);
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return executor.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return executor.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return executor.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return executor.awaitTermination(timeout, unit);
  }

  protected ScheduledExecutorService getExecutor() {
    return executor;
  }

  private static class TriggerRunnableAdapter implements Runnable {
    final Trigger trigger;
    final Runnable delegate;

    private TriggerRunnableAdapter(Trigger trigger, Runnable delegate) {
      this.trigger = trigger;
      this.delegate = delegate;
    }

    @Override
    public void run() {
      DateTime now = DateTime.now();

      if (trigger.shouldSuspendNow()) {
        if (logger.isDebugEnabled()) {
          logger.debug("trigger[{}] has been suspended at {}", trigger.getName(), now);
        }

        return;
      }

      if (logger.isDebugEnabled()) {
        logger.debug("trigger[{}] is fired at {}", trigger.getName(), now);
      }
      delegate.run();
    }
  }

}
