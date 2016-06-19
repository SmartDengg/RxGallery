package com.squareup.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.util.Base64;
import java.io.IOException;

/**
 * Created by SmartDengg on 2016/6/19.
 */
public class DataUriRequestHandler extends RequestHandler {

    private static final String DATA_SCHEME = "data";
    private static final int RETRY_COUNT = 3;

    @Override
    public boolean canHandleRequest(Request data) {
        String scheme = data.uri.getScheme();
        return scheme.equalsIgnoreCase(DATA_SCHEME);
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {

        String uri = request.uri.toString();
        String imageDataBytes = uri.substring(uri.indexOf(",") + 1);
        byte[] bytes = Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if (bitmap == null) return null;

        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }

    @Override
    int getRetryCount() {
        return RETRY_COUNT;
    }

    @Override
    boolean shouldRetry(boolean airplaneMode, NetworkInfo info) {
        return info == null || info.isConnected();
    }

    @Override
    boolean supportsReplay() {
        return true;
    }

}
