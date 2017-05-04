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

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.observables.SyncOnSubscribe;

import static com.smartdengg.rxgallery.Utils.hasReadExternalPermission;
import static com.smartdengg.rxgallery.core.GalleryUseCase.Type.TYPE_EXTERNAL;
import static com.smartdengg.rxgallery.core.GalleryUseCase.Type.TYPE_INTERNAL;

/**
 * 创建时间:  2017/01/30 16:21 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
abstract class GalleryUseCase<T> implements ImageHunter<T> {

  static String[] GALLERY_PROJECTION;

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

  private static class CursorDisposeHelper {

    private static Action1<Cursor> createAction(CursorLoader cursorLoader) {
      return new DisposeAction(cursorLoader);
    }

    private static class DisposeAction implements Action1<Cursor> {

      private CursorLoader cursorLoader;

      private DisposeAction(CursorLoader cursorLoader) {
        this.cursorLoader = cursorLoader;
      }

      @Override public void call(Cursor cursor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          cursorLoader.cancelLoadInBackground();
        }
        if (!cursor.isClosed()) cursor.close();
      }
    }
  }

  private Context context;
  private final CursorLoader internalLoader;
  private final CursorLoader externalLoader;

  /*package*/GalleryUseCase(Context context) {
    this.context = context;
    this.internalLoader =
        new CursorLoader(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, GALLERY_PROJECTION,
            null, null, GALLERY_PROJECTION[2] + " DESC");
    this.externalLoader =
        new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_PROJECTION,
            null, null, GALLERY_PROJECTION[2] + " DESC");

    //this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
    //    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    //MediaScannerConnection.scanFile(context,
    //    new String[] { Environment.getExternalStorageDirectory().getAbsolutePath() }, null,
    //    new MediaScannerConnection.OnScanCompletedListener() {
    //
    //      @Override public void onScanCompleted(String path, Uri uri) {
    //        Log.d("gallery", "uri" + uri.toString());
    //        Log.d("gallery", "path" + path);
    //      }
    //    });
  }

  public Observable<T> scanInternalGallery() {
    return createCursorObservable(TYPE_INTERNAL, internalLoader).compose(
        TransformerFactory.<T>applyHunterTransformer(this));
  }

  public Observable<T> scanExternalGallery() {

    if (!hasReadExternalPermission(context)) return Observable.empty();

    return createCursorObservable(TYPE_EXTERNAL, internalLoader).compose(
        TransformerFactory.<T>applyHunterTransformer(this));
  }

  public Observable<T> scanAllGallery() {

    if (!hasReadExternalPermission(context)) return Observable.empty();

    return Observable.merge(createCursorObservable(TYPE_INTERNAL, internalLoader),
        createCursorObservable(TYPE_EXTERNAL, externalLoader))
        .compose(TransformerFactory.<T>applyHunterTransformer(this));
  }

  private Observable<Cursor> createCursorObservable(final Type type, CursorLoader cursorLoader) {
    return Observable.create(
        SyncOnSubscribe.createStateful(new CursorGeneratorHelper(type), CursorFactory.create(),
            CursorDisposeHelper.createAction(cursorLoader)));
  }

  private class CursorGeneratorHelper implements Func0<Cursor> {

    private Type type;

    private CursorGeneratorHelper(Type type) {
      this.type = type;
    }

    @Override public Cursor call() {
      switch (type) {
        case TYPE_INTERNAL:
          return GalleryUseCase.this.internalLoader.loadInBackground();
        case TYPE_EXTERNAL:
          return GalleryUseCase.this.externalLoader.loadInBackground();
      }
      throw new IllegalStateException("Inner Exception");
    }
  }

  enum Type {
    TYPE_INTERNAL,

    TYPE_EXTERNAL
  }
}
