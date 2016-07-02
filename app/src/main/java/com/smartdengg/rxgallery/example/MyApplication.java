package com.smartdengg.rxgallery.example;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.DataUriRequestHandler;
import com.squareup.picasso.Picasso;
import okhttp3.OkHttpClient;

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
    @Override public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
      Logger.d("Picasso failure: %s \n    path = %s", exception.toString(), uri);
    }
  };

  @Override public void onCreate() {
    super.onCreate();

    Picasso picasso = new Picasso.Builder(MyApplication.this).downloader(
        new OkHttp3Downloader(new OkHttpClient()))
        .listener(picassoListener)
        .defaultBitmapConfig(Bitmap.Config.ARGB_8888)
        .addRequestHandler(new DataUriRequestHandler())
        .build();

    Picasso.setSingletonInstance(picasso);

    Logger.init(this.getPackageName())
        .setMethodOffset(0)
        .setMethodCount(4)
        .setLogLevel(LogLevel.FULL);
  }
}
