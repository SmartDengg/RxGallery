package com.smartdengg.rxgallery.example.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.orhanobut.logger.Logger;
import com.smartdengg.rxgallery.example.R;
import com.smartdengg.rxgallery.example.entity.WrapperFolderEntity;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;
import rx.Observer;
import rx.RxDebounceClick;
import rx.functions.Action1;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryFolderAdapter extends RecyclerView.Adapter<GalleryFolderAdapter.ViewHolder>
    implements Observer<List<WrapperFolderEntity>> {

  private Context context;
  private List<WrapperFolderEntity> items;
  private Callback callback;

  public GalleryFolderAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.gallery_folder_item, parent, false));
  }

  @SuppressLint("SetTextI18n") @Override
  public void onBindViewHolder(ViewHolder holder, final int position) {

    WrapperFolderEntity entity = items.get(position);

    holder.nameTv.setText(entity.getFolderName());
    holder.countTv.setText(entity.getImageCount() + "");
    holder.flagIv.setVisibility(entity.isChecked() ? View.VISIBLE : View.GONE);

    RxDebounceClick.onClick(holder.itemView).forEach(new Action1<Void>() {
      @Override public void call(Void aVoid) {
        if (callback != null) {
          callback.onItemClick(items.get(position));
        }
      }
    });

    String thumbUrl = entity.getThumbPath();
    if (thumbUrl != null) {
            /*Sorry for my poor english, but you must be careful of this file,because it may be load fail*/
      Picasso.with(context)
          .load(new File(thumbUrl))
          .placeholder(R.drawable.holder)
          .error(R.drawable.holder)
          .networkPolicy(NetworkPolicy.OFFLINE)
          .fit()
          .centerCrop()
          .noFade()
          .into(holder.coverIv);
    }
  }

  @Override public int getItemCount() {
    return (this.items != null) ? (this.items.size()) : 0;
  }

  @Override public void onCompleted() {
    GalleryFolderAdapter.this.notifyDataSetChanged();
  }

  @Override public void onError(Throwable e) {
    Logger.e(e.toString());
  }

  @Override public void onNext(List<WrapperFolderEntity> galleryFolderEntities) {
    this.items = galleryFolderEntities;
  }

  public void updateItem(WrapperFolderEntity oldEntity, WrapperFolderEntity newEntity) {

    int oldPosition = this.items.indexOf(oldEntity);
    this.items.set(oldPosition, oldEntity);

    int newPosition = this.items.indexOf(newEntity);
    this.items.set(newPosition, newEntity);

    GalleryFolderAdapter.this.notifyDataSetChanged();
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    void onItemClick(WrapperFolderEntity folderEntity);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @NonNull @Bind(R.id.gallery_folder_item_thumb_iv) protected ImageView coverIv;
    @NonNull @Bind(R.id.gallery_folder_item_name_tv) protected TextView nameTv;
    @NonNull @Bind(R.id.gallery_folder_item_count_tv) protected TextView countTv;
    @NonNull @Bind(R.id.gallery_folder_item_flag_iv) protected ImageView flagIv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }
  }
}
