package com.smartdengg.rxgallery.core;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Joker on 2015/8/10.
 */
@SuppressWarnings("unchecked") class IoScheduler {

  static final Observable.Transformer ioTransformer = new Observable.Transformer() {
    @Override public Object call(Object observable) {
      return ((Observable) observable).subscribeOn(Schedulers.io());
    }
  };

  /**
   * Don't break the chain: use RxJava's compose() operator
   */
  static <T> Observable.Transformer<T, T> apply() {
    return (Observable.Transformer<T, T>) ioTransformer;
  }
}
