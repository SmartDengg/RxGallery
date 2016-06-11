package com.smartdengg.rxgallery.onsubscribe;

import android.content.CursorLoader;
import android.database.Cursor;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by SmartDengg on 2016/6/11.
 */
public class InternalOnSubscribe implements Observable.OnSubscribe<Cursor> {

    private String[] GALLERY_PROJECTION;

    private final CursorLoader internalLoader;

    public InternalOnSubscribe(CursorLoader internalLoader, String[] GALLERY_PROJECTION) {
        this.internalLoader = internalLoader;
        this.GALLERY_PROJECTION = GALLERY_PROJECTION;
    }

    @Override
    public void call(Subscriber<? super Cursor> subscriber) {
        final Cursor cursor = internalLoader.loadInBackground();

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (internalLoader.isStarted() && cursor != null && !cursor.isClosed()) {
                    internalLoader.cancelLoad();
                    cursor.close();
                }
            }
        }));

        if (cursor == null) {
            Observable.error(new NullPointerException("cursor must not be null"));
        } else {
            try {
                while (cursor.moveToNext() && !subscriber.isUnsubscribed()) {

                    /**exclude .gif*/
                    if (cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[0]))
                              .endsWith(".gif")) {
                        continue;
                    }

                    subscriber.onNext(cursor);
                }
            } catch (Exception e) {
                if (!subscriber.isUnsubscribed()) Observable.error(e);
            } finally {
                if (!cursor.isClosed()) cursor.close();
                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
            }
        }
    }
}
