package com.smartdengg.rxgallery.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import com.smartdengg.rxgallery.R;

/**
 * 创建时间: 2016/08/18 11:41 <br>
 * 作者: dengwei <br>
 * 描述: 未获取到权限时弹出的DialogFragment
 */
@SuppressLint("ValidFragment") class PermissionNotGrantedFragment extends DialogFragment {
  static final String TAG = PermissionNotGrantedFragment.class.getSimpleName();

  static final String PERMISSION = "permission";

  static PermissionNotGrantedFragment newInstance(String[] permissions) {
    PermissionNotGrantedFragment notGrantedFragment = new PermissionNotGrantedFragment();

    if (permissions != null && permissions.length > 0) {
      Bundle args = new Bundle();
      args.putStringArray(PERMISSION, permissions);
      notGrantedFragment.setArguments(args);
    }

    return notGrantedFragment;
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {

    Bundle arguments = getArguments();
    String[] permissions = (String[]) arguments.get(PERMISSION);

    StringBuilder stringBuilder = new StringBuilder();

    if (permissions != null) {

      int length = permissions.length;
      if (length > 0) {
        for (int i = 0; i < length; i++) {
          if (i != length - 1) {
            stringBuilder.append(permissions[i]).append(",");
          } else {
            stringBuilder.append(permissions[i]);
          }
        }
      }
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.PermissionDialog);
    builder.setTitle("Grant permission")
        .setMessage("To use RxGallery feature you have to grant \""
            + stringBuilder.toString()
            + "\" permission. Go to device settings and enable it.")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {

            Activity activity = getActivity();
            if (activity != null && activity instanceof PermissionDialogListener) {
              ((PermissionDialogListener) activity).onDismiss();
            }
          }
        });

    AlertDialog dialog = builder.create();
    dialog.setCanceledOnTouchOutside(false);
    dialog.setCancelable(false);
    dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
      @Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0;
      }
    });

    return dialog;
  }
}