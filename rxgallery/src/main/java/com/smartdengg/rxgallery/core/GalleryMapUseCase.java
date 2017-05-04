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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import static com.smartdengg.rxgallery.Configuration.ALL_PICTURE_FOLDER_NAME;

/**
 * 创建时间:  2017/01/30 18:36 <br>
 * 作者:  SmartDengg <br>
 * 描述:
 */
public class GalleryMapUseCase extends GalleryUseCase<Map<String, FolderEntity>> {

  private List<ImageEntity> allPictures = new ArrayList<>();

  private GalleryMapUseCase(Context context) {
    super(context);
  }

  public static GalleryMapUseCase createdUseCase(Context context) {
    return new GalleryMapUseCase(context);
  }

  @Override public final Observable<Map<String, FolderEntity>> hunt(
      Observable<ImageEntity> entityObservable) {

    final Map<String, FolderEntity> separatedFolderMap = new LinkedHashMap<>();
    final ImageEntity[] firstImageEntity = { null };

    return entityObservable.toList()
        .flatMap(new Func1<List<ImageEntity>, Observable<ImageEntity>>() {
          @Override public Observable<ImageEntity> call(List<ImageEntity> imageEntities) {
            return Observable.from(imageEntities);
          }
        })
        .map(new Func1<ImageEntity, ImageEntity>() {
          @Override public ImageEntity call(ImageEntity imageEntity) {
            if (firstImageEntity[0] == null) firstImageEntity[0] = imageEntity;
            return imageEntity;
          }
        })
        .toMap(new Func1<ImageEntity, String>() {
          @Override public String call(ImageEntity imageEntity) {
            File parentFile = new File(imageEntity.getImagePath()).getParentFile();
            return parentFile.getAbsolutePath();
          }
        }, new Func1<ImageEntity, FolderEntity>() {
          @Override public FolderEntity call(ImageEntity imageEntity) {

            final File folderFile = new File(imageEntity.getImagePath()).getParentFile();
            final String key = folderFile.getAbsolutePath();
            final FolderEntity folderEntity = separatedFolderMap.get(key);
            if (folderEntity == null) {
              FolderEntity entity = FolderEntity.newInstance();
              entity.setFolderName(folderFile.getName());
              entity.setFolderPath(key);
              entity.setThumbPath(imageEntity.getImagePath());
              entity.addImage(imageEntity);
              return entity;
            } else {
              folderEntity.addImage(imageEntity);
            }
            return folderEntity;
          }
        }, new Func0<Map<String, FolderEntity>>() {
          @Override public Map<String, FolderEntity> call() {
            return separatedFolderMap;
          }
        })
        .map(new Func1<Map<String, FolderEntity>, Map<String, FolderEntity>>() {
          @Override public Map<String, FolderEntity> call(Map<String, FolderEntity> entityMap) {

            final List<ImageEntity> imageEntities = new ArrayList<>();
            for (Map.Entry<String, FolderEntity> entry : entityMap.entrySet()) {
              final List<ImageEntity> entities = entry.getValue().getImageEntities();
              for (ImageEntity imageEntity : entities) {
                imageEntities.add(imageEntity);
              }
            }
            FolderEntity fullFolderEntity = FolderEntity.newInstance();
            fullFolderEntity.setFolderName(ALL_PICTURE_FOLDER_NAME);
            fullFolderEntity.setFolderPath("");
            fullFolderEntity.setThumbPath(firstImageEntity[0].getImagePath());
            fullFolderEntity.setImageEntities(imageEntities);
            entityMap.put(ALL_PICTURE_FOLDER_NAME, fullFolderEntity);

            /*根据文件夹照片数量降序*/
            Map<String, FolderEntity> map = new TreeMap<>(new SortComparator(entityMap));
            for (Map.Entry<String, FolderEntity> entry : entityMap.entrySet()) {
              map.put(entry.getKey(), entry.getValue());
            }

            entityMap.clear();

            return Collections.unmodifiableMap(map);
          }
        })
        .compose(IoScheduler.<Map<String, FolderEntity>>apply());
  }

  private static final class SortComparator implements Comparator<String> {

    Map<String, FolderEntity> base;

    SortComparator(Map<String, FolderEntity> base) {
      this.base = base;
    }

    @Override public int compare(String lhs, String rhs) {

      FolderEntity lhsEntity = base.get(lhs);
      FolderEntity rhsEntity = base.get(rhs);

      int lhsCount = lhsEntity.getImageCount();
      int rhsCount = rhsEntity.getImageCount();

      return (rhsCount < lhsCount || lhsCount == rhsCount) ? -1 : 1;
    }
  }
}
