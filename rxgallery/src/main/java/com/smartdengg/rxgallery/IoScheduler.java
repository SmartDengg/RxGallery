package com.smartdengg.rxgallery;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Joker on 2015/8/10.
 */
@SuppressWarnings("All")
public class IoScheduler {

    static final Observable.Transformer ioTransformer = new Observable.Transformer() {
        @Override
        public Object call(Object observable) {
            return ((Observable) observable).subscribeOn(Schedulers.io());
        }
    };

    /**
     * Don't break the chain: use RxJava's compose() operator
     */

    public static <T> Observable.Transformer<T, T> apply() {
        return (Observable.Transformer<T, T>) ioTransformer;
    }
}
