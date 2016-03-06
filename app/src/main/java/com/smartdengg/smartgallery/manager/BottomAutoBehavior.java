package com.smartdengg.smartgallery.manager;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import com.smartdengg.smartgallery.utils.DensityUtil;

/**
 * Created by Joker on 2015/9/20.
 */
public class BottomAutoBehavior extends CoordinatorLayout.Behavior<RelativeLayout> {

  private static final String TAG = BottomAutoBehavior.class.getSimpleName();

  private static final int MIN_SCROLL_TO_HIDE = 10;
  private int totalDy;
  private int initialOffset;
  private int accummulatedDy;

  public BottomAutoBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.initialOffset = DensityUtil.getActionBarSize(context);
  }

  @Override
  public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, RelativeLayout child, View directTargetChild,
                                     View target, int nestedScrollAxes) {
    return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, RelativeLayout child, View target, int dx, int dy,
                                int[] consumed) {

    totalDy += dy;

    if (totalDy < initialOffset) {
      return;
    }

    if (dy > 0) {
      accummulatedDy = accummulatedDy > 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy > MIN_SCROLL_TO_HIDE) {
        BottomAutoBehavior.this.hideView(child);
      }
    } else if (dy < 0) {
      accummulatedDy = accummulatedDy < 0 ? accummulatedDy + dy : dy;
      if (accummulatedDy < -MIN_SCROLL_TO_HIDE) {
        BottomAutoBehavior.this.showView(child);
      }
    }
  }

  private void showView(final View view) {
    BottomAutoBehavior.this.runTranslateAnimation(view, 0, new FastOutSlowInInterpolator());
  }

  private void hideView(final View view) {
    int translateY = BottomAutoBehavior.this.calculateTranslation(view);
    BottomAutoBehavior.this.runTranslateAnimation(view, translateY, new FastOutLinearInInterpolator());
  }

  private int calculateTranslation(View view) {
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    int margin = params.topMargin;
    return view.getHeight() + margin;
  }

  private void runTranslateAnimation(View view, int translateY, Interpolator interpolator) {

    ViewCompat
        .animate(view)
        .translationY(translateY)
        .setInterpolator(interpolator)
        .withLayer()
        .setDuration(view.getResources().getInteger(android.R.integer.config_mediumAnimTime));
  }
}
