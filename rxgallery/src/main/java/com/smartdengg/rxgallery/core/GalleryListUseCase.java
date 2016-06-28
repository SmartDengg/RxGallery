package com.smartdengg.rxgallery.core;

import android.content.Context;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rx.Observable;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryListUseCase extends GalleryUseCase<List<FolderEntity>> {

    private List<ImageEntity> imageEntities = new ArrayList<>();

    private GalleryListUseCase(Context context, String name) {
        super(context, name);
    }

    public static GalleryListUseCase createdUseCase(Context context) {
        return createdUseCase(context, null);
    }

    public static GalleryListUseCase createdUseCase(Context context, String name) {
        return new GalleryListUseCase(context, name);
    }

    //@formatter:on
    @Override
    protected Observable<List<FolderEntity>> hunter(Observable<ImageEntity> cursorObservable) {

        return cursorObservable.collect(new Func0<List<FolderEntity>>() {
            @Override
            public List<FolderEntity> call() {
                return new ArrayList<>();
            }
        }, new CollectionAction(folderEntity, imageEntities))
                               .map(new Func1<List<FolderEntity>, List<FolderEntity>>() {
                                   @Override
                                   public List<FolderEntity> call(List<FolderEntity> folderEntities) {

                                       //@formatter:off
                                       FolderEntity clone = folderEntity.newInstance();
                                       clone.setFolderName((name != null && !name.isEmpty()) ? name : "全部图片");
                                       clone.setFolderPath("");
                                       clone.setThumbPath(imageEntities.get(0).getImagePath());
                                       clone.setImageEntities(imageEntities);

                                       folderEntities.add(clone);
                                       Collections.sort(folderEntities, new ValueComparator());

                                       return folderEntities;
                                   }
                               })
                               .compose(IoScheduler.<List<FolderEntity>>apply());
    }

    private static final class CollectionAction implements Action2<List<FolderEntity>, ImageEntity> {

        private FolderEntity folderEntity;
        private List<ImageEntity> imageEntities;

        public CollectionAction(FolderEntity folderEntity, List<ImageEntity> imageEntities) {
            this.folderEntity = folderEntity;
            this.imageEntities = imageEntities;
        }

        @Override
        public void call(List<FolderEntity> folderEntities, ImageEntity imageEntity) {

            imageEntities.add(imageEntity);

            File folderFile = new File(imageEntity.getImagePath()).getParentFile();
            FolderEntity clone = folderEntity.newInstance();
            clone.setFolderName(folderFile.getName());
            clone.setFolderPath(folderFile.getAbsolutePath());

            if (!folderEntities.contains(clone)) {
                clone.setThumbPath(imageEntity.getImagePath());
                clone.addImage(imageEntity);
                folderEntities.add(clone);
            } else {
                folderEntities.get(folderEntities.indexOf(clone))
                              .addImage(imageEntity);
            }
        }
    }

    private static final class ValueComparator implements Comparator<FolderEntity> {

        @Override
        public int compare(FolderEntity lhs, FolderEntity rhs) {

            int lhsCount = lhs.getImageCount();
            int rhsCount = rhs.getImageCount();

            return (lhsCount == rhsCount) ? lhs.getFolderName()
                                               .compareTo(rhs.getFolderName()) : rhsCount - lhsCount;
        }
    }

}
