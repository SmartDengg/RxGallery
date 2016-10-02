package com.smartdengg.rxgallery.example.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.rxgallery.example.R;
import rx.Subscription;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Bind(R.id.gallery_button) Button button;
  private Subscription subscription;

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

  @Override protected void onDestroy() {
    super.onDestroy();

    subscription.unsubscribe();
    ButterKnife.unbind(MainActivity.this);
  }
}
