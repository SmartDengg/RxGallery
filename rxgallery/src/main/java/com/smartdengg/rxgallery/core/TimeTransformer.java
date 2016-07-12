package com.smartdengg.rxgallery.core;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.TimeInterval;

/**
 * Created by Joker on 2016/6/28.
 */
class TimeTransformer<T> implements Observable.Transformer<T, T> {

  @Override public Observable<T> call(Observable<T> tObservable) {
    return tObservable.timeInterval().map(new Func1<TimeInterval<T>, T>() {
      @Override public T call(TimeInterval<T> tTimeInterval) {
        long milliseconds = tTimeInterval.getIntervalInMilliseconds();
        System.out.println("  --->  Rx-Gallery cast : " + milliseconds);
        return tTimeInterval.getValue();
      }
    });
  }
}
