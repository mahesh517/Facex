package com.app.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;


public class OnGetImageListener implements OnImageAvailableListener {
    private static final boolean SAVE_PREVIEW_BITMAP = true;

    private static final int INPUT_SIZE = 224;
    private static final String TAG = "liveness.listener";


    private float FRAMES = 0;
    private int INDEX = 0;
    private int mPreviewWdith = 0;
    private int mPreviewHeight = 0;
    private byte[][] mYUVBytes;
    private int[] mRGBBytes = null;
    private Bitmap mRGBframeBitmap = null;
    private Bitmap mCroppedBitmap = null;
    private Bitmap sendbackBitmap;
    private boolean mIsComputing = false;
    private boolean CallbackTriggered = false;

    private Float leftBound = null;
    private Float rightBound = null;
    private Float topBound = null;
    private Float bottomBound = null;

    private float leftFace;
    private float rightFace;
    private float topFace;
    private float bottomFace;

    private Handler mInferenceHandler;
    private Context mContext;
    FaceDetector mFaceDet;
    private InfoView mTransparentTitleView;
    private ImageView imageView;
    private LivenessListenerColour livenessListener;
    private Paint mFaceLandmardkPaint;
    private Frame frame;
    private Fragment fragment;
    final Handler handler2 = new Handler();
    private boolean ACTION_START = false;
    private boolean success = false;
    private SparseArray<Face> results;
    private Face mFace;
    private ByteBuffer nv21byteArray;
    Map<String, Boolean> result = new HashMap<>();

    public OnGetImageListener(Fragment fragment) {
        this.fragment = fragment;
    }

    public void initialize(final Context context, final InfoView scoreView, final Handler handler, final LivenessListenerColour listener, final ImageView image) {

        this.livenessListener = listener;
        this.mContext = context;
        this.mTransparentTitleView = scoreView;
        this.mInferenceHandler = handler;
        imageView = image;
        mFaceDet = createFaceDetector(mContext);
        mFaceLandmardkPaint = new Paint();
        mFaceLandmardkPaint.setColor(Color.GREEN);
        mFaceLandmardkPaint.setStrokeWidth(2);
        mFaceLandmardkPaint.setStyle(Paint.Style.STROKE);


    }

    public void deInitialize() {
        synchronized (OnGetImageListener.this) {
            if (mFaceDet != null) {
                mFaceDet.release();
            }
        }
    }

    @NonNull
    private FaceDetector createFaceDetector(Context context) {

        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(true)
                .build();
        if (!detector.isOperational()) {

            Log.w(TAG, "Face detector dependencies are not yet available.");

            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(mContext, "Low Storage", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Low Storage");
            }
        }
        return detector;
    }


    public Bitmap RotateBitmap(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }
            if (mIsComputing) {
                image.close();
                return;
            }
            mIsComputing = true;

            final Plane[] planes = image.getPlanes();

            if (mPreviewWdith != image.getWidth() || mPreviewHeight != image.getHeight()) {

                mPreviewWdith = image.getWidth();
                mPreviewHeight = image.getHeight();


                mRGBBytes = new int[mPreviewWdith * mPreviewHeight];
                mRGBframeBitmap = Bitmap.createBitmap(mPreviewWdith, mPreviewHeight, Config.ARGB_8888);
                mCroppedBitmap = Bitmap.createBitmap(mPreviewWdith, mPreviewHeight, Config.ARGB_8888);


                mYUVBytes = new byte[planes.length][];
                for (int i = 0; i < planes.length; ++i) {
                    mYUVBytes[i] = new byte[planes[i].getBuffer().capacity()];
                }
            }

            for (int i = 0; i < planes.length; ++i) {
                planes[i].getBuffer().get(mYUVBytes[i]);
            }

