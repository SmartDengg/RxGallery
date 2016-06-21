package com.smartdengg.rxgallery.example.activity;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.rxgallery.example.R;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.BooleanSubscription;

public class MainActivity extends AppCompatActivity {

    private AtomicInteger atomicInteger = new AtomicInteger(5);
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(MainActivity.this);
    }

    @NonNull
    @OnClick(R.id.gallery_button)
    protected void onGalleryClick() {

        GalleryActivity.navigateToGallery(MainActivity.this);
    }

    private void reduce() {

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                if (subscriber.isUnsubscribed()) return;

                Integer blocking = blocking();

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(blocking);
                    System.out.println("blocking = [" + blocking + "]");
                    if (blocking >= 0) subscriber.onCompleted();
                }
            }
        })
                  .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                      @Override
                      public Observable<?> call(Observable<? extends Void> observable) {

                          return observable.map(new Func1<Void, Integer>() {
                              @Override
                              public Integer call(Void aVoid) {
                                  return new Random().nextInt(5);
                              }
                          })
                                           .concatMap(new Func1<Integer, Observable<?>>() {
                                               @Override
                                               public Observable<?> call(Integer integer) {

                                                   System.out.println("integer = [" + integer + "]");

                                                   return Observable.timer((long) Math.pow(2, integer), TimeUnit.SECONDS, Schedulers.immediate());
                                               }
                                           });

                      }
                  })
                  .subscribeOn(Schedulers.newThread())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Subscriber<Integer>() {
                      @Override
                      public void onCompleted() {

                      }

                      @Override
                      public void onError(Throwable e) {

                      }

                      @Override
                      public void onNext(Integer integer) {
                          //System.out.println("onNext:" + integer);
                      }
                  });
    }

    private void reduce2() {

        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();

        subscription = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {

                if (subscriber.isUnsubscribed()) return;

                final BlockingObservable<Integer> blockingObservable = blocking2();

                subscriber.add(BooleanSubscription.create(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("isUnsubscribed");

                    }
                }));

                long startTime = System.currentTimeMillis();
                System.out.println("Start:  " + startTime);

                Integer value = blockingObservable.single();

                long endTime = System.currentTimeMillis();
                System.out.println("End:  " + endTime);
                System.out.println("Total:  " + (endTime - startTime));

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(value);
                    System.out.println("value = [" + value + "]");
                    //if (blocking <= 4) subscriber.onCompleted();
                }
            }
        })
                                 .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                                     @Override
                                     public Observable<?> call(Observable<? extends Void> observable) {
                                         return observable.concatMap(new Func1<Void, Observable<?>>() {
                                             @Override
                                             public Observable<?> call(Void aVoid) {
                                                 //return Observable.error(new RuntimeException("error"));
                                                 //return Observable.empty();
                                                 return Observable.just(aVoid);
                                             }
                                         });
                                     }
                                 })
                                 .subscribeOn(Schedulers.newThread())
                                 .observeOn(AndroidSchedulers.mainThread())
                                 .subscribe(new Subscriber<Integer>() {
                                     @Override
                                     public void onCompleted() {
                                         System.out.println("onCompleted");
                                     }

                                     @Override
                                     public void onError(Throwable e) {
                                         System.out.println(e.toString());
                                     }

                                     @Override
                                     public void onNext(Integer integer) {
                                         System.out.println("onNext:" + integer);
                                     }
                                 });
    }

    private Integer blocking() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                if (subscriber.isUnsubscribed()) return;

                SystemClock.sleep(1000);

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(atomicInteger.getAndDecrement());
                    subscriber.onCompleted();
                }
            }
        })
                         .toBlocking()
                         .single();
    }

    private BlockingObservable<Integer> blocking2() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                if (subscriber.isUnsubscribed()) return;

                SystemClock.sleep(5000);

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(new Random().nextInt(10));
                    subscriber.onCompleted();
                }
            }
        })
                         .toBlocking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(MainActivity.this);
    }
}
