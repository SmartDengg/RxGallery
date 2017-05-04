/*
 * Copyright 2016 SmartDengg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.smartdengg.rxgallery.core;

import android.database.Cursor;
import android.os.Build;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.exceptions.OnErrorThrowable;
import rx.plugins.RxJavaHooks;

/**
 * Created by SmartDengg on 2016/7/12.
 */
class OperatorMapCursorToEntity implements Observable.Operator<ImageEntity, Cursor> {

  private String[] projection;

  OperatorMapCursorToEntity(String[] projection) {
    this.projection = projection;
  }

  @Override public Subscriber<? super Cursor> call(Subscriber<? super ImageEntity> child) {
    MapCursorSubscriber parent = new MapCursorSubscriber(child, projection);
    child.add(parent);
    return parent;
  }

  private static final class MapCursorSubscriber extends Subscriber<Cursor> {

    private String[] projection;
    private Subscriber<? super ImageEntity> actual;
    boolean done;

    MapCursorSubscriber(Subscriber<? super ImageEntity> child, String[] projection) {
      this.actual = child;
      this.projection = projection;
    }

    @Override public void onCompleted() {
      if (done) return;
      actual.onCompleted();
    }

    @Override public void onError(Throwable e) {

      if (done) {
        RxJavaHooks.onError(e);
        return;
      }
      done = true;
      actual.onError(e);
    }

    @Override public void onNext(Cursor cursor) {

      if (isUnsubscribed()) return;

      ImageEntity result;

      try {
        result = this.convertToImageEntity(cursor);
      } catch (Exception ex) {
        Exceptions.throwIfFatal(ex);
        onError(OnErrorThrowable.addValueAsLastCause(ex, cursor));
        unsubscribe();
        return;
      }

      actual.onNext(result);
    }

    @Override public void setProducer(Producer p) {
      actual.setProducer(p);
    }

    private ImageEntity convertToImageEntity(Cursor cursor) {
      String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
      String imageName = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]));
      long addDate = cursor.getLong(cursor.getColumnIndexOrThrow(projection[2]));
      long id = cursor.getLong(cursor.getColumnIndexOrThrow(projection[3]));
      String title = cursor.getString(cursor.getColumnIndexOrThrow(projection[4]));
      String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(projection[5]));

      long size = cursor.getLong(cursor.getColumnIndexOrThrow(projection[6]));
      long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(projection[7]));

      ImageEntity entity = ImageEntity.newInstance();

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        String width = cursor.getString(cursor.getColumnIndexOrThrow(projection[8]));
        String Height = cursor.getString(cursor.getColumnIndexOrThrow(projection[9]));
        entity.setWidth(width);
        entity.setHeight(Height);
      } else {
        entity.setWidth("0");
        entity.setHeight("0");
      }

      entity.setImagePath(imagePath);
      entity.setImageName(imageName);
      entity.setAddDate(addDate);
      entity.setId(id);
      entity.setTitle(title);
      entity.setMimeType(mimeType);
      entity.setSize(size);
      entity.setModifyDate(modifyDate);
      return entity;
    }
  }
}
