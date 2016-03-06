package com.smartdengg.smartgallery.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.smartdengg.smartgallery.R;
import com.smartdengg.smartgallery.adapter.GalleryImageAdapter;
import com.smartdengg.smartgallery.entity.FolderEntity;
import com.smartdengg.smartgallery.manager.SchedulersCompat;
import com.smartdengg.smartgallery.ui.MarginDecoration;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryFolderActivity extends AppCompatActivity {

  private static final String COLUMN_NAME_COUNT = "v_count";
  private static String[] mediaColumns = new String[] {
      MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA, "COUNT(*) AS " + COLUMN_NAME_COUNT
  };
  private static String selection = " 1=1 ) GROUP BY (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME;

  @NonNull @Bind(R.id.folder_layout_rv) protected RecyclerView recyclerView;
  private GalleryImageAdapter folderAdapter;
  private FolderEntity folderEntity = new FolderEntity();

  private CompositeSubscription compositeSubscription = new CompositeSubscription();

  public static void navigateToGalleryFolder(AppCompatActivity startingActivity) {
    startingActivity.startActivity(new Intent(startingActivity, GalleryFolderActivity.class));
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_folder_layout);
    ButterKnife.bind(GalleryFolderActivity.this);

    GalleryFolderActivity.this.initView();
    GalleryFolderActivity.this.initData();
  }

  private void initView() {
    this.folderAdapter = new GalleryImageAdapter(GalleryFolderActivity.this);
    this.recyclerView.addItemDecoration(new MarginDecoration(GalleryFolderActivity.this));
    this.recyclerView.setHasFixedSize(true);
    this.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    this.recyclerView.setAdapter(folderAdapter);
  }

  private void initData() {

    compositeSubscription.add(Observable.create(new Observable.OnSubscribe<Cursor>() {
      @Override public void call(Subscriber<? super Cursor> subscriber) {

        final Cursor cursor = GalleryFolderActivity.this
            .getContentResolver()
            .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaColumns, selection, null, null);

        subscriber.add(Subscriptions.create(new Action0() {
          @Override public void call() {
            if (cursor != null && !cursor.isClosed()) {
              cursor.close();
            }
          }
        }));

        if (!subscriber.isUnsubscribed()) {
          if (cursor == null) {
            Observable.error(new NullPointerException("cursor must not be null"));
          } else {
            while (cursor.moveToNext()) {
              if (cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)).endsWith(".gif")) {
                continue;
              }
              subscriber.onNext(cursor);
            }
            cursor.close();
            subscriber.onCompleted();
          }
        }
      }
    }).map(new Func1<Cursor, FolderEntity>() {
      @Override public FolderEntity call(Cursor cursor) {

        String thumbUrl = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

        String folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
        int imageCount = cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_COUNT));

        FolderEntity clone = folderEntity.newInstance();
        clone.setThumbPath(thumbUrl);
        clone.setFolderName(folderName);

        return clone;
      }
    }).toList().compose(SchedulersCompat.<List<FolderEntity>>applyIoSchedulers()).subscribe());
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(GalleryFolderActivity.this);

    if (compositeSubscription.hasSubscriptions() && compositeSubscription.isUnsubscribed()) {
      this.compositeSubscription.clear();
    }
  }
}
