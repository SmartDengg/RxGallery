package com.smartdengg.rxgallery.example.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.CheckResult;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.orhanobut.logger.Logger;
import com.smartdengg.rxgallery.entity.FolderEntity;
import com.smartdengg.rxgallery.entity.ImageEntity;
import com.smartdengg.rxgallery.example.R;
import com.smartdengg.rxgallery.example.adapter.GalleryFolderAdapter;
import com.smartdengg.rxgallery.example.adapter.GalleryImageAdapter;
import com.smartdengg.rxgallery.example.entity.WrapperFolderEntity;
import com.smartdengg.rxgallery.example.utils.BestBlur;
import com.smartdengg.rxgallery.example.view.BottomSheetDialog;
import com.smartdengg.rxgallery.example.view.MarginDecoration;
import com.smartdengg.rxgallery.usecase.GalleryListUseCase;
import com.smartdengg.rxgallery.usecase.GalleryMapUseCase;
import com.squareup.picasso.Picasso;
import hugo.weaving.DebugLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import rx.Observable;
import rx.RxDebounceClick;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryActivity extends AppCompatActivity {

    protected static final int BLUR_RADIUS = 6;
    protected static final float BLUR_DESATURATE = 0.0f;
    protected static final int BLUR_SCALE = BLUR_RADIUS / 2;
    private static final int MAX_COUNT = 9;
    private static final int SPAN_COUNT = 2;

    @NonNull
    @Bind(R.id.gallery_layout_count_tv)
    protected TextView countTv;
    @NonNull
    @Bind(R.id.gallery_layout_bottom_rl)
    protected RelativeLayout bottomRl;
    @NonNull
    @Bind(R.id.gallery_layout_category_btn)
    protected Button categoryBtn;
    @NonNull
    @Bind(R.id.gallery_layout_preview_btn)
    protected Button previewBtn;

    @NonNull
    @Bind(R.id.gallery_layout_rv)
    protected RecyclerView recyclerView;
    @NonNull
    @Bind(R.id.gallery_layout_hero_iv)
    protected ImageView heroIv;

    /*Image 相关*/
    private GalleryImageAdapter galleryImageAdapter;
    private List<ImageEntity> selectedImageEntities = new ArrayList<>();

    /*SheetDialog 相关*/
    private BottomSheetDialog sheetDialog;
    private List<WrapperFolderEntity> folderEntities = new ArrayList<>();
    private GalleryFolderAdapter galleryFolderAdapter;
    private WrapperFolderEntity currentFolderEntity;

    private Subscription subscription = Subscriptions.empty();

    /*缓存屏幕快照相关*/
    private Canvas canvas = null;
    private Paint paint = null;
    private Bitmap screenBitmap;

    private DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            ViewCompat.animate(bottomRl)
                      .translationY(0.0f)
                      .setStartDelay(getResources().getInteger(android.R.integer.config_shortAnimTime))
                      .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                      .setListener(null);
        }
    };

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            final Picasso picasso = Picasso.with(GalleryActivity.this);
            if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                picasso.resumeTag(GalleryImageAdapter.TAG);
            } else {
                picasso.pauseTag(GalleryImageAdapter.TAG);
            }
        }
    };

    private GalleryImageAdapter.Callback imageCallback = new GalleryImageAdapter.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onItemClick(ImageEntity imageEntity) {

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

    private GalleryFolderAdapter.Callback folderCallback = new GalleryFolderAdapter.Callback() {
        @Override
        public void onItemClick(WrapperFolderEntity folderEntity) {

            if (currentFolderEntity.equals(folderEntity)) {
                sheetDialog.dismiss();
                return;
            }

            GalleryActivity.this.refreshData(folderEntity);

            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    sheetDialog.dismiss();
                }
            });
        }
    };

    public static void navigateToGallery(AppCompatActivity startingActivity) {
        startingActivity.startActivity(new Intent(startingActivity, GalleryActivity.class));
        startingActivity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_layout);
        ButterKnife.bind(GalleryActivity.this);

        GalleryActivity.this.initView(savedInstanceState);
        GalleryActivity.this.loadGallery();
    }

    private void initView(Bundle savedInstanceState) {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(GalleryActivity.this, SPAN_COUNT);
        gridLayoutManager.setSmoothScrollbarEnabled(true);

        galleryImageAdapter = new GalleryImageAdapter(GalleryActivity.this);
        galleryImageAdapter.setCallback(imageCallback);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new MarginDecoration(GalleryActivity.this));
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setAdapter(galleryImageAdapter);

        RxDebounceClick.onClick(previewBtn)
                       .filter(new Func1<Void, Boolean>() {
                           @Override
                           public Boolean call(Void aVoid) {
                               return GalleryActivity.this.selectedImageEntities.size() > 0;
                           }
                       })
                       .forEach(new Action1<Void>() {
                           @Override
                           public void call(Void aVoid) {
                               GalleryActivity.this.navigateToPreview(selectedImageEntities);
                           }
                       });

        if (savedInstanceState == null) {
            final View rootView = bottomRl.getRootView()
                                          .findViewById(android.R.id.content);
            final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        rootView.getViewTreeObserver()
                                .removeOnPreDrawListener(this);
                        ViewCompat.setTranslationY(bottomRl, bottomRl.getHeight());
                        return true;
                    }
                });
            }
        }
    }

    private void navigateToPreview(List<ImageEntity> imageEntities) {

        BestBlur bestBlur = new BestBlur(GalleryActivity.this);

        Bitmap screenSnapshot = GalleryActivity.this.catchScreen();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(screenSnapshot,
                screenSnapshot.getWidth() / BLUR_SCALE, screenSnapshot.getHeight() / BLUR_SCALE, true);
        Bitmap blurBitmap = bestBlur.blurBitmap(scaledBitmap, BLUR_RADIUS, BLUR_DESATURATE);

        heroIv.setVisibility(View.VISIBLE);
        heroIv.setImageBitmap(blurBitmap);

        if (screenSnapshot != scaledBitmap) scaledBitmap.recycle();
        bestBlur.destroy();

        PreviewActivity.navigateToPreview(GalleryActivity.this, imageEntities);
    }

    @DebugLog
    private void loadGallery() {

        subscription = GalleryMapUseCase.createdUseCase(GalleryActivity.this)
                                        .retrieveAllGallery()
                                        .takeFirst(new Func1<Map<String, FolderEntity>, Boolean>() {
                                            @Override
                                            public Boolean call(Map<String, FolderEntity> stringFolderEntityMap) {
                                                return !subscription.isUnsubscribed();
                                            }
                                        })
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Subscriber<Map<String, FolderEntity>>() {
                                            @Override
                                            public void onCompleted() {
                                                ViewCompat.animate(bottomRl)
                                                          .translationY(0.0f)
                                                          .setStartDelay(getResources().getInteger(android.R.integer.config_longAnimTime))
                                                          .withLayer();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Logger.t(0)
                                                      .e(e.toString());
                                            }

                                            @Override
                                            public void onNext(Map<String, FolderEntity> folderEntityMap) {

                                                //@formatter:off
                                                for (Map.Entry<String, FolderEntity> entry : folderEntityMap.entrySet()) {

                                                    WrapperFolderEntity wrapperFolderEntity = new WrapperFolderEntity();
                                                    wrapperFolderEntity.setFolderName(entry.getValue().getFolderName());
                                                    wrapperFolderEntity.setFolderPath(entry.getValue().getFolderPath());
                                                    wrapperFolderEntity.setThumbPath(entry.getValue().getThumbPath());
                                                    wrapperFolderEntity.setImageEntities(entry.getValue().getImageEntities());

                                                    GalleryActivity.this.folderEntities.add(wrapperFolderEntity);
                                                }

                                                //@formatter:on
                                                GalleryActivity.this.currentFolderEntity = GalleryActivity.this.folderEntities.get(0);
                                                currentFolderEntity.setChecked(true);

                                                /*填充数据*/
                                                Observable.just(currentFolderEntity.getImageEntities())
                                                          .subscribe(GalleryActivity.this.galleryImageAdapter);

                                            }
                                        });
    }

    @NonNull
    @OnClick(R.id.gallery_layout_back_iv)
    protected void onBackClick() {
        GalleryActivity.this.finish();
    }

    @NonNull
    @OnClick(R.id.gallery_layout_category_btn)
    protected void onCategoryClick() {

        ViewCompat.animate(bottomRl)
                  .translationY(bottomRl.getHeight())
                  .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                  .setListener(new ViewPropertyAnimatorListenerAdapter() {
                      @Override
                      public void onAnimationEnd(View view) {
                          if (sheetDialog == null) {
                              GalleryActivity.this.installSheet();
                          }
                          sheetDialog.show();
                      }
                  });
    }

    private void installSheet() {

        RecyclerView recyclerView = (RecyclerView) LayoutInflater.from(GalleryActivity.this)
                                                                 .inflate(R.layout.recycler_view_layout, null);

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
        Observable.just(folderEntities)
                  .subscribe(galleryFolderAdapter);

        sheetDialog = new BottomSheetDialog(GalleryActivity.this);
        sheetDialog.setContentView(recyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sheetDialog.setOnDismissListener(dismissListener);
    }

    private void refreshData(WrapperFolderEntity folderEntity) {

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
        Observable.just(currentFolderEntity.getImageEntities())
                  .subscribe(galleryImageAdapter);

        /*更改文件夹提示*/
        categoryBtn.setText(currentFolderEntity.getFolderName()
                                               .toLowerCase());
    }

    @CheckResult
    private Bitmap catchScreen() {

        View rootView = GalleryActivity.this.getWindow()
                                            .getDecorView()
                                            .findViewById(android.R.id.content);

        rootView.setDrawingCacheEnabled(true);
        Bitmap drawingCache = rootView.getDrawingCache();

        if (screenBitmap == null || canvas == null || paint == null) {
            screenBitmap = Bitmap.createBitmap(drawingCache.getWidth(), drawingCache.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(screenBitmap);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        }

        canvas.drawBitmap(drawingCache, 0, 0, paint);

        rootView.destroyDrawingCache();

        return screenBitmap;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.heroIv.setVisibility(View.GONE);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.subscription.unsubscribe();
        Picasso.with(GalleryActivity.this).cancelTag(GalleryImageAdapter.TAG);
        ButterKnife.unbind(GalleryActivity.this);
    }
}
