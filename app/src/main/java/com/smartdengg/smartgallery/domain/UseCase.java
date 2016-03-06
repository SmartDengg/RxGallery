package com.smartdengg.smartgallery.domain;

import android.support.annotation.CheckResult;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public abstract class UseCase<E> {

  private Subscription subscription = Subscriptions.empty();

  @SuppressWarnings("unchecked") public void subscribe(Observer<E> useCaseSubscriber) {

    this.subscription = Observable.defer(new Func0<Observable<E>>() {
      @Override public Observable<E> call() {
        return UseCase.this.interactor();
      }
    }).onBackpressureBuffer().filter(new Func1<E, Boolean>() {
      @Override public Boolean call(E e) {
        return !subscription.isUnsubscribed();
      }
    }).subscribe(useCaseSubscriber);
  }

  public void unsubscribe() {
    if (!subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }

  @CheckResult protected abstract Observable<E> interactor();
}
