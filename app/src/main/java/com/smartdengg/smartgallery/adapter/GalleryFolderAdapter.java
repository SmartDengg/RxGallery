package com.smartdengg.smartgallery.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.orhanobut.logger.Logger;
import com.smartdengg.smartgallery.R;
import com.smartdengg.smartgallery.entity.FolderEntity;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;
import rx.Observer;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class GalleryFolderAdapter extends RecyclerView.Adapter<GalleryFolderAdapter.ViewHolder>
    implements Observer<List<FolderEntity>> {

  private Context context;
  private List<FolderEntity> items;
  private Callback callback;

  public GalleryFolderAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.gallery_folder_item, parent, false));
  }

  @SuppressLint("SetTextI18n") @Override public void onBindViewHolder(ViewHolder holder, int position) {

     /*无增删操作，所以，position可以作为唯一变量*/
    holder.rootView.setTag(position);

    FolderEntity entity = items.get(position);

    holder.nameTv.setText(entity.getFolderName());
    holder.countTv.setText(entity.getImageCount() + "");
    holder.flagIv.setVisibility(entity.isChecked() ? View.VISIBLE : View.GONE);

    String thumbUrl = entity.getThumbPath();
    if (thumbUrl != null) {
      /*Sorry for my poor english, but you must be careful of this file,because it may be load fail*/
      Picasso
          .with(context)
          .load(new File(thumbUrl))
          .placeholder(R.drawable.holder)
          .error(R.drawable.holder)
          .fit()
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

  @Override public void onNext(List<FolderEntity> galleryFolderEntities) {
    this.items = galleryFolderEntities;
  }

  public void updateItem(FolderEntity oldEntity, FolderEntity newEntity) {

    int oldPosition = this.items.indexOf(oldEntity);
    this.items.set(oldPosition, oldEntity);

    int newPosition = this.items.indexOf(newEntity);
    this.items.set(newPosition, newEntity);

    GalleryFolderAdapter.this.notifyDataSetChanged();

    /*麻蛋了，为什么在support V 22.2.0上有Bug*/
    //GalleryFolderAdapter.this.notifyItemChanged(oldPosition);
    //GalleryFolderAdapter.this.notifyItemChanged(newPosition);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @NonNull @Bind(R.id.gallery_folder_item_root_view) protected RelativeLayout rootView;

    @NonNull @Bind(R.id.gallery_folder_item_thumb_iv) protected ImageView coverIv;
    @NonNull @Bind(R.id.gallery_folder_item_name_tv) protected TextView nameTv;
    @NonNull @Bind(R.id.gallery_folder_item_count_tv) protected TextView countTv;
    @NonNull @Bind(R.id.gallery_folder_item_flag_iv) protected ImageView flagIv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @NonNull @OnClick(R.id.gallery_folder_item_root_view) protected void onItemClick() {

      Integer position = (Integer) rootView.getTag();
      if (callback != null && position != null) {
        callback.onItemClick(items.get(position));
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {
    void onItemClick(FolderEntity folderEntity);
  }
}
