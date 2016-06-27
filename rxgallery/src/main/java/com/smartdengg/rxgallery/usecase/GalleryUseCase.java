package com.smartdengg.rxgallery.usecase;

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
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
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

    @Retention(value = RetentionPolicy.CLASS)
    @Target(value = ElementType.PARAMETER)
    @IntDef(value = { TYPE_INTERNAL, TYPE_EXTERNAL })
    private @interface Type {}

    static String[] GALLERY_PROJECTION;
    static String DEFAULT_NAME = "全部图片";

    String name;
    final CursorLoader externalLoader;
    final CursorLoader internalLoader;

    FolderEntity folderEntity = new FolderEntity();
    Map<String, FolderEntity> folderListMap = new HashMap<>();

    private Context context;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            GalleryUseCase.GALLERY_PROJECTION =
                    new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED,
                            MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE,
                            MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.WIDTH,
                            MediaStore.Images.Media.HEIGHT };
        } else {
            GalleryUseCase.GALLERY_PROJECTION =
                    new String[] { MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED,
                            MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE,
                            MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED };
        }
    }

    /*package*/GalleryUseCase(Context context, String name) {
        this.context = context;
        this.name = name;
        this.externalLoader = new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                GALLERY_PROJECTION[2] + " DESC");
        this.internalLoader = new CursorLoader(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                GALLERY_PROJECTION[2] + " DESC");
    }

    public Observable<T> retrieveInternalGallery() {
        return this.hunter(this.transferObservable(this.getObservable(TYPE_INTERNAL)));
    }

    public Observable<T> retrieveExternalGallery() {

        if (!Utils.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            throw new RuntimeException("missing permission: 'android.permission.READ_EXTERNAL_STORAGE' in your Manifest.xml");
        }

        return this.hunter(this.transferObservable(this.getObservable(TYPE_EXTERNAL)));
    }

    public Observable<T> retrieveAllGallery() {

        if (!Utils.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            throw new RuntimeException("miss 'android.permission.READ_EXTERNAL_STORAGE' in your Manifest.xml");
        }

        return this.hunter(this.transferObservable(Observable.merge(this.getObservable(TYPE_INTERNAL), this.getObservable(TYPE_EXTERNAL))));
    }

    private Observable<Cursor> getObservable(@Type final int type) {

        return Observable.defer(new Func0<Observable<Cursor>>() {
            @Override
            public Observable<Cursor> call() {
                return Observable.using(new Func0<Cursor>() {
                    @Override
                    public Cursor call() {
                        switch (type) {
                            case TYPE_INTERNAL:
                                return GalleryUseCase.this.internalLoader.loadInBackground();

                            case TYPE_EXTERNAL:
                                return GalleryUseCase.this.externalLoader.loadInBackground();
                        }
                        return null;
                    }
                }, new CursorFactory(), DISPOSE_ACTION);
            }
        });
    }

    private Observable<ImageEntity> transferObservable(Observable<Cursor> cursorObservable) {
        return cursorObservable.compose(TRANSFORMER);
    }

    private static ImageEntity convertToImageEntity(Cursor cursor, ImageEntity parent) {
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[0]));
        String imageName = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[1]));
        long addDate = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[2]));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[3]));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[4]));
        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[5]));

        long size = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[6]));
        long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[7]));

        ImageEntity clone = parent.newInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            String width = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[8]));
            String Height = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[9]));
            clone.setWidth(width);
            clone.setHeight(Height);
        } else {
            clone.setWidth("0");
            clone.setHeight("0");
        }

        clone.setImagePath(imagePath);
        clone.setImageName(imageName);
        clone.setAddDate(addDate);
        clone.setId(id);
        clone.setTitle(title);
        clone.setMimeType(mimeType);
        clone.setSize(size);
        clone.setModifyDate(modifyDate);
        return clone;
    }

    private static Action1<Cursor> DISPOSE_ACTION = new Action1<Cursor>() {
        @Override
        public void call(Cursor cursor) {
            if (!cursor.isClosed()) cursor.close();
        }
    };

    private static final Observable.Transformer<Cursor, ImageEntity> TRANSFORMER = new Observable.Transformer<Cursor, ImageEntity>() {
        @Override
        public Observable<ImageEntity> call(Observable<Cursor> cursorObservable) {
            return cursorObservable.takeUntil(new Func1<Cursor, Boolean>() {
                @Override
                public Boolean call(Cursor cursor) {
                    return cursor.isClosed();
                }
            })
                                   .onBackpressureBuffer()
                                   .map(CONVERT_FUNCTION)
                                   .filter(FILTER_FUNCTION);
        }
    };

    private static final Func1<Cursor, ImageEntity> CONVERT_FUNCTION = new Func1<Cursor, ImageEntity>() {
        @Override
        public ImageEntity call(Cursor cursor) {
            return GalleryUseCase.convertToImageEntity(cursor, new ImageEntity());
        }
    };

    private static final Func1<ImageEntity, Boolean> FILTER_FUNCTION = new Func1<ImageEntity, Boolean>() {
        @Override
        public Boolean call(ImageEntity imageEntity) {

            /*校验文件是否存在*/
            File file = new File(imageEntity.getImagePath());
            File parentFile = file.getParentFile();

            return file.exists() && parentFile != null;
        }
    };

    protected abstract Observable<T> hunter(Observable<ImageEntity> cursorObservable);
}
