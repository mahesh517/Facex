package com.app.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.fragment.app.FragmentActivity;

import com.app.CustomDialogs.ImagePickerDailog;
import com.app.Fragments.CameraFragment;
import com.app.MlKitUtils.Classifier;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.ArrayList;
import java.util.List;


public class FaceRecongintion {

    public static Classifier classifier;

    Context context;
    onFaceRecognition FaceRecognition;
    private Activity activity;
    private int resId;
    Bitmap currentBitmap;
    private ArrayList<String> search_vectors = new ArrayList<String>();


    public FirebaseVisionFaceDetectorOptions options;

    public FirebaseVisionFaceDetector detector;
    public ProgressDialog mProgressDialog;

    ImagePickerDailog imagePickerDailog;

    public FaceRecongintion(Context context, int resId, onFaceRecognition faceRecognitionListner) {

        this.context = context;
        this.FaceRecognition = (onFaceRecognition) context;
        this.activity = (Activity) context;
        this.resId = resId;
    }

    public void FaceRegistrtion() {

        ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction().replace(resId, CameraFragment.newInstance((onFaceRecognition) context, 1)).commit();

    }

    public void FaceSearch(List<String> strings) {

        ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction().replace(resId, CameraFragment.newInstance((onFaceRecognition) context, 2, strings)).commit();

    }


}
