package com.smartdengg.rxgallery.example.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.rxgallery.example.R;
import com.smartdengg.rxgallery.example.adapter.GalleryPagerAdapter;
import com.smartdengg.rxgallery.entity.ImageEntity;
import com.squareup.picasso.Picasso;
import java.io.Serializable;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

/**
 * Created by SmartDengg on 2016/3/10.
 */
@SuppressWarnings("unchecked")
public class PreviewActivity extends AppCompatActivity {

    private static final String ENTITIES = "entities";

    @NonNull
    @Bind(R.id.preview_layout_vp)
    protected ViewPager viewPager;
    @NonNull
    @Bind(R.id.preview_layout_index_tv)
    protected TextView indexTv;

    private int totalCount;

    private ViewPager.PageTransformer transformer = new ViewPager.PageTransformer() {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.8f;

        @Override
        public void transformPage(View page, float position) {

            /*
            * http://stackoverflow.com/questions/23433027/onpagechangelistener-alpha-crossfading/23526632#23526632
            * http://andraskindler.com/blog/2013/create-viewpager-transitions-a-pagertransformer-example/
            * */
            int pageWidth = page.getWidth();
            int pageHeight = page.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.setAlpha(MIN_ALPHA);
            } else if (position < 0) { // [-1,0]
                // This page is moving out to the left

                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;

                page.setTranslationX(horzMargin - vertMargin / 2);
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else if (position <= 1) { //  (0,1]
                // This page is moving in from the right

                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                page.setTranslationX(-horzMargin + vertMargin / 2);

                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
                page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                page.setAlpha(MIN_ALPHA);
            }
        }
    };

    private ViewPager.SimpleOnPageChangeListener changeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int state) {
            final Picasso picasso = Picasso.with(PreviewActivity.this);
            if (state == RecyclerView.SCROLL_STATE_IDLE || state == RecyclerView.SCROLL_STATE_SETTLING) {
                picasso.resumeTag(PreviewActivity.this);
            } else {
                picasso.pauseTag(PreviewActivity.this);
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPageSelected(int position) {
            indexTv.setText(position + 1 + "/" + totalCount);
        }
    };

    public static void navigateToPreview(Activity startingActivity, List<ImageEntity> imageEntities) {
        Intent intent = new Intent(startingActivity, PreviewActivity.class);
        intent.putExtra(ENTITIES, (Serializable) imageEntities);
        startingActivity.startActivity(intent);
        startingActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_in);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_activity_layout);
        ButterKnife.bind(PreviewActivity.this);

        PreviewActivity.this.initView(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    private void initView(Bundle savedInstanceState) {
        GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(PreviewActivity.this);
        viewPager.setClipToPadding(false);
        viewPager.setPageMargin(16);
        viewPager.addOnPageChangeListener(changeListener);
        viewPager.setPageTransformer(false, transformer);
        viewPager.setAdapter(pagerAdapter);

        Observable.fromCallable(new Func0<List<ImageEntity>>() {
            @Override
            public List<ImageEntity> call() {
                return (List<ImageEntity>) getIntent().getSerializableExtra(ENTITIES);
            }
        })
                  .doOnNext(new Action1<List<ImageEntity>>() {
                      @Override
                      public void call(List<ImageEntity> imageEntities) {
                          if (imageEntities != null) PreviewActivity.this.totalCount = imageEntities.size();
                          indexTv.setText("1/" + totalCount);
                      }
                  })
                  .subscribe(pagerAdapter);
    }

    @NonNull
    @OnClick(R.id.preview_layout_cancel_iv)
    protected void onCancelClick() {
        PreviewActivity.this.finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.viewPager.clearOnPageChangeListeners();
        this.viewPager.setPageTransformer(false, null);
        ButterKnife.unbind(PreviewActivity.this);
    }
}
