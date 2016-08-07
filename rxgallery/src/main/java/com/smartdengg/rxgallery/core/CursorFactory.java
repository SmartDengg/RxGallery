package com.smartdengg.rxgallery.core;

import android.database.Cursor;
import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

/**
 * Created by Joker on 2016/6/27.
 */
class CursorFactory implements Func1<Cursor, Observable<Cursor>> {

  private CursorFactory() {
  }

  public static CursorFactory created() {
    return new CursorFactory();
  }

  @Override public Observable<Cursor> call(final Cursor cursor) {
    return Observable.create(new Observable.OnSubscribe<Cursor>() {
      @Override public void call(Subscriber<? super Cursor> subscriber) {
        try {
          while (cursor.moveToNext() && !subscriber.isUnsubscribed()) {
            /**exclude .gif*/
            //@formatter:off
            if (cursor.getString(cursor.getColumnIndexOrThrow(GalleryUseCase.GALLERY_PROJECTION[0])).endsWith(".gif")) {
              continue;
            }

            subscriber.onNext(cursor);
          }
        } catch (Exception e) {
          Exceptions.throwIfFatal(e);
          if (!subscriber.isUnsubscribed()) subscriber.onError(e);
        } finally {
          if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
        }
      }
    });
  }
}
