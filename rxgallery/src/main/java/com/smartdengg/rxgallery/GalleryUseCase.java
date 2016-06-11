package com.smartdengg.rxgallery;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import com.smartdengg.rxgallery.onsubscribe.InternalOnSubscribe;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryUseCase {

    private static String[] GALLERY_PROJECTION;

    private String name;
    private final CursorLoader externalLoader;
    private final CursorLoader internalLoader;
    private List<FolderEntity> items = new ArrayList<>();

    private FolderEntity folderEntity = new FolderEntity();
    private ImageEntity imageEntity = new ImageEntity();

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

    private GalleryUseCase(Context context, String name) {
        this.name = name;
        this.externalLoader = new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                GALLERY_PROJECTION[2] + " DESC");
        this.internalLoader = new CursorLoader(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                GALLERY_PROJECTION[2] + " DESC");
    }

    public static GalleryUseCase createdUseCase(Context context) {
        return createdUseCase(context, null);
    }

    public static GalleryUseCase createdUseCase(Context context, String name) {
        return new GalleryUseCase(context, name);
    }

    public Observable<List<FolderEntity>> retrieveInternalGallery() {
        return this.hunter(this.getInternalObservable());
    }

    public Observable<List<FolderEntity>> retrieveEnternalGallery() {
        return this.hunter(this.getExternalObservable());
    }

    public Observable<List<FolderEntity>> retrieveAllGallery() {
        return this.hunter(Observable.merge(this.getInternalObservable(), this.getExternalObservable()));
    }

    private Observable<List<FolderEntity>> hunter(Observable<Cursor> cursorObservable) {

        cursorObservable.takeUntil(new Func1<Cursor, Boolean>() {
            @Override
            public Boolean call(Cursor cursor) {
                return cursor.isClosed();
            }
        })
                        .onBackpressureBuffer()
                        .map(new Func1<Cursor, ImageEntity>() {
                            @Override
                            public ImageEntity call(Cursor cursor) {
                                return GalleryUseCase.this.convertToImageEntity(cursor);
                            }
                        })
                        .filter(new Func1<ImageEntity, Boolean>() {
                            @Override
                            public Boolean call(ImageEntity imageEntity) {

                                /*校验文件是否存在*/
                                File file = new File(imageEntity.getImagePath());
                                File parentFile = file.getParentFile();

                                return file.exists() && parentFile != null;
                            }
                        })
                        .groupBy(new Func1<ImageEntity, String>() {
                            @Override
                            public String call(ImageEntity imageEntity) {
                                File parentFile = new File(imageEntity.getImagePath()).getParentFile();
                                return parentFile.getAbsolutePath();
                            }
                        })
                        .map(new Func1<GroupedObservable<String, ImageEntity>, Observable<ImageEntity>>() {
                            @Override
                            public Observable<ImageEntity> call(GroupedObservable<String, ImageEntity> groupedObservable) {

                                String key = groupedObservable.getKey();
                                System.out.println(key);

                                return groupedObservable.flatMap(new Func1<ImageEntity, Observable<ImageEntity>>() {
                                    @Override
                                    public Observable<ImageEntity> call(ImageEntity imageEntity) {

                                        System.out.println(imageEntity.getImageName());
                                        return Observable.just(imageEntity);
                                    }
                                });
                            }
                        })
                        .subscribe(new Action1<Observable<ImageEntity>>() {
                            @Override
                            public void call(Observable<ImageEntity> imageEntityObservable) {

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });

        return cursorObservable.takeUntil(new Func1<Cursor, Boolean>() {
            @Override
            public Boolean call(Cursor cursor) {
                return cursor.isClosed();
            }
        })
                               .onBackpressureBuffer()
                               .map(new Func1<Cursor, ImageEntity>() {
                                   @Override
                                   public ImageEntity call(Cursor cursor) {
                                       return GalleryUseCase.this.convertToImageEntity(cursor);
                                   }
                               })
                               .filter(new Func1<ImageEntity, Boolean>() {
                                   @Override
                                   public Boolean call(ImageEntity imageEntity) {

                                       /*校验文件是否存在*/
                                       File file = new File(imageEntity.getImagePath());
                                       File parentFile = file.getParentFile();

                                       return file.exists() && parentFile != null;
                                   }
                               })
                               .doOnNext(new Action1<ImageEntity>() {
                                   @Override
                                   public void call(ImageEntity imageEntity) {

                                       File folderFile = new File(imageEntity.getImagePath()).getParentFile();
                                       FolderEntity clone = folderEntity.newInstance();
                                       clone.setFolderName(folderFile.getName());
                                       clone.setFolderPath(folderFile.getAbsolutePath());

                                       if (!items.contains(clone)) {
                                           clone.setThumbPath(imageEntity.getImagePath());
                                           clone.addImage(imageEntity);
                                           items.add(clone);
                                       } else {
                                           items.get(items.indexOf(clone))
                                                .addImage(imageEntity);
                                       }
                                   }
                               })
                               .toList()
                               .map(new Func1<List<ImageEntity>, FolderEntity>() {
                                   @Override
                                   public FolderEntity call(List<ImageEntity> imageEntities) {

                                       FolderEntity clone = folderEntity.newInstance();
                                       clone.setFolderName((name != null && !name.isEmpty()) ? name : "全部图片");
                                       clone.setFolderPath("");
                                       clone.setThumbPath(imageEntities.get(0)
                                                                       .getImagePath());
                                       clone.setImageEntities(imageEntities);

                                       return clone;
                                   }
                               })
                               .map(new Func1<FolderEntity, List<FolderEntity>>() {
                                   @Override
                                   public List<FolderEntity> call(FolderEntity folderEntity) {

                                       List<FolderEntity> galleryFolderEntities = new ArrayList<>(items.size() + 1);

                                       galleryFolderEntities.add(folderEntity);
                                       galleryFolderEntities.addAll(items);

                                       return galleryFolderEntities;
                                   }
                               })
                               .subscribeOn(Schedulers.io());

    }

    private Observable<Cursor> getInternalObservable() {
        return Observable.create(new InternalOnSubscribe(this.internalLoader, GALLERY_PROJECTION));
    }

    private Observable<Cursor> getExternalObservable() {
        return Observable.create(new InternalOnSubscribe(this.externalLoader, GALLERY_PROJECTION));

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
}
