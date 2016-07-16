package com.smartdengg.rxgallery.core;

import android.Manifest;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import com.smartdengg.rxgallery.Utils;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Created by SmartDengg on 2016/3/5.
 */
abstract class GalleryUseCase<T> {

  private static final int TYPE_INTERNAL = 0;
  private static final int TYPE_EXTERNAL = 1;
  static String[] GALLERY_PROJECTION;
  static String DEFAULT_NAME = "全部图片";
  private static Action1<Cursor> DISPOSE_ACTION = new Action1<Cursor>() {
    @Override public void call(Cursor cursor) {
      if (!cursor.isClosed()) cursor.close();
    }
  };

  static {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      GalleryUseCase.GALLERY_PROJECTION = new String[] {
          MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
          MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID,
          MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE,
          MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED,
          MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT
      };
    } else {
      GalleryUseCase.GALLERY_PROJECTION = new String[] {
          MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
          MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID,
          MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE,
          MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED
      };
    }
  }

  final CursorLoader externalLoader;
  final CursorLoader internalLoader;
  String name;
  FolderEntity folderEntity = new FolderEntity();
  private Context context;

  /*package*/GalleryUseCase(Context context, String name) {
    this.context = context;
    this.name = name;
    this.externalLoader =
        new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_PROJECTION,
            null, null, GALLERY_PROJECTION[2] + " DESC");
    this.internalLoader =
        new CursorLoader(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, GALLERY_PROJECTION,
            null, null, GALLERY_PROJECTION[2] + " DESC");
  }

  public Observable<T> retrieveInternalGallery() {

    return this.getObservable(TYPE_INTERNAL)
        .compose(TransformerFactory.applyCursorTransformer(GALLERY_PROJECTION))
        .concatMap(new Func1<Observable<ImageEntity>, Observable<? extends T>>() {
          @Override
          public Observable<? extends T> call(Observable<ImageEntity> imageEntityObservable) {
            return GalleryUseCase.this.hunter(imageEntityObservable);
          }
        })
        .compose(TransformerFactory.<T>applyTimeTransformer());
  }

  public Observable<T> retrieveExternalGallery() {

    if (!Utils.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      throw new RuntimeException(
          "missing permission: 'android.permission.READ_EXTERNAL_STORAGE' in your Manifest.xml");
    }

    return this.getObservable(TYPE_EXTERNAL)
        .compose(TransformerFactory.applyCursorTransformer(GALLERY_PROJECTION))
        .concatMap(new Func1<Observable<ImageEntity>, Observable<? extends T>>() {
          @Override
          public Observable<? extends T> call(Observable<ImageEntity> imageEntityObservable) {
            return GalleryUseCase.this.hunter(imageEntityObservable);
          }
        })
        .compose(TransformerFactory.<T>applyTimeTransformer());
  }

  public Observable<T> retrieveAllGallery() {

    if (!Utils.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      throw new RuntimeException(
          "miss 'android.permission.READ_EXTERNAL_STORAGE' in your Manifest.xml");
    }

    return Observable.merge(this.getObservable(TYPE_INTERNAL), this.getObservable(TYPE_EXTERNAL))
        .compose(TransformerFactory.applyCursorTransformer(GALLERY_PROJECTION))
        .concatMap(new Func1<Observable<ImageEntity>, Observable<? extends T>>() {
          @Override
          public Observable<? extends T> call(Observable<ImageEntity> imageEntityObservable) {
            return GalleryUseCase.this.hunter(imageEntityObservable);
          }
        })
        .compose(TransformerFactory.<T>applyTimeTransformer());
  }

  private Observable<Cursor> getObservable(@Type final int type) {

    return Observable.defer(new Func0<Observable<Cursor>>() {
      @Override public Observable<Cursor> call() {
        return Observable.using(new Func0<Cursor>() {
          @Override public Cursor call() {
            switch (type) {
              case TYPE_INTERNAL:
                return GalleryUseCase.this.internalLoader.loadInBackground();

              case TYPE_EXTERNAL:
                return GalleryUseCase.this.externalLoader.loadInBackground();
            }

            throw new IllegalStateException("Inner Exception");
          }
        }, new CursorFactory(), DISPOSE_ACTION);
      }
    });
  }

  protected abstract Observable<T> hunter(Observable<ImageEntity> cursorObservable);

  @Retention(value = RetentionPolicy.CLASS) @Target(value = ElementType.PARAMETER)
  @IntDef(value = { TYPE_INTERNAL, TYPE_EXTERNAL }) private @interface Type {
  }
}
