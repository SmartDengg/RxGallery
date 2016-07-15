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

          return file.exists() && parentFile != null;
        }
      };

  private String[] galleryProjection;

  public CursorTransformer(String[] galleryProjection) {
    this.galleryProjection = galleryProjection;
  }

  @Override public Observable<Observable<ImageEntity>> call(Observable<Cursor> cursorObservable) {

    return cursorObservable.takeUntil(STOP_PREDICATE_FUNCTION)
        .onBackpressureBuffer()
        .lift(new OperatorMapCursorToEntity(galleryProjection))
        .filter(FILTER_FUNCTION)
        .nest();
  }
}
