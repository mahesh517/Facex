package com.app.Utils;

import android.graphics.Bitmap;

public interface LivenessListenerColour {
    public void livenessSuccess(Boolean live, Bitmap bitmap);

    public void livenessError(Boolean live, String error);
}
