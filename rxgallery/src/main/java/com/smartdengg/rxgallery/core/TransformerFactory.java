package com.smartdengg.rxgallery.core;

import com.smartdengg.rxgallery.entity.ImageEntity;
import rx.Observable;

/**
 * Created by Joker on 2016/6/28.
 */
public class TransformerFactory {

    private TransformerFactory() {
        throw new IllegalStateException("No instance");
    }

    @SuppressWarnings("unchecked")
    static <T> Observable.Transformer<T, T> applyTimeTransformer() {
        return (Observable.Transformer<T, T>) new TimeTransformer<>();
    }

    @SuppressWarnings("unchecked")
    static <T,R> Observable.Transformer<T, R> applyCursorTransformer(String[] galleryProjection, ImageEntity parent) {
        return (Observable.Transformer<T, R>) new CursorTransformer(galleryProjection, parent);
    }
}
