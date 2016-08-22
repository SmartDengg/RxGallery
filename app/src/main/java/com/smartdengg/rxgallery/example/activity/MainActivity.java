package com.smartdengg.rxgallery.example.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.rxgallery.example.R;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_layout);
    ButterKnife.bind(MainActivity.this);
  }

  @NonNull @OnClick(R.id.gallery_button) protected void onGalleryClick() {
    GalleryActivity.navigateToGallery(MainActivity.this);

    /*ProcessBuilder processBuilder =
        new ProcessBuilder("adb shell am start -W com.smartdengg.aopexapmle/.MainActivity");
    try {
      Process process = processBuilder.start();
      InputStream inputStream = process.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder buffer = new StringBuilder();
      String line = "";
      while ((line = in.readLine()) != null) {
        buffer.append(line);
      }
      String string = buffer.toString();
      Log.d(TAG, string);
    } catch (IOException e) {
      e.printStackTrace();
    }*/
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(MainActivity.this);
  }
}
