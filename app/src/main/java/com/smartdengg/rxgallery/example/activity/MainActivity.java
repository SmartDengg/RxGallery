package com.smartdengg.rxgallery.example.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.smartdengg.rxgallery.example.R;

public class MainActivity extends AppCompatActivity {

    public enum LoadedFrom {
        MEMORY(Color.GREEN),
        DISK(Color.BLUE),
        NETWORK(Color.RED);

        final int debugColor;

        private LoadedFrom(int debugColor) {
            this.debugColor = debugColor;
        }
    }

    LoadedFrom loadedFrom = LoadedFrom.MEMORY;
    boolean noFade = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        ButterKnife.bind(MainActivity.this);

        boolean fade = loadedFrom != LoadedFrom.MEMORY && !(noFade);

        System.out.println(loadedFrom);
        System.out.println(fade);
    }

    @NonNull
    @OnClick(R.id.gallery_button)
    protected void onGalleryClick() {
        GalleryActivity.navigateToGallery(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(MainActivity.this);
    }
}
