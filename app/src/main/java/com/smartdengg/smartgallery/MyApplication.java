package com.smartdengg.smartgallery;

import android.app.ActivityManager;
import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

/**
 * *   ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * ┃　　　┃
 * ┃　　　┃
 * ┃　　　┗━━━┓
 * ┃　　　　　   ┣┓
 * ┃　　　　　   ┏┛
 * ┗┓┓┏━┳┓┏┛
 * ┃┫┫　┃┫┫
 * ┗┻┛　┗┻┛
 * Created by SmartDengg on 2016/3/4.
 */
public class MyApplication extends Application {

    private Picasso.Listener picassoListener = new Picasso.Listener() {
        @Override
        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            Logger.d("Picasso failure: %s \n    path = %s", exception.toString(), uri);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Picasso picasso = new Picasso.Builder(MyApplication.this).listener(picassoListener)
                                                                 .defaultBitmapConfig(Bitmap.Config.ARGB_8888)
                                                                 .build();

        Picasso.setSingletonInstance(picasso);

        Logger.init("SmartDengg")
              .setMethodOffset(0)
              .setMethodCount(4)
              .setLogLevel(LogLevel.FULL);

        Logger.t(0)
              .d(calculateMemoryCacheSize() + "");
    }

    int calculateMemoryCacheSize() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        boolean largeHeap = (this.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap && SDK_INT >= HONEYCOMB) {
            memoryClass = am.getLargeMemoryClass();
        }
        // Target ~15% of the available heap.
        return memoryClass / 7;
    }

}
