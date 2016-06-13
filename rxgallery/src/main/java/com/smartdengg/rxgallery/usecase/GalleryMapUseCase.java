package com.smartdengg.rxgallery.usecase;

import android.content.Context;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryMapUseCase extends GalleryUseCase<Map<String, FolderEntity>> {

    private List<ImageEntity> allPictures = new ArrayList<>();

    private GalleryMapUseCase(Context context, String name) {
        super(context, name);
    }

    public static GalleryMapUseCase createdUseCase(Context context) {
        return createdUseCase(context, null);
    }

    public static GalleryMapUseCase createdUseCase(Context context, String name) {
        return new GalleryMapUseCase(context, name);
    }

    //@formatter:off
    @SuppressWarnings("all")
    @Override
    protected Observable<Map<String, FolderEntity>> hunter(Observable<ImageEntity> cursorObservable) {

        return cursorObservable.groupBy(new Func1<ImageEntity, String>() {
                                   @Override
                                   public String call(ImageEntity imageEntity) {
                                       File parentFile = new File(imageEntity.getImagePath()).getParentFile();
                                       return parentFile.getAbsolutePath();
                                   }
                               })
                               .concatMap(new Func1<GroupedObservable<String, ImageEntity>, Observable<Map<String, FolderEntity>>>() {
                                   @Override
                                   public Observable<Map<String, FolderEntity>> call(
                                           final GroupedObservable<String, ImageEntity> groupedObservable) {

                                       return groupedObservable.map(new Func1<ImageEntity, Map<String, FolderEntity>>() {
                                           @Override
                                           public Map<String, FolderEntity> call(ImageEntity imageEntity) {

                                               /*全部图片集合*/
                                               GalleryMapUseCase.this.allPictures.add(imageEntity);

                                               String key = groupedObservable.getKey();
                                               File folderFile = new File(imageEntity.getImagePath()).getParentFile();

                                               if (!folderListMap.containsKey(key)) {

                                                   FolderEntity folderEntity = GalleryMapUseCase.this.folderEntity.newInstance();
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
                                   public Map<String, FolderEntity> call(Map<String, FolderEntity> entityMap) {

                                       FolderEntity allFolderEntity = GalleryMapUseCase.this.folderEntity.newInstance();
                                       allFolderEntity.setFolderName((name != null && !name.isEmpty()) ? name : DEFAULT_NAME);
                                       allFolderEntity.setFolderPath("");
                                       allFolderEntity.setThumbPath(allPictures.get(0).getImagePath());
                                       allFolderEntity.setImageEntities(allPictures);
                                       entityMap.put((name != null && !name.isEmpty()) ? name : DEFAULT_NAME, allFolderEntity);

                                       /*根据文件夹照片数量排序*/
                                       Map<String, FolderEntity> folderEntityMap =
                                               new TreeMap<String, FolderEntity>(new ValueComparator(entityMap));
                                       for (Map.Entry<String, FolderEntity> entry : folderListMap.entrySet()) {
                                           folderEntityMap.put(entry.getKey(), entry.getValue());
                                       }

                                       return Collections.unmodifiableMap(folderEntityMap);
                                   }
                               })
                               .subscribeOn(Schedulers.io());
    }

    private static final class ValueComparator implements Comparator<String> {

        Map<String, FolderEntity> base;

        public ValueComparator(Map<String, FolderEntity> base) {
            this.base = base;
        }

        @Override
        public int compare(String lhs, String rhs) {

            FolderEntity lhsEntity = base.get(lhs);
            FolderEntity rhsEntity = base.get(rhs);

            if (lhsEntity.getImageCount() - rhsEntity.getImageCount() >= 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
