package com.smartdengg.rxgallery.example;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.TimeInterval;

/**
 * 创建时间:  2017/01/30 18:10 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */

public class Main {

  public static void main(String[] args) {

    Observable.create(new Observable.OnSubscribe<Integer>() {
      @Override public void call(Subscriber<? super Integer> subscriber) {
        System.out.println(System.currentTimeMillis() + "");

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        subscriber.onNext(1);
        subscriber.onCompleted();
      }
    }).map(new Func1<Integer, Integer>() {
      @Override public Integer call(Integer integer) {
        System.out.println(System.currentTimeMillis() + "");
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return 2;
      }
    }).map(new Func1<Integer, Integer>() {
      @Override public Integer call(Integer integer) {
        System.out.println(System.currentTimeMillis() + "");
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return 3;
      }
    }).timeInterval().map(new Func1<TimeInterval<Integer>, Integer>() {
      @Override public Integer call(TimeInterval<Integer> integerTimeInterval) {
        System.out.println(integerTimeInterval.getIntervalInMilliseconds() + "");
        return integerTimeInterval.getValue();
      }
    }).map(new Func1<Integer, Integer>() {
      @Override public Integer call(Integer integer) {
        System.out.println(System.currentTimeMillis() + "");
        try {
          Thread.sleep(4000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return 3;
      }
    }).timeInterval().map(new Func1<TimeInterval<Integer>, Integer>() {
      @Override public Integer call(TimeInterval<Integer> integerTimeInterval) {
        System.out.println(integerTimeInterval.getIntervalInMilliseconds() + "");
        return integerTimeInterval.getValue();
      }
    }).subscribe(new Subscriber<Integer>() {
      @Override public void onCompleted() {

      }

      @Override public void onError(Throwable e) {
        e.printStackTrace();
      }

      @Override public void onNext(Integer integer) {
        System.out.println(integer + "");
        System.out.println(System.currentTimeMillis() + "");
      }
    });

    for (; ; ) ;
  }
}
