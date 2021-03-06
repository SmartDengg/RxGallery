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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class MainActivity2 extends AppCompatActivity {

  private static final Integer THRESHOLD = 4;
  private static final int INITIAL_DELAY = 0;
  private static final Integer PERIOD = 5;
  private static final Observable.Transformer newTransformer = new Observable.Transformer() {
    @Override public Object call(Object observable) {
      return ((Observable) observable).subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread());
    }
  };
  private static AtomicInteger delayAtomic = new AtomicInteger(0);
  private Subscription subscription = Subscribers.empty();
  private Subscription loopSubscription = Subscriptions.empty();

  @SuppressWarnings("unchecked")
  private static <T> Observable.Transformer<T, T> applyNewSchedulers() {
    return (Observable.Transformer<T, T>) newTransformer;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_layout);
    ButterKnife.bind(MainActivity2.this);
  }

  @NonNull @OnClick(R.id.gallery_button) protected void onReduceClick() {

    if (loopSubscription != null && !loopSubscription.isUnsubscribed()) {
      loopSubscription.unsubscribe();
    }
    if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();

    loopSubscription = Observable.interval(INITIAL_DELAY, PERIOD, TimeUnit.SECONDS)
        .subscribe(new Subscriber<Long>() {
          @Override public void onCompleted() {

          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onNext(Long aLong) {
            MainActivity2.this.reduce();
          }
        });
  }

  private void reduce() {

    if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();

    subscription = Observable.defer(new Func0<Observable<Integer>>() {
      @Override public Observable<Integer> call() {

        Executor<Integer> executor = MainActivity2.this.getExecutor(new Random().nextInt(10));
        System.out.println("executor:   " + executor.value);

        return Observable.create(new CallOnSubscribe<>(executor));
      }
    }).repeatWhen(new Func1<Observable<? extends Void>, Observable<Long>>() {
      @Override public Observable<Long> call(Observable<? extends Void> observable) {

        return observable.map(new Func1<Void, Integer>() {
          @Override public Integer call(Void aVoid) {
            return delayAtomic.get();
          }
        }).concatMap(new Func1<Integer, Observable<Long>>() {
          @Override public Observable<Long> call(Integer integer) {

            System.out.println("Delay = [" + "2^" + integer + "] = " +
                (int) Math.pow(2, integer));

            return Observable.timer((long) Math.pow(2, integer), TimeUnit.SECONDS,
                Schedulers.immediate());
          }
        });
      }
    }).compose(MainActivity2.<Integer>applyNewSchedulers()).subscribe(new Subscriber<Integer>() {
      @Override public void onCompleted() {
                                         /*因为.repeatWhen()操作符，因此.onCompleted()永远不会调用*/
        //System.out.println("onCompleted");
      }

      @Override public void onError(Throwable e) {
        System.out.println("e = [" + e + "]");
      }

      @Override public void onNext(Integer integer) {
        //System.out.println("OnNext : " + integer);
      }
    });
  }

  private <T> Executor<T> getExecutor(T value) {
    return new Executor<>(value);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(MainActivity2.this);
  }

  private static final class CallOnSubscribe<T> implements Observable.OnSubscribe<T> {

    private Executor<T> executor;

    public CallOnSubscribe(Executor<T> executor) {
      this.executor = executor;
    }

    @Override public void call(Subscriber<? super T> subscriber) {
      IMArbiter arbiter = new IMArbiter<>(executor, subscriber);
      subscriber.add(arbiter);
      subscriber.setProducer(arbiter);
    }
  }

  private static final class IMArbiter<T> extends AtomicBoolean implements Subscription, Producer {

    private final AtomicBoolean unsubscribed = new AtomicBoolean(false);
    private Executor<T> executor;
    private Subscriber<? super T> subscriber;

    public IMArbiter(Executor<T> executor, Subscriber<? super T> subscriber) {
      this.executor = executor;
      this.subscriber = subscriber;
    }

    @Override public void request(long n) {

      if (n < 0) throw new IllegalArgumentException("n < 0: " + n);
      if (n == 0) return; // Nothing to do when requesting 0.
      if (!this.compareAndSet(false, true)) return; // Request was already triggered.

      long start = System.currentTimeMillis();
      //System.out.println("Start : " + start);
      /*this will blocking*/
      T value = executor.execute();
      if (executor.isCanceled()) System.out.println("Canceled: " + value);
      long end = System.currentTimeMillis();
      //System.out.println("End  : " + end);
      if (!subscriber.isUnsubscribed() && !executor.isCanceled()) {
        subscriber.onNext(value);
        System.out.println("Value :   " + value + "  duration : " + (end - start));
        if (value.getClass().isAssignableFrom(Integer.class) && (Integer) value <= THRESHOLD) {/*如果小于'THRESHOLD'的话，重试*/
          System.out.println("----重试----");
          /*设置重试退避时间*/
          delayAtomic.set((Integer) value);
          subscriber.onCompleted();
        }
      }
    }

    @Override public void unsubscribe() {
      if (this.unsubscribed.compareAndSet(false, true)) executor.cancel();
    }

    @Override public boolean isUnsubscribed() {
      return unsubscribed.get() && executor.isCanceled();
    }
  }

  private class Executor<T> {

    private T value;
    private boolean isCanceled = false;

    public Executor(T i) {
      this.value = i;
    }

    public T execute() {
      SystemClock.sleep(2 * 1000);
      return value;
    }

    public void cancel() {
      this.isCanceled = true;
    }

    public boolean isCanceled() {
      return this.isCanceled;
    }
  }
}
