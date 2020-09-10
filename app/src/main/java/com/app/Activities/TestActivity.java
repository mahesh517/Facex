package com.app.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.Database.DatabaseHelper;
import com.app.Utils.FaceRecongintion;
import com.app.Utils.Users;
import com.app.Utils.onFaceRecognition;
import com.app.facexterminalsdk.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import tgio.rncryptor.RNCryptorNative;

public class TestActivity extends AppCompatActivity implements onFaceRecognition {


    LinearLayout register, search;
    DatabaseHelper databaseHelper;
    FaceRecongintion faceRecongintion;
    ImageView my_image_view, user_pic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        databaseHelper = new DatabaseHelper(this);
        register = findViewById(R.id.register);
        search = findViewById(R.id.search);
        user_pic = findViewById(R.id.user_pic);
        faceRecongintion = new FaceRecongintion(TestActivity.this, R.id.action_container, TestActivity.this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                faceRecongintion.FaceRegistrtion();

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Users> usersList = databaseHelper.getUSerVectors();
                List<String> vectors = new ArrayList<>();
                for (int i = 0; i < usersList.size(); i++) {
                    final String[] vector = {usersList.get(i).getVector()};
                    vectors.add(vector[0]);
                }


                faceRecongintion.FaceSearch(vectors);
            }
        });


    }


    @Override
    public void onSuccess(String vector, Bitmap bitmap) {

//        my_image_view.setImageBitmap(bitmap);

        String user_photo = convertBitmapToBase64(bitmap);


        Log.e("vector", vector);

        Users users = new Users();
        Users user1 = new Users();
        users.setName("Bachu mahesh");
        users.setEmpid("emp1");
        user1.setEmpid("emp1");
        user1.setUserphoto(user_photo);
        user1.setVector(vector);
        users.setImage_url("empty");
        users.setDirty("false");
        databaseHelper.addUser(users);
        databaseHelper.addUserPhoto(user1);


        Log.e("vector_onSuccess", vector);
    }

    @Override
    public void onError(String error) {


        Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSearchSuccess(int position, String distance, final Bitmap bitmap) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                user_pic.setImageBitmap(bitmap);
            }
        });
        Log.e("position", String.valueOf(position));
        Log.e("distance", distance);

    }

    @Override
    public void onSearchError(String message) {

        Log.e("onSearchError", message);

        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();

    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