            final int yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            nv21byteArray = ByteBuffer.wrap(PhotoUtils.YUV420toNV21(image));
            PhotoUtils.convertYUV420ToARGB8888(
                    mYUVBytes[0],
                    mYUVBytes[1],
                    mYUVBytes[2],
                    mPreviewWdith,
                    mPreviewHeight,
                    yRowStride,
                    uvRowStride,
                    uvPixelStride,
                    mRGBBytes);
            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            Log.e(TAG, "Exception!", e);
            return;
        }

        mRGBframeBitmap.setPixels(mRGBBytes, 0, mPreviewWdith, 0, 0, mPreviewWdith, mPreviewHeight);
        mCroppedBitmap = RotateBitmap(mRGBframeBitmap);


        mInferenceHandler.post(new Runnable() {
            @Override
            public void run() {

                synchronized (OnGetImageListener.this) {

                    frame = new Frame.Builder().setBitmap(mCroppedBitmap).build();
//                    frame = new Frame.Builder().setImageData(nv21byteArray, mPreviewWdith, mPreviewHeight, ImageFormat.NV21).setRotation(1).build();
//
                    long time1 = System.currentTimeMillis();
                    results = mFaceDet.detect(frame);
//                    results = mFaceDet.detect(frame);
                    long time2 = System.currentTimeMillis();

//                    Log.e("TIME", (time2 - time1) + "ms");
//                    frame = new Frame.Builder().setImageData(nv21byteArray, mPreviewWdith, mPreviewHeight, ImageFormat.NV21).setRotation(1).build();
//                    results = mFaceDet.detect(frame);

                }
                try {
                    if (results != null) {
                        switch (results.size()) {
                            case 0:
                                if (!ACTION_START) {
                                    mTransparentTitleView.setText("Keep your face within the box");
                                }

                                livenessListener.livenessError(false, "");
                                mFace = null;
                                break;
                            case 1:
                                if (!ACTION_START) {
                                    mTransparentTitleView.setText("");
                                    ACTION_START = true;
                                }
                                mFace = results.valueAt(0);
                                break;
                        }


                        if (mFace != null) {

                            float angleY = mFace.getEulerY();
                                float angleZ = mFace.getEulerZ();
                            if (angleY > -10 && angleY < 10 && angleZ > -10 && angleZ < 10) {

                                livenessListener.livenessSuccess(true, mCroppedBitmap);


                            } else {

                                mTransparentTitleView.setText("");

                                livenessListener.livenessError(false, "");
                            }

//                            if (checkBounds()) {
//
//                                mTransparentTitleView.setText("");
//                                float angleY = mFace.getEulerY();
//                                float angleZ = mFace.getEulerZ();
//
//
//                                if (angleY > -10 && angleY < 10 && angleZ > -10 && angleZ < 10) {
//
//                                    livenessListener.livenessSuccess(true, mCroppedBitmap);
//
//
//                                } else {
//
//                                    mTransparentTitleView.setText("");
//
//                                    livenessListener.livenessError(false, "");
//                                }
//                            } else {
//                                mTransparentTitleView.setText("Please  show face within circle");
//                            }


//                            mTransparentTitleView.setText("Y:" + mFace.getEulerY() + "," + "Z:" + mFace.getEulerZ());
                        } else {
                            livenessListener.livenessError(false, "");
                        }
                    } else {
                        livenessListener.livenessError(false, "");
                    }

                } catch (Exception e) {
                    livenessListener.livenessError(false, e.getLocalizedMessage());
                }
                mIsComputing = false;
            }
        });


        success = true;

    }

    private void saveImage(Bitmap bitmap) {

//        Log.e("save_image", "----");
        String filename = System.currentTimeMillis() + ".png";
        File sd = new File(Environment.getExternalStorageDirectory() + "/Test");

        if (!sd.exists()) {
            sd.mkdirs();
        }
        File dest = new File(sd, filename);

        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", "----" + e.getLocalizedMessage());
        }
    }

    public Boolean checkBounds() {
        if (leftBound == null) {
            leftBound = (mPreviewHeight - mPreviewHeight * 0.8f) / 2;
            rightBound = leftBound + mPreviewHeight * 0.8f;
            topBound = (mPreviewWdith - mPreviewWdith * 0.5f) / 2;
            bottomBound = topBound + mPreviewWdith * 0.5f;
        }
        leftFace = mFace.getPosition().x;
        rightFace = mFace.getPosition().x + mFace.getWidth();
        topFace = mFace.getPosition().y;
        bottomFace = mFace.getPosition().y + mFace.getHeight();

        Log.e("boundv", leftBound + " " + leftFace + "  " + rightBound + " " + rightFace + "  " + topBound + " " + topFace + "  " + bottomBound + " " + bottomFace + " " + mFace.getHeight() + " " + mFace.getWidth() + " " + mPreviewWdith);

        if (leftBound < leftFace && rightBound > rightFace && topBound < topFace && bottomBound > bottomFace) {
            Log.e("bound", "in bounds");
            return true;
        }
        return false;
    }

}