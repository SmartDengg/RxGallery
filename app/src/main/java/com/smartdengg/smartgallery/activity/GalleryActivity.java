package com.smartdengg.smartgallery.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.smartdengg.smartgallery.R;
import com.smartdengg.smartgallery.adapter.GalleryFolderAdapter;
import com.smartdengg.smartgallery.adapter.GalleryImageAdapter;
import com.smartdengg.smartgallery.domain.GalleryUseCase;
import com.smartdengg.smartgallery.entity.FolderEntity;
import com.smartdengg.smartgallery.entity.ImageEntity;
import com.smartdengg.smartgallery.ui.BottomSheetDialog;
import com.smartdengg.smartgallery.ui.MarginDecoration;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryActivity extends AppCompatActivity {

  private static final int MAX_COUNT = 9;

  @NonNull @Bind(R.id.gallery_layout_count_tv) protected TextView countTv;
  @NonNull @Bind(R.id.gallery_layout_bottom_rl) protected RelativeLayout bottomRl;
  @NonNull @Bind(R.id.gallery_layout_category_btn) protected Button categoryBtn;

  @NonNull @Bind(R.id.gallery_layout_rv) protected RecyclerView recyclerView;

  /*Image 相关*/
  private GalleryImageAdapter galleryImageAdapter;
  private List<ImageEntity> selectedImageEntities = new ArrayList<>();

  /*SheetDialog 相关*/
  private BottomSheetDialog sheetDialog;
  private List<FolderEntity> galleryFolderEntities;
  private GalleryFolderAdapter galleryFolderAdapter;
  private FolderEntity currentFolderEntity;

  /*加载图片*/
  private GalleryUseCase useCase;

  private DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
    @Override public void onDismiss(DialogInterface dialog) {
      ViewCompat
          .animate(bottomRl)
          .translationY(0.0f)
          .setStartDelay(getResources().getInteger(android.R.integer.config_shortAnimTime))
          .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
          .setListener(null);
    }
  };

  private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
    @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
      final Picasso picasso = Picasso.with(GalleryActivity.this);
      if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) {
        picasso.resumeTag(GalleryActivity.this);
      } else {
        picasso.pauseTag(GalleryActivity.this);
      }
    }
  };

  private GalleryImageAdapter.Callback imageCallback = new GalleryImageAdapter.Callback() {
    @SuppressLint("SetTextI18n") @Override public void onItemClick(ImageEntity imageEntity) {

      if (selectedImageEntities.contains(imageEntity)) {
        imageEntity.setChecked(false);
        selectedImageEntities.remove(imageEntity);
      } else {

        if (selectedImageEntities.size() >= MAX_COUNT) return;

        imageEntity.setChecked(true);
        selectedImageEntities.add(imageEntity);
      }
      galleryImageAdapter.updateItem(imageEntity);

      /*设置数量*/
      Integer size = selectedImageEntities.size();
      countTv.setText(size + "/" + MAX_COUNT);
      countTv.setTextColor((size == MAX_COUNT) ? Color.RED : Color.WHITE);
    }
  };

  GalleryFolderAdapter.Callback folderCallback = new GalleryFolderAdapter.Callback() {
    @Override public void onItemClick(FolderEntity folderEntity) {

      if (currentFolderEntity.equals(folderEntity)) {
        sheetDialog.dismiss();
        return;
      }

      GalleryActivity.this.refreshData(folderEntity);

      recyclerView.post(new Runnable() {
        @Override public void run() {
          sheetDialog.dismiss();
        }
      });
    }
  };

  public static void navigateToGallery(AppCompatActivity startingActivity) {
    startingActivity.startActivity(new Intent(startingActivity, GalleryActivity.class));
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.gallery_activity_layout);
    ButterKnife.bind(GalleryActivity.this);

    GalleryActivity.this.initView(savedInstanceState);
    GalleryActivity.this.initData();
  }

  private void initView(Bundle savedInstanceState) {

    GridLayoutManager gridLayoutManager = new GridLayoutManager(GalleryActivity.this, 3);
    gridLayoutManager.setSmoothScrollbarEnabled(true);

    galleryImageAdapter = new GalleryImageAdapter(GalleryActivity.this);
    galleryImageAdapter.setCallback(imageCallback);
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(new MarginDecoration(GalleryActivity.this));
    recyclerView.addOnScrollListener(scrollListener);
    recyclerView.setAdapter(galleryImageAdapter);

    if (savedInstanceState == null) {
      final View rootView = bottomRl.getRootView().findViewById(android.R.id.content);
      final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
      if (viewTreeObserver.isAlive()) {
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            rootView.getViewTreeObserver().removeOnPreDrawListener(this);
            ViewCompat.setTranslationY(bottomRl, bottomRl.getHeight());
            return true;
          }
        });
      }
    }
  }

  private void initData() {
    useCase = GalleryUseCase.createdUseCase(GalleryActivity.this);
    useCase.subscribe(new Subscriber<List<FolderEntity>>() {
      @Override public void onCompleted() {
        ViewCompat
            .animate(bottomRl)
            .translationY(0.0f)
            .setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime))
            .withLayer();
      }

      @Override public void onError(Throwable e) {
        Logger.e(e.toString());
      }

      @Override public void onNext(List<FolderEntity> galleryFolderEntities) {

        GalleryActivity.this.galleryFolderEntities = galleryFolderEntities;

        /*填充数据*/
        GalleryActivity.this.currentFolderEntity = galleryFolderEntities.get(0);
        Observable.just(currentFolderEntity.getImageEntities()).subscribe(GalleryActivity.this.galleryImageAdapter);
      }
    });
  }

  @NonNull @OnClick(R.id.gallery_layout_category_btn) protected void onCategoryClick() {

    ViewCompat
        .animate(bottomRl)
        .translationY(bottomRl.getHeight())
        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            if (sheetDialog == null) {
              GalleryActivity.this.installSheet();
            }
            sheetDialog.show();
          }
        });
  }

  private void installSheet() {

    RecyclerView recyclerView =
        (RecyclerView) LayoutInflater.from(GalleryActivity.this).inflate(R.layout.recycler_view_layout, null);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(GalleryActivity.this);
    linearLayoutManager.setSmoothScrollbarEnabled(true);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

    galleryFolderAdapter = new GalleryFolderAdapter(GalleryActivity.this);
    galleryFolderAdapter.setCallback(folderCallback);
    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(new MarginDecoration(GalleryActivity.this));
    recyclerView.setAdapter(galleryFolderAdapter);
    /*填充文件夹数据*/
    Observable.just(galleryFolderEntities).subscribe(galleryFolderAdapter);

    sheetDialog = new BottomSheetDialog(GalleryActivity.this);
    sheetDialog.setContentView(recyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                        ViewGroup.LayoutParams.MATCH_PARENT));
    sheetDialog.setOnDismissListener(dismissListener);
  }

  @NonNull @OnClick(R.id.gallery_layout_preview_btn) protected void onPreviewClick() {
    Logger.json(new Gson().toJson(selectedImageEntities));
  }

  private void refreshData(FolderEntity folderEntity) {

    List<ImageEntity> imageEntities = folderEntity.getImageEntities();

    for (ImageEntity entity : selectedImageEntities) {
      if (imageEntities.contains(entity)) {
        Integer index = imageEntities.indexOf(entity);
        imageEntities.set(index, entity);
      }
    }
    folderEntity.setImageEntities(imageEntities);

    /*更新之前选中和当前选中文件夹状态*/
    currentFolderEntity.setChecked(false);
    folderEntity.setChecked(true);
    galleryFolderAdapter.updateItem(currentFolderEntity, folderEntity);

    /*设置当前被选中文件夹*/
    currentFolderEntity = folderEntity;

     /*刷新照片墙*/
    Observable.just(currentFolderEntity.getImageEntities()).subscribe(galleryImageAdapter);

    /*更改文件夹提示*/
    categoryBtn.setText(currentFolderEntity.getFolderName().toLowerCase());
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.useCase.unsubscribe();
    ButterKnife.unbind(GalleryActivity.this);
  }
}
