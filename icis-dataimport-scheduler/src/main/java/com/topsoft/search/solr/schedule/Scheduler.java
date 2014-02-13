package com.topsoft.search.solr.schedule;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author wangyg
 */
public interface Scheduler {
  /**
   * @param trigger
   * @param command
   * @return
   */
  ScheduledFuture<?> schedule(Trigger trigger, Runnable command);

  /**
   * Initiates an orderly shutdown in which previous submitted
   * tasks are executed
   */
  void shutdown();

  /**
   * @return list of tasks that never commenced execution
   */
  List<Runnable> shutdownNow();

  /**
   * @return <tt>true</tt> if the scheduler has been shut down
   */
  boolean isShutdown();

  /**
   * @return <tt>true</tt> if all tasks have completed following shut down
   */
  boolean isTerminated();

  /**
   * Block until all tasks have completed execution after a shutdown request,
   * or the timeout occurs, or the current thread is interrupted, whichever
   * happens first
   *
   * @param timeout the maximum time to wait
   * @param unit    the time unit of the timeout argument
   * @return <tt>true</tt> if the Scheduler terminated and
   *         <tt>false</tt> if the timeout elapsed before termination
   * @throws InterruptedException if interrupted while waiting
   */
  boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
