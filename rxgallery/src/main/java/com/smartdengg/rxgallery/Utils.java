package com.smartdengg.rxgallery;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by Joker on 2016/6/21.
 */
public class Utils {

  private Utils() {
    throw new IllegalStateException("No instance");
  }

  public static void checkPermission(Context context, String permission) {
    if (!(context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)) {
      throw new RuntimeException("miss '" + permission + "' in your Manifest.xml");
    }
  }
}
