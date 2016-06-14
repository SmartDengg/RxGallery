package com.smartdengg.rxgallery.usecase;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import com.smartdengg.rxgallery.onsubscribe.InternalOnSubscribe;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by SmartDengg on 2016/3/5.
 */
abstract class GalleryUseCase<T> {

    static String[] GALLERY_PROJECTION;
    static String DEFAULT_NAME = "全部图片";

    String name;
    final CursorLoader externalLoader;
    final CursorLoader internalLoader;

    FolderEntity folderEntity = new FolderEntity();
    ImageEntity imageEntity = new ImageEntity();
    Map<String, FolderEntity> folderListMap = new HashMap<>();

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            GalleryUseCase.GALLERY_PROJECTION = new String[] {
                    MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT
            };
        } else {
            GalleryUseCase.GALLERY_PROJECTION = new String[] {
                    MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATE_MODIFIED
            };
        }
    }

    /*package*/GalleryUseCase(Context context, String name) {
        this.name = name;
        this.externalLoader = new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                GALLERY_PROJECTION[2] + " DESC");
        this.internalLoader = new CursorLoader(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                GALLERY_PROJECTION[2] + " DESC");
    }

    public Observable<T> retrieveInternalGallery() {
        return this.hunter(this.transferObservable(this.getInternalObservable()));
    }

    public Observable<T> retrieveExternalGallery() {
        return this.hunter(this.transferObservable(this.getExternalObservable()));
    }

    public Observable<T> retrieveAllGallery() {
        return this.hunter(this.transferObservable(Observable.merge(this.getInternalObservable(), this.getExternalObservable())));
    }

    private Observable<Cursor> getInternalObservable() {
        return Observable.create(new InternalOnSubscribe(this.internalLoader, GALLERY_PROJECTION));
    }

    private Observable<Cursor> getExternalObservable() {
        return Observable.create(new InternalOnSubscribe(this.externalLoader, GALLERY_PROJECTION));
    }

    private Observable<ImageEntity> transferObservable(Observable<Cursor> cursorObservable) {

        return cursorObservable.compose(TAKEUNTIL_TRANSFORMER)
                               .onBackpressureBuffer()
                               .map(CONVERT_FUNCTION)
                               .filter(FILTER_FUNCTION);
    }

    private ImageEntity convertToImageEntity(Cursor cursor) {
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[0]));
        String imageName = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[1]));
        long addDate = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[2]));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[3]));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[4]));
        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[5]));

        long size = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[6]));
        long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[7]));

        ImageEntity clone = imageEntity.newInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            String width = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[8]));
            String Height = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[9]));
            clone.setWidth(width);
            clone.setHeight(Height);
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

    private static final Observable.Transformer<Cursor, Cursor> TAKEUNTIL_TRANSFORMER = new Observable.Transformer<Cursor, Cursor>() {
        @Override
        public Observable<Cursor> call(Observable<Cursor> cursorObservable) {
            return cursorObservable.takeUntil(new Func1<Cursor, Boolean>() {
                @Override
                public Boolean call(Cursor cursor) {
                    return cursor.isClosed();
                }
            });
        }
    };

    private final Func1<Cursor, ImageEntity> CONVERT_FUNCTION = new Func1<Cursor, ImageEntity>() {
        @Override
        public ImageEntity call(Cursor cursor) {
            return GalleryUseCase.this.convertToImageEntity(cursor);
        }
    };

    private final Func1<ImageEntity, Boolean> FILTER_FUNCTION = new Func1<ImageEntity, Boolean>() {
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
