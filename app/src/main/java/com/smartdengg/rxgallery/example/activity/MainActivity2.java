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

public class MainActivity2 extends AppCompatActivity {

    private AtomicInteger atomicInteger = new AtomicInteger(5);
    private Subscription subscription = Subscribers.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(MainActivity2.this);
    }

    @NonNull
    @OnClick(R.id.gallery_button)
    protected void onGalleryClick() {

        MainActivity2.this.reduce();
    }

    private void reduce() {

        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();

        subscription = Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {

                Executor<Integer> executor = MainActivity2.this.getExecutor(new Random().nextInt(10));
                System.out.printf("executor:   " + executor.value);

                return Observable.create(new CallOnSubscribe<>(executor));
            }
        })
                                 .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                                     @Override
                                     public Observable<?> call(Observable<? extends Void> observable) {
                                         return Observable.timer(4, TimeUnit.SECONDS, Schedulers.immediate());
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

                                     }

                                     @Override
                                     public void onNext(Integer integer) {
                                         System.out.println("OnNext : " + integer);
                                     }
                                 });
    }

    private static final class CallOnSubscribe<T> implements Observable.OnSubscribe<T> {

        private Executor<T> executor;

        public CallOnSubscribe(Executor<T> executor) {
            this.executor = executor;
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {

            IMArbiter arbiter = new IMArbiter<>(executor, subscriber);
            subscriber.add(arbiter);
            subscriber.setProducer(arbiter);
        }
    }

    private static final class IMArbiter<T> extends AtomicBoolean implements Subscription, Producer {

        private Executor<T> executor;
        private Subscriber<? super T> subscriber;
        private final AtomicBoolean unsubscribed = new AtomicBoolean(false);

        public IMArbiter(Executor<T> executor, Subscriber<? super T> subscriber) {
            this.executor = executor;
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {

            if (n < 0) throw new IllegalArgumentException("n < 0: " + n);
            if (n == 0) return; // Nothing to do when requesting 0.
            if (!this.compareAndSet(false, true)) return; // Request was already triggered.

            long start = System.currentTimeMillis();
            System.out.println("Start : " + start);
            /*this will blocking*/
            T value = executor.execute();
            long end = System.currentTimeMillis();
            System.out.println("End : " + end);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(value);
                System.out.printf("Value:->" + value + "Total:->" + (end - start));
                if (value.getClass()
                         .isAssignableFrom(Integer.class)) {
                    if ((Integer) value <= 6) subscriber.onCompleted();
                }
            }
        }

        @Override
        public void unsubscribe() {
            if (this.unsubscribed.compareAndSet(false, true)) executor.cancel();
        }

        @Override
        public boolean isUnsubscribed() {
            return unsubscribed.get() && executor.isCanceled();
        }
    }

    private <T> Executor<T> getExecutor(T value) {
        return new Executor<>(value);
    }

    private class Executor<T> {

        private T value;
        private boolean isCanceled = false;

        public Executor(T i) {
            this.value = i;
        }

        public T execute() {
            SystemClock.sleep(3 * 1000);
            return value;
        }

        public void cancel() {
            this.isCanceled = true;
        }

        public boolean isCanceled() {
            return this.isCanceled;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(MainActivity2.this);
    }
}
