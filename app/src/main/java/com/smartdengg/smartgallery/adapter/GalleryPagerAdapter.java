package com.smartdengg.smartgallery.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.orhanobut.logger.Logger;
import com.smartdengg.smartgallery.R;
import com.lianjiatech.infrastructure.smartgallery.entity.ImageEntity;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;
import rx.Observer;

/**
 * Created by SmartDengg on 2016/3/10.
 */
public class GalleryPagerAdapter extends PagerAdapter implements Observer<List<ImageEntity>> {

    private Context context;
    private List<ImageEntity> items;

    public GalleryPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View page = LayoutInflater.from(context)
                                  .inflate(R.layout.gallery_item, container, false);
        //page.setBackgroundColor(Color.argb(255, position * 50, position * 10, position * 50));

        ImageView imageView = (ImageView) page.findViewById(R.id.gallery_item_iv);

        String imagePath = this.items.get(position)
                                     .getImagePath();
        Picasso.with(context)
               .load(new File(imagePath))
               .placeholder(R.drawable.holder)
               .error(R.drawable.holder)
               .networkPolicy(NetworkPolicy.NO_CACHE)
               .fit()
               .centerInside()
               .noFade()
               .tag(imagePath)
               .into(imageView);

        container.addView(page);

        return page;
    }

    @Override
    public int getCount() {
        return (this.items != null) ? (this.items.size()) : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void onCompleted() {
        GalleryPagerAdapter.this.notifyDataSetChanged();
    }

    @Override
    public void onError(Throwable e) {
        Logger.t(0)
              .e(e.toString());
    }

    @Override
    public void onNext(List<ImageEntity> imageEntities) {
        this.items = imageEntities;
    }
}
