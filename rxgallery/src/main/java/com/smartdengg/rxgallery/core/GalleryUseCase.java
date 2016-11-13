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

import android.Manifest;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.smartdengg.rxgallery.Utils;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.ui.TransparentActivity;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.observables.SyncOnSubscribe;

import static com.smartdengg.rxgallery.core.GalleryUseCase.Type.TYPE_EXTERNAL;
import static com.smartdengg.rxgallery.core.GalleryUseCase.Type.TYPE_INTERNAL;

/**
 * Created by SmartDengg on 2016/3/5.
 */
abstract class GalleryUseCase<T> implements ImageHunter<T> {

  static String[] GALLERY_PROJECTION;
  static String DEFAULT_NAME = "全部图片";

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

  private static class CursorCloseHolder {
    private static final Action1<Cursor> DISPOSE_ACTION = new Action1<Cursor>() {
      @Override public void call(Cursor cursor) {
        if (!cursor.isClosed()) cursor.close();
      }
    };
  }

  private final CursorLoader externalLoader;
  private final CursorLoader internalLoader;
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

    //this.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
    //    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    MediaScannerConnection.scanFile(context,
        new String[] { Environment.getExternalStorageDirectory().getAbsolutePath() }, null,
        new MediaScannerConnection.OnScanCompletedListener() {

          @Override public void onScanCompleted(String path, Uri uri) {

            Log.d("gallery", "uri" + uri.toString());
            Log.d("gallery", "path" + path);
          }
        });
  }

  public Observable<T> retrieveInternalGallery() {
    return this.createCursorObservable(TYPE_INTERNAL)
        .compose(TransformerFactory.<T>applyHunterTransformer(this));
  }

  public Observable<T> retrieveExternalGallery() {

    if (!this.hasReadExternalPermission()) return Observable.empty();

    return this.createCursorObservable(TYPE_EXTERNAL)
        .compose(TransformerFactory.<T>applyHunterTransformer(this));
  }

  public Observable<T> retrieveAllGallery() {

    if (!this.hasReadExternalPermission()) return Observable.empty();

    return Observable.merge(createCursorObservable(TYPE_INTERNAL),
        createCursorObservable(TYPE_EXTERNAL))
        .compose(TransformerFactory.<T>applyHunterTransformer(this));
  }

  private boolean hasReadExternalPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && !Utils.hasPermission(context,
        Manifest.permission.READ_EXTERNAL_STORAGE)) {
      TransparentActivity.navigateToTransparentActivity(context,
          new String[] { Manifest.permission.READ_EXTERNAL_STORAGE });
      return false;
    }
    return true;
  }

  private Observable<Cursor> createCursorObservable(final Type type) {
    return Observable.create(
        SyncOnSubscribe.createStateful(new CursorGeneratorHelper(type), CursorFactory.created(),
            CursorCloseHolder.DISPOSE_ACTION));
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
