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

package com.smartdengg.rxgallery.example.view;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by SmartDengg on 2016/8/26.
 */
public class ScaleTransformer implements ViewPager.PageTransformer {

  private static final float MIN_SCALE = 0.85f;
  private static final float MIN_ALPHA = 0.8f;

  @Override public void transformPage(View page, float position) {

      /*
      * http://stackoverflow.com/questions/23433027/onpagechangelistener-alpha-crossfading/23526632#23526632
      * http://andraskindler.com/blog/2013/create-viewpager-transitions-a-pagertransformer-example/
      * */
    int pageWidth = page.getWidth();
    int pageHeight = page.getHeight();

    if (position < -1) { // [-Infinity,-1)
      // This page is way off-screen to the left.
      page.setAlpha(MIN_ALPHA);
    } else if (position < 0) { // [-1,0]
      // This page is moving out to the left

      float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
      float vertMargin = pageHeight * (1 - scaleFactor) / 2;
      float horzMargin = pageWidth * (1 - scaleFactor) / 2;

      page.setTranslationX(horzMargin - vertMargin / 2);
      page.setScaleX(scaleFactor);
      page.setScaleY(scaleFactor);
      page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
    } else if (position <= 1) { //  (0,1]
      // This page is moving in from the right

      float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
      float vertMargin = pageHeight * (1 - scaleFactor) / 2;
      float horzMargin = pageWidth * (1 - scaleFactor) / 2;
      page.setTranslationX(-horzMargin + vertMargin / 2);

      page.setScaleX(scaleFactor);
      page.setScaleY(scaleFactor);
      page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
    } else { // (1,+Infinity]
      // This page is way off-screen to the right.
      page.setAlpha(MIN_ALPHA);
    }
  }
}
