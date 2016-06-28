package com.smartdengg.rxgallery.core;

import android.database.Cursor;
import android.os.Build;
import com.smartdengg.rxgallery.entity.ImageEntity;
import java.io.File;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Joker on 2016/6/28.
 */
class CursorTransformer implements Observable.Transformer<Cursor, ImageEntity> {

    private String[] galleryProjection;
    private ImageEntity parent;

    public CursorTransformer(String[] galleryProjection, ImageEntity parent) {
        this.galleryProjection = galleryProjection;
        this.parent = parent;
    }

    @Override
    public Observable<ImageEntity> call(Observable<Cursor> cursorObservable) {

        return cursorObservable.takeUntil(STOP_PREDICATE_FUNCTION)
                               .onBackpressureBuffer()
                               .map(CONVERT_FUNCTION)
                               .filter(FILTER_FUNCTION);
    }

    private static final Func1<Cursor, Boolean> STOP_PREDICATE_FUNCTION = new Func1<Cursor, Boolean>() {
        @Override
        public Boolean call(Cursor cursor) {
            return cursor.isClosed();
        }
    };

    private final Func1<Cursor, ImageEntity> CONVERT_FUNCTION = new Func1<Cursor, ImageEntity>() {
        @Override
        public ImageEntity call(Cursor cursor) {
            return CursorTransformer.this.convertToImageEntity(cursor);
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

    private ImageEntity convertToImageEntity(Cursor cursor) {
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[0]));
        String imageName = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[1]));
        long addDate = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[2]));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[3]));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[4]));
        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[5]));

        long size = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[6]));
        long modifyDate = cursor.getLong(cursor.getColumnIndexOrThrow(galleryProjection[7]));

        ImageEntity clone = parent.newInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            String width = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[8]));
            String Height = cursor.getString(cursor.getColumnIndexOrThrow(galleryProjection[9]));
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
}
