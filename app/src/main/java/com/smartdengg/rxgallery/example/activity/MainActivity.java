package com.smartdengg.rxgallery.example.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.rxgallery.example.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();
  private String mCurrentPhotoPath;

  //private Subscription subscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_layout);
    ButterKnife.bind(MainActivity.this);

    /*subscription = Observable.fromAsync(new Action1<AsyncEmitter<Cursor>>() {
      @Override public void call(AsyncEmitter<Cursor> asyncEmitter) {

        CompositeSubscription compositeSubscription = new CompositeSubscription();

        View.OnClickListener clickListener = new View.OnClickListener() {
          @Override public void onClick(View v) {
            System.out.println("onClick");
          }
        };

        button.setOnClickListener(clickListener);

        Scheduler.Worker worker = Schedulers.computation().createWorker();
        worker.schedule(new Action0() {
          @Override public void call() {
            System.out.println("worker.call");
          }
        });

        compositeSubscription.add(worker);
        compositeSubscription.add(BooleanSubscription.create(new Action0() {
          @Override public void call() {
            button.setOnClickListener(null);
            System.out.println("onUnsubscribe");
          }
        }));

        asyncEmitter.setCancellation(new AsyncEmitter.Cancellable() {
          @Override public void cancel() throws Exception {

          }
        });

        asyncEmitter.setSubscription(compositeSubscription);
      }
    }, AsyncEmitter.BackpressureMode.BUFFER).subscribe();*/
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

  @NonNull @OnClick(R.id.take_button) protected void onTakeClick() {

    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    try {
      File imageFile = createImageFile();
      mCurrentPhotoPath = imageFile.getAbsolutePath();
      Log.d(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);
      takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
    } catch (IOException e) {
      e.printStackTrace();
    }

    startActivityForResult(takePictureIntent, 1);
  }

  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
    String imageFileName = "IMG_" + timeStamp + "_";
    File albumF = getAlbumDir();
    return File.createTempFile(imageFileName, ".jpg", albumF);
  }

  @TargetApi(Build.VERSION_CODES.FROYO) private File getAlbumDir() {
    File storageDir = null;

    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

      //storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "RxGallery");

      storageDir =
          new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
              "Rx-Gallery");

      if (storageDir != null) {
        if (!storageDir.mkdirs()) {
          if (!storageDir.exists()) {
            Log.d(TAG, "failed to create directory");
            return null;
          }
        }
      }
    } else {
      Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
    }

    return storageDir;
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    flushPic();
  }

  private void flushPic() {
    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    File f = new File(mCurrentPhotoPath);
    Uri contentUri = Uri.fromFile(f);
    mediaScanIntent.setData(contentUri);
    this.sendBroadcast(mediaScanIntent);
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    //subscription.unsubscribe();
    ButterKnife.unbind(MainActivity.this);
  }
}
