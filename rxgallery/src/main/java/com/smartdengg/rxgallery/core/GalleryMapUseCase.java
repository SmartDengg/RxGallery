package com.smartdengg.rxgallery.core;

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
    @Override
    protected Observable<Map<String, FolderEntity>> hunter(Observable<ImageEntity> cursorObservable) {

        return cursorObservable.groupBy(new GroupByFunc())
                               .concatMap(new ContactMapFunc(this.folderEntity, allPictures, folderListMap))
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

                                       /*根据文件夹照片数量降序*/
                                       Map<String, FolderEntity> folderEntityMap = new TreeMap<>(new ValueComparator(entityMap));
                                       for (Map.Entry<String, FolderEntity> entry : folderListMap.entrySet()) {
                                           folderEntityMap.put(entry.getKey(), entry.getValue());
                                       }

                                       //@formatter:on
                                       GalleryMapUseCase.this.folderListMap.clear();

                                       return Collections.unmodifiableMap(folderEntityMap);
                                   }
                               })
                               .compose(IoScheduler.<Map<String, FolderEntity>>apply());
    }

    //@formatter:on
    private static final class GroupByFunc implements Func1<ImageEntity, String> {

        @Override
        public String call(ImageEntity imageEntity) {
            File parentFile = new File(imageEntity.getImagePath()).getParentFile();
            return parentFile.getAbsolutePath();
        }
    }

    private static final class ContactMapFunc
            implements Func1<GroupedObservable<String, ImageEntity>, Observable<Map<String, FolderEntity>>> {

        private FolderEntity instance;
        private List<ImageEntity> pictures;
        private Map<String, FolderEntity> folderListMap;

        public ContactMapFunc(FolderEntity instance, List<ImageEntity> pictures, Map<String, FolderEntity> folderListMap) {
            this.instance = instance;
            this.pictures = pictures;
            this.folderListMap = folderListMap;
        }

        @Override
        public Observable<Map<String, FolderEntity>> call(final GroupedObservable<String, ImageEntity> groupedObservable) {

            return groupedObservable.map(new Func1<ImageEntity, Map<String, FolderEntity>>() {
                @Override
                public Map<String, FolderEntity> call(ImageEntity imageEntity) {

                    /*All pictures*/
                    pictures.add(imageEntity);

                    String key = groupedObservable.getKey();
                    File folderFile = new File(imageEntity.getImagePath()).getParentFile();

                    if (!folderListMap.containsKey(key)) {
                        FolderEntity clone = instance.newInstance();
                        clone.setFolderName(folderFile.getName());
                        clone.setFolderPath(folderFile.getAbsolutePath());
                        clone.setThumbPath(imageEntity.getImagePath());
                        clone.addImage(imageEntity);
                        folderListMap.put(key, clone);
                    } else {
                        folderListMap.get(key)
                                     .addImage(imageEntity);
                    }

                    return folderListMap;
                }
            });
        }
    }

    //@formatter:off
    private static final class ValueComparator implements Comparator<String> {

        Map<String, FolderEntity> base;

        public ValueComparator(Map<String, FolderEntity> base) {
            this.base = base;
        }

        @Override
        public int compare(String lhs, String rhs) {

            FolderEntity lhsEntity = base.get(lhs);
            FolderEntity rhsEntity = base.get(rhs);

            int lhsCount = lhsEntity.getImageCount();
            int rhsCount = rhsEntity.getImageCount();

            return (lhsCount == rhsCount) ? rhsEntity.getFolderName().compareTo(lhsEntity.getFolderName()) : rhsCount - lhsCount;
        }
    }
}
