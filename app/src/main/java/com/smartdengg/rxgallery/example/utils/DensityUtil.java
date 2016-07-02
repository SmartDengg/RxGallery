package com.smartdengg.rxgallery.example.utils;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class DensityUtil {

  private static int actionBarSize;

  /**
   * 获取ActionBarSize
   */
  public static int getActionBarSize(Context context) {

    if (actionBarSize == 0) {
      TypedArray actionbarSizeTypedArray = null;
      try {
        actionbarSizeTypedArray =
            context.obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        actionBarSize = (int) actionbarSizeTypedArray.getDimension(0, 0);
      } finally {
        actionbarSizeTypedArray.recycle();
      }
    }

    return actionBarSize;
  }
}
