package com.app.Utils;

import android.graphics.Bitmap;

public interface onFaceRecognition {

    void onSuccess(String vector, Bitmap bitmap);

    void onError(String error);

    void onSearchSuccess(int position, String distance, Bitmap bitmap);

    void onSearchError(String message);
}
