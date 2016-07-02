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

  public static boolean hasPermission(Context context, String permission) {
    return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
  }
}
