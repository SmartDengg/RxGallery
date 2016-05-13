package com.smartdengg.smartgallery.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatDialog;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.smartdengg.smartgallery.R;

/**
 * Created by SmartDengg on 2016/3/5.
 */
public class BottomSheetDialog extends AppCompatDialog {

    private Context context;

    public BottomSheetDialog(Activity activity) {
        super(activity);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setWindowAnimations(R.style.AnimBottom);

        this.context = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(this.wrapInBottomSheet(layoutResId, null, null));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(this.wrapInBottomSheet(0, view, null));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(this.wrapInBottomSheet(0, view, params));
    }

    private View wrapInBottomSheet(int layoutResId, View view, ViewGroup.LayoutParams params) {

        final CoordinatorLayout coordinator = (CoordinatorLayout) LayoutInflater.from(getContext())
                                                                                .inflate(R.layout.bottom_sheet_layout, null);

        if (layoutResId != 0 && view == null) {
            view = getLayoutInflater().inflate(layoutResId, coordinator, false);
        }
        FrameLayout bottomSheet = (FrameLayout) coordinator.findViewById(R.id.bottom_sheet_container);

        if (params == null) {
            bottomSheet.addView(view);
        } else {
            bottomSheet.addView(view, params);
        }

        if (this.shouldWindowCloseOnTouchOutside()) {
            final View finalView = view;
            coordinator.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    boolean showing = BottomSheetDialog.this.isShowing();
                    float x = event.getX();
                    float y = event.getY();

                    boolean pointInChildBounds = coordinator.isPointInChildBounds(finalView, (int) x, (int) y);
                    Integer actionMasked = MotionEventCompat.getActionMasked(event);
                    Integer actionDown = MotionEvent.ACTION_DOWN;

                    if (showing && actionMasked.equals(actionDown) && !pointInChildBounds) {
                        BottomSheetDialog.this.cancel();
                        return true;
                    }
                    return false;
                }
            });
        }
        return coordinator;
    }

    private boolean shouldWindowCloseOnTouchOutside() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return true;
        }
        TypedValue value = new TypedValue();
        boolean b = this.context.getTheme()
                                .resolveAttribute(android.R.attr.windowCloseOnTouchOutside, value, true);
        return b && value.data != 0;
    }
}
