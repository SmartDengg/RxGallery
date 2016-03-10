package com.smartdengg.smartgallery.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class ImageEntity implements Cloneable, Parcelable {

  private String imageName;
  private String imagePath;
  private long date;

  private boolean isChecked = false;

  public ImageEntity() {
  }

  public ImageEntity newInstance() {

    try {
      return (ImageEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return new ImageEntity();
  }

  public String getImageName() {
    return imageName;
  }

  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
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
    int result = imageName != null ? imageName.hashCode() : 0;
    result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
    result = 31 * result + (int) (date ^ (date >>> 32));
    result = 31 * result + (isChecked ? 1 : 0);
    return result;
  }

  @Override public String toString() {
    return "ImageEntity{" +
        "imageName='" + imageName + '\'' +
        ", imagePath='" + imagePath + '\'' +
        ", date=" + date +
        ", isChecked=" + isChecked +
        '}';
  }

  @Override public int describeContents() { return 0; }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.imageName);
    dest.writeString(this.imagePath);
    dest.writeLong(this.date);
    dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
  }

  protected ImageEntity(Parcel in) {
    this.imageName = in.readString();
    this.imagePath = in.readString();
    this.date = in.readLong();
    this.isChecked = in.readByte() != 0;
  }

  public static final Creator<ImageEntity> CREATOR = new Creator<ImageEntity>() {
    public ImageEntity createFromParcel(Parcel source) {return new ImageEntity(source);}

    public ImageEntity[] newArray(int size) {return new ImageEntity[size];}
  };
}
