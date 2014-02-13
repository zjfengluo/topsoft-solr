package com.topsoft.search.solr.schedule;

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.*;

/**
 * @author wangyg
 */
class DefaultTrigger implements Trigger {
  private final long initDelayMillis, repeatMillis;
  private final Suspension suspension;
  private final AtomicInteger instanceNumber = new AtomicInteger(1);
  private String name;

  DefaultTrigger(String name, long initDelayMillis, long repeatMillis, @Nullable Suspension suspension) {
    this.name = Strings.isNullOrEmpty(name) ?
        DefaultTrigger.class.getName() + "-" + instanceNumber.getAndIncrement() : name;
    checkArgument(initDelayMillis >= 0);
    checkArgument(initDelayMillis >= 0);
    this.initDelayMillis = initDelayMillis;
    this.repeatMillis = repeatMillis;
    this.suspension = suspension;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public long getInitDelayMillis() {
    return initDelayMillis;
  }

  @Override
  public long getRepeatMillis() {
    return repeatMillis;
  }

  @Override
  public boolean shouldSuspendNow() {
    if (suspension == null) {
      return false;
    }

    return suspension.shouldSuspend(new Date());
  }
}
