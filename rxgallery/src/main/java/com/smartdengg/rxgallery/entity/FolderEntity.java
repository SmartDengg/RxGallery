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

package com.smartdengg.rxgallery.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class FolderEntity implements Cloneable, Comparable<FolderEntity> {

  private String folderName = "";
  private String folderPath = "";
  private String thumbPath = "";

  private List<ImageEntity> imageEntities;

  public FolderEntity newInstance() {
    try {
      return (FolderEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return new FolderEntity();
  }

  public String getFolderName() {
    return folderName;
  }

  public void setFolderName(String folderName) {
    this.folderName = folderName;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public void setFolderPath(String folderPath) {
    this.folderPath = folderPath;
  }

  public String getThumbPath() {
    return thumbPath;
  }

  public void setThumbPath(String thumbPath) {
    this.thumbPath = thumbPath;
  }

  public int getImageCount() {
    return (this.imageEntities != null) ? (this.imageEntities.size()) : 0;
  }

  public List<ImageEntity> getImageEntities() {
    return imageEntities;
  }

  public void setImageEntities(List<ImageEntity> imageEntities) {
    this.imageEntities = imageEntities;
  }

  public void addImage(ImageEntity imageEntity) {

    if (this.imageEntities == null) this.imageEntities = new ArrayList<>();
    this.imageEntities.add(imageEntity);
  }

  @Override public boolean equals(Object o) {

    if (o == null || getClass() != o.getClass()) return false;

    FolderEntity that = (FolderEntity) o;
    return this.folderName.equalsIgnoreCase(that.getFolderName()) &&  //
        this.folderPath.equalsIgnoreCase(that.getFolderPath());
  }

  @Override public String toString() {
    return "FolderEntity{" +
        "folderName='" + folderName + '\'' +
        ", folderPath='" + folderPath + '\'' +
        ", thumbPath='" + thumbPath + '\'' +
        ", imageEntities=" + imageEntities +
        '}';
  }

  @SuppressWarnings("all") @Override public int compareTo(FolderEntity another) {
    if (another == null) throw new NullPointerException("another == null");

    if (this.getImageEntities().size() - another.getImageEntities().size() >= 0) {
      return 1;
    } else {
      return -1;
    }
  }
}