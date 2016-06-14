package com.smartdengg.rxgallery.usecase;

import android.content.Context;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryListUseCase extends GalleryUseCase<List<FolderEntity>> {

    private List<FolderEntity> items = new ArrayList<>();

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

        return cursorObservable.doOnNext(new Action1<ImageEntity>() {
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

                                       List<FolderEntity> folderEntityList = new ArrayList<>(items.size() + 1);

                                       folderEntityList.add(folderEntity);
                                       folderEntityList.addAll(items);

                                       Collections.sort(folderEntityList, new ValueComparator());

                                       return folderEntityList;
                                   }
                               })
                               .subscribeOn(Schedulers.io());
    }

    private static final class ValueComparator implements Comparator<FolderEntity> {

        @Override
        public int compare(FolderEntity lhs, FolderEntity rhs) {

            if (lhs.getImageCount() - rhs.getImageCount() >= 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

}
