package com.app.AppController;

import android.app.Application;

import com.app.MlKitUtils.Classifier;
import com.google.firebase.FirebaseApp;

public class FaceXTerminal extends Application {

    public static Classifier classifier;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(getBaseContext());


    }




}
