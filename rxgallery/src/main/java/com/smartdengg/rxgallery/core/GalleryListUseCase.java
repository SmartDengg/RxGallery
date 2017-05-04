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

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import rx.Observable;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

import static com.smartdengg.rxgallery.Configuration.ALL_PICTURE_FOLDER_NAME;

/**
 * 创建时间:  2017/01/30 18:57 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class GalleryListUseCase extends GalleryUseCase<List<FolderEntity>> {

  private List<ImageEntity> imageEntities = new ArrayList<>();

  private GalleryListUseCase(Context context) {
    super(context);
  }

  public static GalleryListUseCase createdUseCase(Context context) {
    return new GalleryListUseCase(context);
  }

  @Override
  public final Observable<List<FolderEntity>> hunt(Observable<ImageEntity> entityObservable) {

    return entityObservable.collect(new Func0<List<FolderEntity>>() {
      @Override public List<FolderEntity> call() {
        return new ArrayList<>();
      }
    }, new CollectionAction(imageEntities))
        .map(new Func1<List<FolderEntity>, List<FolderEntity>>() {
          @Override public List<FolderEntity> call(List<FolderEntity> folderEntities) {

            FolderEntity entity = FolderEntity.newInstance();
            entity.setFolderName(ALL_PICTURE_FOLDER_NAME);
            entity.setFolderPath("");
            entity.setThumbPath(imageEntities.get(0).getImagePath());
            entity.setImageEntities(imageEntities);
            folderEntities.add(entity);

            Collections.sort(folderEntities, new SortComparator());

            return folderEntities;
          }
        })
        .compose(IoScheduler.<List<FolderEntity>>apply());
  }

  private static final class CollectionAction implements Action2<List<FolderEntity>, ImageEntity> {

    private List<ImageEntity> imageEntities;

    private CollectionAction(List<ImageEntity> imageEntities) {
      this.imageEntities = imageEntities;
    }

    @Override public void call(List<FolderEntity> folderEntities, ImageEntity imageEntity) {

      imageEntities.add(imageEntity);

      File folderFile = new File(imageEntity.getImagePath()).getParentFile();
      FolderEntity entity = FolderEntity.newInstance();
      entity.setFolderName(folderFile.getName());
      entity.setFolderPath(folderFile.getAbsolutePath());

      if (!folderEntities.contains(entity)) {
        entity.setThumbPath(imageEntity.getImagePath());
        entity.addImage(imageEntity);
        folderEntities.add(entity);
      } else {
        folderEntities.get(folderEntities.indexOf(entity)).addImage(imageEntity);
      }
    }
  }

  private static final class SortComparator implements Comparator<FolderEntity> {

    @Override public int compare(FolderEntity lhs, FolderEntity rhs) {

      int lhsCount = lhs.getImageCount();
      int rhsCount = rhs.getImageCount();

      return (rhsCount < lhsCount) ? -1 : ((lhsCount == rhsCount) ? 0 : 1);
    }
  }
}
