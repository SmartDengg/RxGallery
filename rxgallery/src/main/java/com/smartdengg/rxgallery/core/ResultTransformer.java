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
import com.smartdengg.rxgallery.entity.ImageEntity;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by SmartDengg on 2016/10/2.
 */
class ResultTransformer<T> implements Observable.Transformer<Cursor, T> {

  private ImageHunter<T> imageHunter;

  ResultTransformer(ImageHunter<T> hunter) {
    this.imageHunter = hunter;
  }

  @Override public Observable<T> call(Observable<Cursor> cursorObservable) {
    return cursorObservable.compose(TransformerFactory.applyCursorTransformer())
        .concatMap(new Func1<Observable<ImageEntity>, Observable<? extends T>>() {
          @Override
          public Observable<? extends T> call(Observable<ImageEntity> imageEntityObservable) {
            return imageHunter.hunt(imageEntityObservable);
          }
        })
        .compose(TransformerFactory.<T>applyTimeTransformer());
  }
}
