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
import java.io.File;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Joker on 2016/6/28.
 */
class CursorTransformer implements Observable.Transformer<Cursor, Observable<ImageEntity>> {

  private static final Func1<Cursor, Boolean> STOP_PREDICATE_FUNCTION =
      new Func1<Cursor, Boolean>() {
        @Override public Boolean call(Cursor cursor) {
          return cursor.isClosed();
        }
      };

  private static final Func1<ImageEntity, Boolean> FILTER_FUNCTION =
      new Func1<ImageEntity, Boolean>() {
        @Override public Boolean call(ImageEntity imageEntity) {
          /*校验文件是否存在*/
          File file = new File(imageEntity.getImagePath());
          File parentFile = file.getParentFile();

          return file.exists() && file.canRead() && parentFile != null;
        }
      };

  CursorTransformer() {
  }

  @Override public Observable<Observable<ImageEntity>> call(Observable<Cursor> cursorObservable) {

    return cursorObservable.takeUntil(STOP_PREDICATE_FUNCTION)
        .onBackpressureBuffer()
        .lift(new OperatorMapCursorToEntity(GalleryUseCase.GALLERY_PROJECTION))
        .filter(FILTER_FUNCTION)
        .nest();
  }
}
