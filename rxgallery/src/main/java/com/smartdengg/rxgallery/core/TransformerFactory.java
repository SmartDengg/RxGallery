package com.smartdengg.rxgallery.core;

import android.database.Cursor;
import com.smartdengg.rxgallery.entity.ImageEntity;
import rx.Observable;

/**
 * Created by Joker on 2016/6/28.
 */
public class TransformerFactory {

  private TransformerFactory() {
    throw new IllegalStateException("No instance");
  }

  @SuppressWarnings("unchecked") static <T> Observable.Transformer<T, T> applyTimeTransformer() {
    return (Observable.Transformer<T, T>) new TimeTransformer<>();
  }

  @SuppressWarnings("unchecked")
  static Observable.Transformer<Cursor, Observable<ImageEntity>> applyCursorTransformer(
      String[] galleryProjection) {
    return new CursorTransformer(galleryProjection);
  }
}
