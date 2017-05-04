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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class ImageEntity implements Cloneable, Parcelable {

  public static final Creator<ImageEntity> CREATOR = new Creator<ImageEntity>() {
    @Override public ImageEntity createFromParcel(Parcel source) {
      return new ImageEntity(source);
    }

    @Override public ImageEntity[] newArray(int size) {
      return new ImageEntity[size];
    }
  };
  private String imagePath;
  private String imageName;
  private long id;
  private long addDate;
  private String title;
  private String mimeType;
  private String width;
  private String height;
  private long size;
  private long modifyDate;
  private boolean isChecked = false;

  protected ImageEntity(Parcel in) {
    this.imagePath = in.readString();
    this.imageName = in.readString();
    this.id = in.readLong();
    this.addDate = in.readLong();
    this.title = in.readString();
    this.mimeType = in.readString();
    this.width = in.readString();
    this.height = in.readString();
    this.size = in.readLong();
    this.modifyDate = in.readLong();
    this.isChecked = in.readByte() != 0;
  }

  private ImageEntity() {
  }

  static ImageEntity newInstance() {
    return new ImageEntity();
  }

  public String getImagePath() {
    return imagePath;
  }

  void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  public String getImageName() {
    return imageName;
  }

  void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public long getId() {
    return id;
  }

  void setId(long id) {
    this.id = id;
  }

  public long getAddDate() {
    return addDate;
  }

  void setAddDate(long addDate) {
    this.addDate = addDate;
  }

  public String getTitle() {
    return title;
  }

  void setTitle(String title) {
    this.title = title;
  }

  public String getMimeType() {
    return mimeType;
  }

  void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getWidth() {
    return width;
  }

  void setWidth(String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  void setHeight(String height) {
    this.height = height;
  }

  public long getSize() {
    return size;
  }

  void setSize(long size) {
    this.size = size;
  }

  public long getModifyDate() {
    return modifyDate;
  }

  void setModifyDate(long modifyDate) {
    this.modifyDate = modifyDate;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setChecked(boolean checked) {
    isChecked = checked;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ImageEntity that = (ImageEntity) o;

    return this.imageName.equalsIgnoreCase(that.getImageName()) &&  //
        this.imagePath.equalsIgnoreCase(that.getImagePath());
  }

  @Override public int hashCode() {
    int result = imagePath != null ? imagePath.hashCode() : 0;
    result = 31 * result + (imageName != null ? imageName.hashCode() : 0);
    result = 31 * result + (int) (id ^ (id >>> 32));
    result = 31 * result + (int) (addDate ^ (addDate >>> 32));
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
    result = 31 * result + (width != null ? width.hashCode() : 0);
    result = 31 * result + (height != null ? height.hashCode() : 0);
    result = 31 * result + (int) (size ^ (size >>> 32));
    result = 31 * result + (int) (modifyDate ^ (modifyDate >>> 32));
    result = 31 * result + (isChecked ? 1 : 0);
    return result;
  }

  @Override public String toString() {
    return "ImageEntity{" +
        "imagePath='" + imagePath + '\'' +
        ", imageName='" + imageName + '\'' +
        ", id=" + id +
        ", addDate=" + addDate +
        ", title='" + title + '\'' +
        ", mimeType='" + mimeType + '\'' +
        ", width='" + width + '\'' +
        ", height='" + height + '\'' +
        ", size=" + size +
        ", modifyDate=" + modifyDate +
        ", isChecked=" + isChecked +
        '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.imagePath);
    dest.writeString(this.imageName);
    dest.writeLong(this.id);
    dest.writeLong(this.addDate);
    dest.writeString(this.title);
    dest.writeString(this.mimeType);
    dest.writeString(this.width);
    dest.writeString(this.height);
    dest.writeLong(this.size);
    dest.writeLong(this.modifyDate);
    dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
  }
}
