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
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.app.facexterminalsdk.R;


public class EncryptionCodeDialog extends Dialog {

    Context context;
    EncryptionInterface encryptionInterface;

    EditText key_et;
    TextView submit, cancel;

    public EncryptionCodeDialog(@NonNull Context context, int themeResId, EncryptionInterface encryptionInterface) {
        super(context, themeResId);

        this.context = context;
        this.encryptionInterface = encryptionInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setBackgroundDrawable(new ColorDrawable(context.getColor(R.color.dialog_traspe)));
        } else {
            getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.dialog_traspe)));
        }
        getWindow().setGravity(Gravity.CENTER);
//        setContentView(R.layout.encryption_dailog);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);


//        key_et = findViewById(R.id.encrypt);
//        submit = findViewById(R.id.submit);
//        cancel = findViewById(R.id.cancel_action);


        onClickevents();

    }

    private void onClickevents() {

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String key = key_et.getText().toString();
                if (key.equalsIgnoreCase("")) {

                    key_et.setError("Please enter valid key");
                } else {
                    if (encryptionInterface != null) {
                        encryptionInterface.onClick(true, key);
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public interface EncryptionInterface {
        void onClick(boolean status, String key);
    }
}
