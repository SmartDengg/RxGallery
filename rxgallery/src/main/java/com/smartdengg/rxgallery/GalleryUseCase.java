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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
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
    private List<ImageEntity> allPictures = new ArrayList<>();

    private Map<String, FolderEntity> folderListMap = new TreeMap<>();

    private SortedSet<Map.Entry<String, FolderEntity>> sortedSet = new TreeSet<>(new Comparator<Map.Entry<String, FolderEntity>>() {
        @Override
        public int compare(Map.Entry<String, FolderEntity> e1, Map.Entry<String, FolderEntity> e2) {

            return e1.getValue()
                     .getImageCount() - e2.getValue()
                                          .getImageCount();
        }
    });

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
        return this.hunterList(this.getInternalObservable());
    }

    public Observable<List<FolderEntity>> retrieveEnternalGallery() {
        return this.hunterList(this.getExternalObservable());
    }

    public Observable<List<FolderEntity>> retrieveAllGallery() {
        return this.hunterList(Observable.merge(this.getInternalObservable(), this.getExternalObservable()));
    }

    private Observable<List<FolderEntity>> hunterList(Observable<Cursor> cursorObservable) {

        hunterMap(cursorObservable);

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

    @SuppressWarnings("all")
    private Observable<Map<String, FolderEntity>> hunterMap(Observable<Cursor> cursorObservable) {

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
                        .concatMap(new Func1<GroupedObservable<String, ImageEntity>, Observable<Map<String, FolderEntity>>>() {
                            @Override
                            public Observable<Map<String, FolderEntity>> call(final GroupedObservable<String, ImageEntity> groupedObservable) {

                                return groupedObservable.map(new Func1<ImageEntity, Map<String, FolderEntity>>() {
                                    @Override
                                    public Map<String, FolderEntity> call(ImageEntity imageEntity) {

                                        /*全部图片集合*/
                                        GalleryUseCase.this.allPictures.add(imageEntity);

                                        String key = groupedObservable.getKey();
                                        File folderFile = new File(imageEntity.getImagePath()).getParentFile();

                                        if (!folderListMap.containsKey(key)) {

                                            FolderEntity folderEntity = GalleryUseCase.this.folderEntity.newInstance();
                                            folderEntity.setFolderName(folderFile.getName());
                                            folderEntity.setFolderPath(folderFile.getAbsolutePath());
                                            folderEntity.setThumbPath(imageEntity.getImagePath());
                                            folderEntity.addImage(imageEntity);

                                            folderListMap.put(key, folderEntity);
                                        } else {
                                            folderListMap.get(key)
                                                         .addImage(imageEntity);
                                        }

                                        return folderListMap;
                                    }
                                });
                            }
                        })
                        .last()
                        .map(new Func1<Map<String, FolderEntity>, Map<String, FolderEntity>>() {
                            @Override
                            public Map<String, FolderEntity> call(Map<String, FolderEntity> folderEntityMap) {

                                FolderEntity allFolderEntity = GalleryUseCase.this.folderEntity.newInstance();
                                allFolderEntity.setFolderName((name != null && !name.isEmpty()) ? name : "全部图片");
                                allFolderEntity.setFolderPath("");
                                allFolderEntity.setThumbPath(allPictures.get(0)
                                                                        .getImagePath());
                                allFolderEntity.setImageEntities(allPictures);

                                folderEntityMap.put((name != null && !name.isEmpty()) ? name : "全部图片", allFolderEntity);

                                sortedSet.addAll(folderEntityMap.entrySet());
                                return Collections.unmodifiableMap((Map<? extends String, ? extends FolderEntity>) sortedSet);
                            }
                        })
                        .subscribe(new Action1<Map<String, FolderEntity>>() {
                            @Override
                            public void call(Map<String, FolderEntity> folderEntityMap) {

                                for (Map.Entry<String, FolderEntity> entry : folderEntityMap.entrySet()) {
                                    System.out.println(entry.getKey() + " : " + entry.getValue());
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });

        return null;
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


    private <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                int res = e1.getValue()
                            .compareTo(e2.getValue());
                return res != 0 ? res : 1;
            }
        });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
