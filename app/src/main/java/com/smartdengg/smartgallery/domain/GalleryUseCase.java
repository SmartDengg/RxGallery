package com.smartdengg.smartgallery.domain;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import com.smartdengg.smartgallery.entity.FolderEntity;
import com.smartdengg.smartgallery.entity.ImageEntity;
import com.smartdengg.smartgallery.manager.SchedulersCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryUseCase extends UseCase<List<FolderEntity>> {

  private final String[] GALLERY_PROJECTION = {
      MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED,
      MediaStore.Images.Media._ID
  };

  private final CursorLoader cursorLoader;
  private List<FolderEntity> items = new ArrayList<>();

  private FolderEntity folderEntity = new FolderEntity();
  private ImageEntity imageEntity = new ImageEntity();

  private GalleryUseCase(Context context) {
    this.cursorLoader =
        new CursorLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, GALLERY_PROJECTION, null, null,
                         GALLERY_PROJECTION[2] + " DESC");
  }

  public static GalleryUseCase createdUseCase(Context context) {
    return new GalleryUseCase(context);
  }

  @Override protected Observable<List<FolderEntity>> interactor() {

    return Observable.create(new Observable.OnSubscribe<Cursor>() {
      @Override public void call(Subscriber<? super Cursor> subscriber) {

        final Cursor cursor = cursorLoader.loadInBackground();

        subscriber.add(Subscriptions.create(new Action0() {
          @Override public void call() {
            if (cursorLoader.isStarted() && cursor != null && !cursor.isClosed()) {
              cursorLoader.cancelLoad();
              cursor.close();
            }
          }
        }));

        if (!subscriber.isUnsubscribed()) {
          if (cursor == null) {
            Observable.error(new NullPointerException("cursor must not be null"));
          } else {
            while (cursor.moveToNext()) {
              if (cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[0])).endsWith(".gif")) {
                continue;
              }
              subscriber.onNext(cursor);
            }
            cursor.close();
            subscriber.onCompleted();
          }
        }
      }
    }).map(new Func1<Cursor, ImageEntity>() {
      @Override public ImageEntity call(Cursor cursor) {

        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[0]));
        String imageName = cursor.getString(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[1]));
        long addDate = cursor.getLong(cursor.getColumnIndexOrThrow(GALLERY_PROJECTION[2]));

        ImageEntity clone = imageEntity.newInstance();

        clone.setImagePath(imagePath);
        clone.setImageName(imageName);
        clone.setDate(addDate);

        return clone;
      }
    }).filter(new Func1<ImageEntity, Boolean>() {
      @Override public Boolean call(ImageEntity imageEntity) {

        /*校验文件是否存在*/
        File file = new File(imageEntity.getImagePath());
        File parentFile = file.getParentFile();

        return file.exists() && parentFile != null;
      }
    }).doOnNext(new Action1<ImageEntity>() {
      @Override public void call(ImageEntity imageEntity) {

        File folderFile = new File(imageEntity.getImagePath()).getParentFile();
        FolderEntity clone = folderEntity.newInstance();
        clone.setFolderName(folderFile.getName());
        clone.setFolderPath(folderFile.getAbsolutePath());

        if (!items.contains(clone)) {
          clone.setThumbPath(imageEntity.getImagePath());
          clone.addImage(imageEntity);
          items.add(clone);
        } else {
          items.get(items.indexOf(clone)).addImage(imageEntity);
        }
      }
    }).toList().map(new Func1<List<ImageEntity>, FolderEntity>() {
      @Override public FolderEntity call(List<ImageEntity> imageEntities) {

        FolderEntity clone = folderEntity.newInstance();
        clone.setFolderName("所有图片");
        clone.setFolderPath("");
        clone.setChecked(true);
        clone.setThumbPath(imageEntities.get(0).getImagePath());
        clone.setImageEntities(imageEntities);

        return clone;
      }
    }).map(new Func1<FolderEntity, List<FolderEntity>>() {
      @Override public List<FolderEntity> call(FolderEntity folderEntity) {

        List<FolderEntity> galleryFolderEntities = new ArrayList<>(items.size() + 1);

        galleryFolderEntities.add(folderEntity);
        galleryFolderEntities.addAll(items);

        return galleryFolderEntities;
      }
    }).compose(SchedulersCompat.<List<FolderEntity>>applyIoSchedulers());
  }
}
