package com.smartdengg.rxgallery.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * 创建时间:  2016/08/18 11:33 <br>
 * 作者:  SmartDengg <br>
 * 描述:  透明activity,承载PermissionNotGrantedFragment弹出未获取权限对话框
 */
public class TransparentActivity extends Activity implements PermissionDialogListener {

  public static void navigateToTransparentActivity(Context context, String[] permissions) {
    Intent intent = new Intent(context, TransparentActivity.class);
    intent.putExtra(PermissionNotGrantedFragment.PERMISSION, permissions);
    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PermissionNotGrantedFragment notGrantedFragment = PermissionNotGrantedFragment.newInstance(
        getIntent().getStringArrayExtra(PermissionNotGrantedFragment.PERMISSION));
    notGrantedFragment.show(getFragmentManager(), PermissionNotGrantedFragment.TAG);
  }

  @Override public void onDismiss() {
    TransparentActivity.this.finish();
  }
}
