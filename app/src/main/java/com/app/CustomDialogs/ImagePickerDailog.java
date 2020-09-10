package com.app.CustomDialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.app.facexterminalsdk.R;


public class ImagePickerDailog extends Dialog {

    Context context;

    ImageInterface imageInterface;

    TextView camera_tv, gallery_tv, cancle_tv;

    public ImagePickerDailog(Context context, int themeResId, ImageInterface imageInterface) {
        super(context, themeResId);

        this.context = context;
        this.imageInterface = imageInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setBackgroundDrawable(new ColorDrawable(context.getColor(R.color.dialog_traspe)));
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.dialog_traspe)));
        }
        getWindow().setGravity(Gravity.CENTER);
        setContentView(R.layout.image_picker_dialog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        super.onCreate(savedInstanceState);

        initview();

    }

    private void initview() {

        camera_tv = findViewById(R.id.camera);
        gallery_tv = findViewById(R.id.gallery);
        cancle_tv = findViewById(R.id.cancle);

        onClickevents();
    }

    private void onClickevents() {

        camera_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageInterface != null) {
                    imageInterface.imagestatus(1);
                    ImagePickerDailog.this.dismiss();
                }

            }
        });
        gallery_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageInterface != null) {
                    imageInterface.imagestatus(2);

                    ImagePickerDailog.this.dismiss();
                }

            }
        });

        cancle_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ImagePickerDailog.this.dismiss();
                if (imageInterface != null) {
                    imageInterface.imagestatus(3);

                }
            }
        });
    }

    public interface ImageInterface {
        void imagestatus(int position);
    }
}
