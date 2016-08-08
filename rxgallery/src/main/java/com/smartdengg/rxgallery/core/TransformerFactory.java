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
