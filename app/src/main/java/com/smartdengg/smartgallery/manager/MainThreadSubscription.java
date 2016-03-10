package com.smartdengg.smartgallery.manager;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import rx.Subscription;

/**
 * Created by SmartDengg on 2016/3/10.
 */
public abstract class MainThreadSubscription implements Subscription, Runnable {
  private static final Handler mainThread = new Handler(Looper.getMainLooper());
  private static final String fieldName = "unsubscribed";

  private static final int UN = 0;
  private static final int ALREADY = 1;

  private volatile int unsubscribed;
  private static final AtomicIntegerFieldUpdater<MainThreadSubscription> unsubscribedUpdater =
      AtomicIntegerFieldUpdater.newUpdater(MainThreadSubscription.class, fieldName);

  @Override public void unsubscribe() {
    if (MainThreadSubscription.unsubscribedUpdater.compareAndSet(MainThreadSubscription.this, UN, ALREADY)) {
      if (Looper.getMainLooper() == Looper.myLooper()) {
        MainThreadSubscription.this.onUnsubscribe();
      } else {
        mainThread.post(this);
      }
    }
  }

  @Override public boolean isUnsubscribed() {
    return this.unsubscribed != 0;
  }

  @Override public void run() {
    MainThreadSubscription.this.onUnsubscribe();
  }

  protected abstract void onUnsubscribe();
}
