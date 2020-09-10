package com.app.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.app.facexterminalsdk.R;


public class InfoView extends View {
    private String mShowText;
    private final float mTextSizePx;
    private final Paint mFgPaint;
    private final Paint mBgPaint;

    public InfoView(final Context context, final AttributeSet set) {
        super(context, set);

        mTextSizePx = getResources().getDimensionPixelSize(R.dimen.fontSizeDrawText);
        mFgPaint = new Paint();
        mFgPaint.setTextSize(mTextSizePx);
        mFgPaint.setTextAlign(Paint.Align.CENTER);

        mBgPaint = new Paint();
        mFgPaint.setColor(0xccffffff);
    }

    @NonNull
    public void setText(@NonNull String text) {
        this.mShowText = text;
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {

        int x = (canvas.getWidth() / 2);
        int y = (int) ((canvas.getHeight() / 2) - ((mFgPaint.descent() + mFgPaint.ascent()) / 2));
        canvas.drawPaint(mBgPaint);

        if (mShowText != null) {
            canvas.drawText(mShowText, x, y, mFgPaint);
        }
    }
}
