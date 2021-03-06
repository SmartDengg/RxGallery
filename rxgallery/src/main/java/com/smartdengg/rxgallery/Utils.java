/*
 * Copyright 2016 SmartDengg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.smartdengg.rxgallery;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.smartdengg.rxgallery.ui.TransparentActivity;

/**
 * Created by Joker on 2016/6/21.
 */
public class Utils {

  private Utils() {
    throw new IllegalStateException("No instance");
  }

  public static boolean hasReadExternalPermission(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && !Utils.hasPermission(context,
        Manifest.permission.READ_EXTERNAL_STORAGE)) {
      TransparentActivity.navigateToTransparentActivity(context,
          new String[] { Manifest.permission.READ_EXTERNAL_STORAGE });
      return false;
    }
    return true;
  }

  private static boolean hasPermission(Context context, String permission) {
    return (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
  }
}
