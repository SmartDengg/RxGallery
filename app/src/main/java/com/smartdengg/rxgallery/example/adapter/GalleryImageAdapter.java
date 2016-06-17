package com.smartdengg.rxgallery.example.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.orhanobut.logger.Logger;
import com.smartdengg.rxgallery.entity.ImageEntity;
import com.smartdengg.rxgallery.example.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;
import rx.Observer;
import rx.RxDebounceClick;
import rx.functions.Action1;

/**
 * GalleryImageAdapter
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.ViewHolder> implements Observer<List<ImageEntity>> {

    public static final String TAG = GalleryImageAdapter.class.getSimpleName();
    private Context context;
    private List<ImageEntity> items;

    private boolean animationsLocked;
    private int lastAnimatedPosition = -1;

    private int normalColor;
    private Drawable selectedDrawable;

    private Callback callback;

    private int i = 0;

    public GalleryImageAdapter(Context context) {
        this.context = context;
        this.normalColor = context.getResources()
                                  .getColor(android.R.color.transparent);
        this.selectedDrawable = context.getResources()
                                       .getDrawable(R.drawable.iv_boundary_shape);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context)
                                     .inflate(R.layout.gallery_image_item, parent, false);

        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        GalleryImageAdapter.this.bindValue(holder, position, items.get(position));
        GalleryImageAdapter.this.runEnterAnimation(holder.itemView, position);
    }

    private void bindValue(ViewHolder holder, final int position, ImageEntity entity) {

        RxDebounceClick.onClick(holder.thumbIv)
                       .forEach(new Action1<Void>() {
                           @Override
                           public void call(Void aVoid) {

                               if (callback != null) {
                                   callback.onItemClick(items.get(position));
                               }
                           }
                       });

        String thumbUrl = entity.getImagePath();
        if (thumbUrl != null) {
            /*Sorry for my poor english, but you must be careful of this file,because it may be load fail*/
            Picasso picasso = Picasso.with(context);
            //picasso.setIndicatorsEnabled(true);
            /**
             * D/Picasso﹕ Main        created      [R0] Request{http://i.imgur.com/rT5vXE1.jpg}
             * D/Picasso﹕ Dispatcher  enqueued     [R0]+21ms
             * D/Picasso﹕ Hunter      executing    [R0]+26ms
             * D/Picasso﹕ Hunter      decoded      [R0]+575ms
             * D/Picasso﹕ Dispatcher  batched      [R0]+576ms for completion
             * D/Picasso﹕ Main        completed    [R0]+807ms from NETWORK
             * D/Picasso﹕ Dispatcher  delivered    [R0]+809ms
             * */
            //picasso.setLoggingEnabled(true);
            picasso.cancelRequest(holder.thumbIv);

            picasso.load(new File(thumbUrl))
                   .placeholder(R.drawable.holder)
                   .error(R.drawable.holder)
                   .networkPolicy(NetworkPolicy.OFFLINE)
                   .fit()
                   .centerCrop()
                   .tag(TAG)
                   .into(holder.thumbIv);
        }

        if (entity.isChecked()) {
            holder.itemView.setBackgroundDrawable(selectedDrawable);
        } else {
            holder.itemView.setBackgroundColor(normalColor);
        }
    }

    private void runEnterAnimation(View itemView, int position) {

        if (animationsLocked) return;

        if (position > lastAnimatedPosition) {
            GalleryImageAdapter.this.lastAnimatedPosition = position;

            ViewCompat.setTranslationY(itemView, 100);

            ViewCompat.animate(itemView)
                      .translationY(0.0f)
                      .setStartDelay(position * 20)
                      .setInterpolator(new DecelerateInterpolator(2.0f))
                      .setDuration(context.getResources()
                                          .getInteger(android.R.integer.config_mediumAnimTime))
                      .withLayer()
                      .setListener(new ViewPropertyAnimatorListenerAdapter() {
                          @Override
                          public void onAnimationEnd(View view) {
                              GalleryImageAdapter.this.animationsLocked = true;
                          }
                      });
        }
    }

    @Override
    public int getItemCount() {
        return (this.items != null) ? (this.items.size()) : 0;
    }

    @Override
    public void onCompleted() {
        GalleryImageAdapter.this.notifyDataSetChanged();
    }

    @Override
    public void onError(Throwable e) {
        Logger.t(0)
              .e(e.toString());
    }

    @Override
    public void onNext(List<ImageEntity> galleryFolderEntities) {
        this.items = galleryFolderEntities;
    }

    public void updateItem(ImageEntity entity) {

        int position = this.items.indexOf(entity);
        this.items.set(position, entity);

        GalleryImageAdapter.this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        @Bind(R.id.gallery_image_item_thumb_iv)
        protected ImageView thumbIv;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));

            ButterKnife.bind(ViewHolder.this, itemView);
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {

        void onItemClick(ImageEntity imageEntity);
    }
}
