package com.smartdengg.rxgallery.example.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.rxgallery.example.R;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(MainActivity.this);
    }

    @NonNull
    @OnClick(R.id.gallery_button)
    protected void onGalleryClick() {

        Observable.interval(100, TimeUnit.MILLISECONDS)
                  .take(10)
                  .buffer(Observable.interval(250, TimeUnit.MILLISECONDS), new Func1<Long, Observable<?>>() {
                      @Override
                      public Observable<?> call(Long i) {
                          System.out.println("i = [" + i + "]");
                          return Observable.timer(200, TimeUnit.MILLISECONDS)
                                           .map(new Func1<Long, Long>() {
                                               @Override
                                               public Long call(Long aLong) {
                                                   System.out.println("aLong = [" + aLong + "]");
                                                   return aLong;
                                               }
                                           });
                      }
                  })
                  .subscribe(new Action1<List<Long>>() {
                      @Override
                      public void call(List<Long> o) {
                          System.out.println(o);
                      }
                  });

        //GalleryActivity.navigateToGallery(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(MainActivity.this);
    }
}
