package com.app.MlKitUtils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class ImageUtils {
    public static Bitmap prepareImageForClassification(Bitmap bitmap) {
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        matrix.postScale(-1, 1, 112, 112);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        Bitmap finalBitmap = Bitmap.createScaledBitmap(
                bitmap,
                ModelConfig.INPUT_IMG_SIZE_WIDTH,
                ModelConfig.INPUT_IMG_SIZE_HEIGHT,
                false);
        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawBitmap(finalBitmap, 0, 0, paint);
        return finalBitmap;
    }
}
