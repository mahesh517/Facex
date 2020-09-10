package com.app.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.app.CustomDialogs.ImagePickerDailog;
import com.app.MlKitUtils.Classifier;
import com.app.MlKitUtils.ImageUtils;
import com.app.Utils.AutoFitTextureView;
import com.app.Utils.FileUtils;
import com.app.Utils.InfoView;
import com.app.Utils.LivenessListenerColour;
import com.app.Utils.OnGetImageListener;
import com.app.Utils.VisionImage;
import com.app.Utils.onFaceRecognition;
import com.app.facexterminalsdk.R;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import tgio.rncryptor.RNCryptorNative;


public class CameraFragment extends Fragment {

    private static final int MINIMUM_PREVIEW_SIZE = 320;
    private static final String TAG = "liveness.frag";
    IPreviewListener iPreviewListener;
    private InfoView mScoreView;
    private ImageView imageView;
    ImagePickerDailog imagePickerDailog;

    public Classifier classifier;
    float[] recognitions = new float[128];
    public FirebaseVisionFaceDetectorOptions options;

    public FirebaseVisionFaceDetector detector;
    public ProgressDialog mProgressDialog;

    FrameLayout f_view;

    Button capture;

    public static int SESSION_TYPE;

    static List<float[]> vector_List;


    public static List<String> user_vectors;

    ImageView user_pic;

    public Bitmap currentBitmap;
    TextView warning_tv;
    private static onFaceRecognition onfaceListener;

    boolean first_image = true;

    LivenessListenerColour registerlistner;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final String FRAGMENT_DIALOG = "dialog";


    FaceDetector faceDetector;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(
                        final SurfaceTexture texture, final int width, final int height) {
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(
                        final SurfaceTexture texture, final int width, final int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };

    private String cameraId;

    private AutoFitTextureView textureView;

    private CameraCaptureSession captureSession;

    private CameraDevice cameraDevice;

    private Size previewSize;

    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final CameraDevice cd) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    cameraDevice = cd;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(final CameraDevice cd) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;

                    if (mOnGetPreviewListener != null) {
                        mOnGetPreviewListener.deInitialize();
                    }
                }

                @Override
                public void onError(final CameraDevice cd, final int error) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;
                    final Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }

                    if (mOnGetPreviewListener != null) {
                        mOnGetPreviewListener.deInitialize();
                    }
                }
            };

    private HandlerThread backgroundThread;

    private Handler backgroundHandler;

    private HandlerThread inferenceThread;

    private Handler inferenceHandler;

    private ImageReader previewReader;
    public SurfaceView surfaceview;

    private CaptureRequest.Builder previewRequestBuilder;

    private CaptureRequest previewRequest;

    private final Semaphore cameraOpenCloseLock = new Semaphore(1);

    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @SuppressLint("LongLogTag")
    private static Size chooseOptimalSize(final Size[] choices, final int width, final int height, final Size aspectRatio) {
        final List<Size> bigEnough = new ArrayList<>();
        for (final Size option : choices) {
            if (option.getHeight() >= MINIMUM_PREVIEW_SIZE && option.getWidth() >= MINIMUM_PREVIEW_SIZE) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    public static CameraFragment newInstance(onFaceRecognition onFaceRecognition, int type, List<String> vectors) {

        onfaceListener = onFaceRecognition;

        SESSION_TYPE = type;

        user_vectors = vectors;

        decryptvectors();

        return new CameraFragment();
    }

    private static void decryptvectors() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                vector_List = FileUtils.decryptVectors(user_vectors);

            }
        }).start();

    }

    public static CameraFragment newInstance(onFaceRecognition onFaceRecognition, int type) {

        onfaceListener = onFaceRecognition;

        SESSION_TYPE = type;


        return new CameraFragment();
    }

    private static void onRuntime(Boolean live) {


    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_connection, container, false);
    }


    public void setVisiblity(final boolean status) {


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                if (status && SESSION_TYPE == 1) {
                    capture.setVisibility(View.VISIBLE);
                    warning_tv.setVisibility(View.GONE);
                } else if (status && SESSION_TYPE != 1) {
                    warning_tv.setVisibility(View.GONE);
                    capture.setVisibility(View.GONE);
                    if (first_image) {

                        if (classifier == null) {
                            openClassifier(getActivity().getApplicationContext());

                            processNewImage(currentBitmap);

                        } else {
                            processNewImage(currentBitmap);
                        }


                        first_image = false;
                    }

                } else {
                    warning_tv.setVisibility(View.VISIBLE);
                    currentBitmap = null;
                    capture.setVisibility(View.GONE);
                }


            }


        });


    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {


        if (OpenCVLoader.initDebug()) {
        }


        if (classifier == null) {
            openClassifier(getActivity().getApplicationContext());
        }

        textureView = view.findViewById(R.id.texture);
        mScoreView = view.findViewById(R.id.results);
        surfaceview = view.findViewById(R.id.surfaceView);

        f_view = view.findViewById(R.id.f_view);
        capture = view.findViewById(R.id.capture);
        warning_tv = view.findViewById(R.id.warning_tv);

        user_pic = view.findViewById(R.id.user_pic);

        showImagePickerDialog();

        faceDetector = new FaceDetector.Builder(getActivity())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();


        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                closeCamera();
                showLoading();

                if (classifier == null) {
                    openClassifier(getActivity().getApplicationContext());

                    processNewImage(currentBitmap);
                } else {
                    processNewImage(currentBitmap);
                }

            }
        });

        surfaceview.setZOrderOnTop(true);
        SurfaceHolder mHolder = surfaceview.getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                float left = (surfaceview.getWidth() - surfaceview.getWidth() * 0.8f) / 2;
                float right = left + surfaceview.getWidth() * 0.8f;
                float top = (surfaceview.getHeight() - surfaceview.getHeight() * 0.5f) / 2;
                float bottom = top + surfaceview.getHeight() * 0.5f;
                surfaceview.getHeight();
                Canvas canvas = surfaceHolder.lockCanvas();

                if (canvas == null) {
                    Log.e("TAG", "Cannot draw onto the canvas as it's null");
                } else {
                    Paint myPaint = new Paint();
                    myPaint.setColor(Color.rgb(20, 30, 50));
                    myPaint.setStrokeWidth(10);
                    myPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawOval(left, top + 100, right, bottom - 50, myPaint);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });


        iPreviewListener = new IPreviewListener() {

            @Override
            public void onPreviewEvent(final long height, final long width) {
//                Log.e(TAG,"THIS WORKSSSSSSSSSSSSSSSSSSSSS"+height+" "+width);

            }
        };
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @SuppressLint("LongLogTag")
    private void setUpCameraOutputs(final int width, final int height) {
        final Activity activity = getActivity();
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            @SuppressLint("UseSparseArrays") SparseArray<Integer> cameraFaceTypeMap = new SparseArray<>();
            // Check the facing types of camera devices
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    if (cameraFaceTypeMap.get(CameraCharacteristics.LENS_FACING_FRONT) != null) {
                        cameraFaceTypeMap.append(CameraCharacteristics.LENS_FACING_FRONT, cameraFaceTypeMap.get(CameraCharacteristics.LENS_FACING_FRONT) + 1);
                    } else {
                        cameraFaceTypeMap.append(CameraCharacteristics.LENS_FACING_FRONT, 1);
                    }
                }

            }

            Integer num_facing_front_camera = cameraFaceTypeMap.get(CameraCharacteristics.LENS_FACING_FRONT);
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                // If facing back camera or facing external camera exist, we won't use facing front camera
                if (num_facing_front_camera != null && num_facing_front_camera > 0) {
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        continue;
                    }
                }

                final StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                final Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)), new CompareSizesByArea());


                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                final int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
                } else {
                    textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
                }
                iPreviewListener.onPreviewEvent(previewSize.getHeight(), previewSize.getWidth());

                this.cameraId = cameraId;
                return;
            }
        } catch (final CameraAccessException e) {
            Log.e(TAG, "Exception! :" + e);
        } catch (final NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.app_name)).show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }


    @SuppressLint("LongLogTag")
    private void openCamera(final int width, final int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        final Activity activity = getActivity();
        final CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (final CameraAccessException e) {
            Log.e(TAG, "Exception!" + e);
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }


    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != previewReader) {
                previewReader.close();
                previewReader = null;
            }
            if (null != mOnGetPreviewListener) {
                mOnGetPreviewListener.deInitialize();
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("ImageListener");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        inferenceThread = new HandlerThread("InferenceThread");
        inferenceThread.start();
        inferenceHandler = new Handler(inferenceThread.getLooper());
    }

    @SuppressLint("LongLogTag")
    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        inferenceThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;

            inferenceThread.join();
            inferenceThread = null;
            inferenceThread = null;
        } catch (final InterruptedException e) {
            Log.e(TAG, "error" + e);
        }
    }

    private final OnGetImageListener mOnGetPreviewListener = new OnGetImageListener(this);

    private final CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final CaptureResult partialResult) {
                }

                @Override
                public void onCaptureCompleted(
                        final CameraCaptureSession session,
                        final CaptureRequest request,
                        final TotalCaptureResult result) {
                }
            };

    @SuppressLint("LongLogTag")
    private void createCameraPreviewSession() {
        try {
            final SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            final Surface surface = new Surface(texture);

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            Log.e(TAG, "y" + previewSize.getWidth() + "x" + previewSize.getHeight());

            previewReader =
                    ImageReader.newInstance(
                            previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);

            previewReader.setOnImageAvailableListener(mOnGetPreviewListener, backgroundHandler);
            previewRequestBuilder.addTarget(previewReader.getSurface());

            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, previewReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(final CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }

                            captureSession = cameraCaptureSession;
                            try {
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(
                                        previewRequest, captureCallback, backgroundHandler);
                            } catch (final CameraAccessException e) {
                                Log.e(TAG, "Exception!" + e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    },
                    null);
        } catch (final CameraAccessException e) {
            Log.e(TAG, "Exception!" + e);


        }


        mOnGetPreviewListener.initialize(getActivity(), mScoreView, inferenceHandler, registerlistner, imageView);


    }

    private void configureTransform(final int viewWidth, final int viewHeight) {
        final Activity activity = getActivity();
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            final float scale =
                    Math.max(
                            (float) viewHeight / previewSize.getHeight(),
                            (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(final Size lhs, final Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    public static class ErrorDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(final String message) {
            final ErrorDialog dialog = new ErrorDialog();
            final Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(
                            android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, final int i) {
                                    activity.finish();
                                }
                            })
                    .create();
        }
    }

    public interface IPreviewListener {

        void onPreviewEvent(long height, long width);
    }

    private void showImagePickerDialog() {


        imagePickerDailog = new ImagePickerDailog(getActivity(), R.style.AppTheme, new ImagePickerDailog.ImageInterface() {
            @Override
            public void imagestatus(int position) {


                if (position == 1) {
                    f_view.setVisibility(View.VISIBLE);


                    startListner();
                } else if (position == 2) {

                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, 121);

                } else if (position == 3) {
                    onfaceListener.onError("User cancelled");

                    closeFragment();


                }

                Log.e("position", "--" + position);
            }
        });

        imagePickerDailog.show();

    }


    private void startListner() {
        registerlistner = new LivenessListenerColour() {
            @Override
            public void livenessSuccess(Boolean live, Bitmap bitmap) {


                if (live) {
                    currentBitmap = bitmap;
                }
                setVisiblity(live);

            }

            @Override
            public void livenessError(Boolean live, String error) {

                setVisiblity(live);
            }
        };
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 121 && resultCode == Activity.RESULT_OK) {
            String path = getPathFromCameraData(data, this.getActivity());
            Log.i("PICTURE", "Path: " + path);

            Uri contentURI = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), contentURI);

                user_pic.setImageBitmap(bitmap);

                if (classifier == null) {
                    openClassifier(getActivity().getApplicationContext());
                    processNewImage(currentBitmap);

                } else {
                    processNewImage(currentBitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public static String getPathFromCameraData(Intent data, Context context) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        Bitmap OutImage = Bitmap.createScaledBitmap(inImage, 1000, 1000, true);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), OutImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getActivity().getContentResolver() != null) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }


    public void showLoading() {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Loading...Please wait");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void dismissLoading() {

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void processImage(final Bitmap bitmap) {


        Log.e("Process", "-----");

        long startTimeForReference = SystemClock.uptimeMillis();


        FirebaseVisionImage visionImage = VisionImage.imageFromBitmap(bitmap);
        Task<List<FirebaseVisionFace>> detectInImageTask = detector.detectInImage(visionImage);
        detectInImageTask.addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {

                long faceDetectTime = SystemClock.uptimeMillis();


                if (firebaseVisionFaces.size() > 0) {
                    FirebaseVisionFace face = firebaseVisionFaces.get(0);
                    FirebaseVisionFaceContour contour = face.getContour(FirebaseVisionFaceContour.FACE);
                    Bitmap bitmap1 = null;
                    float x = 0.0f;
                    float y = 0.0f;
                    float x1 = 0.0f;
                    float y1 = 0.0f;
                    for (int j = 0; j < contour.getPoints().size(); j++) {

                        FirebaseVisionPoint point = contour.getPoints().get(j);


                        if (j == 0) {
                            y = point.getY();
                        }
                        if (j == 8) {
                            x = point.getX();
                        }
                        if (j == 18) {
                            y1 = point.getY();
                        }
                        if (j == 28) {
                            x1 = point.getX();
                        }

                    }

                    if (contour.getPoints().size() > 0) {
                        int width = (int) (x - x1);
                        int height = (int) (y1 - y);


                        try {
                            bitmap1 = Bitmap.createBitmap(bitmap, (int) x1, (int) y, width, height);


                        } catch (Exception e) {
                            Log.e("catch", e.getLocalizedMessage());
                            bitmap1 = null;
                            first_image = true;
                        }
                        Bitmap preprocessedImage = null;


                        long detectFaceTime = SystemClock.uptimeMillis();


                        if (bitmap1 != null) {
                            preprocessedImage = ImageUtils.prepareImageForClassification(bitmap1);
                        }

                        long preprocessedImageTime = SystemClock.uptimeMillis();


                        if (preprocessedImage != null) {


                            recognitions = classifier.recognizeImage(preprocessedImage);


                            if (SESSION_TYPE == 1) {
                                final String[] vector = {getVector(recognitions)};
                                RNCryptorNative.encryptAsync(vector[0], "mahesh", new RNCryptorNative.RNCryptorNativeCallback() {
                                    @Override
                                    public void done(String result, Exception e) {
                                        vector[0] = result;
                                    }
                                });
                                onfaceListener.onSuccess(vector[0], bitmap1);
                            } else if (SESSION_TYPE != 1) {

                                if (vector_List.size() > 0) {
                                    List<String> search_result = FileUtils.searchUser(recognitions, vector_List);
                                    if (search_result.get(0).equalsIgnoreCase("-1")) {
                                        onfaceListener.onSearchError("No match found");
                                    } else {
                                        onfaceListener.onSearchSuccess(Integer.parseInt(search_result.get(0)), search_result.get(1), bitmap1);
                                    }
                                } else {
                                    onfaceListener.onSearchError("User List empty");
                                }

                            }

                        } else {
                            onfaceListener.onError("unable process image");
                            first_image = true;
                        }


                    } else {

                        onfaceListener.onError("unable process image");
                        first_image = true;
                    }

                } else {
                    onfaceListener.onError("no face found");
                    first_image = true;
                }


                dismissLoading();
                closeFragment();
                Log.e("onSuccess", "-----");
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.e("onCanceled", "-----");
                dismissLoading();
                closeFragment();
                onfaceListener.onError("User canclled");
            }
        }).addOnCompleteListener(new OnCompleteListener<List<FirebaseVisionFace>>() {
            @Override
            public void onComplete(@NonNull Task<List<FirebaseVisionFace>> task) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("onFailure", "-----" + e.getMessage());

                dismissLoading();
                closeFragment();
                onfaceListener.onError("onFailure");
            }
        });


    }


    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private String getVector(float[] name) {
        StringBuilder sb = new StringBuilder();
        for (Float n : name) {
            if (sb.length() > 0) sb.append(',');
            sb.append(n);
        }
        return sb.toString();
    }

    private File storeImage(Bitmap image, String filename) {
        File pictureFile = getOutputMediaFile(filename);
        if (pictureFile == null) {
            Log.d("TAG", "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("TAG", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("TAG", "Error accessing file: " + e.getMessage());
        }

        return pictureFile;
    }

    private File getOutputMediaFile(String filename) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/FaceRec"
                + "/Files");


        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        File mediaFile;
        String mImageName = filename + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    private void closeFragment() {
        getFragmentManager().beginTransaction().remove(CameraFragment.this).commit();

    }


    private void processNewImage(Bitmap bitmap) {


        long startTimeForReference = SystemClock.uptimeMillis();


        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);


        Bitmap bitmap1 = null;

        boolean image_dark = false;
        if (faces.size() > 0) {
            Face face = faces.valueAt(0);


            bitmap1 = FacePareProcess.Facerotation(bitmap, bitmap, face);

            bitmap1 = ImageUtils.prepareImageForClassification(bitmap1);

        }

        if (bitmap1 != null) {

            recognitions = classifier.recognizeImage(bitmap1);

            long endTimeForReference = SystemClock.uptimeMillis();


            String basebitamp = convertBitmapToBase64(bitmap1);

            File new_file = storeImage(bitmap, "test");
            if (SESSION_TYPE == 1) {
                final String[] vector = {getVector(recognitions)};
                RNCryptorNative.encryptAsync(vector[0], "mahesh", new RNCryptorNative.RNCryptorNativeCallback() {
                    @Override
                    public void done(String result, Exception e) {
                        vector[0] = result;
                    }
                });
                onfaceListener.onSuccess(vector[0], bitmap1);
            } else if (SESSION_TYPE != 1) {

                if (vector_List.size() > 0) {
                    List<String> search_result = FileUtils.searchUser(recognitions, vector_List);
                    if (search_result.get(0).equalsIgnoreCase("-1")) {
                        onfaceListener.onSearchError("No match found");
                    } else {
                        onfaceListener.onSearchSuccess(Integer.parseInt(search_result.get(0)), search_result.get(1), bitmap1);
                    }
                } else {
                    onfaceListener.onSearchError("User List empty");
                }

            }

        } else {
            onfaceListener.onError("unable find face in  image");
            first_image = true;
        }


        dismissLoading();
        closeFragment();

    }


    public void openClassifier(final Context context) {


        Log.e("classifier", "open");

        new Thread(new Runnable() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createClassifier(context);
                    }
                });
            }
        }).start();

    }

    public void createClassifier(Context context) {

        Log.e("classifier", "before");


        if (classifier == null) {


            Log.e("classifier", "after");


            AssetManager am = context.getAssets();
            InputStream is = null;
            try {
                is = am.open("face_encrypt.tflite");
            } catch (IOException e) {
                Log.e("io_1", e.getMessage());
            }


            File inputfile = FileUtils.createFileFromInputStream(is);

            File decryptedFile = new File(Environment.getExternalStorageDirectory() + "/new_decrypt.tflite");

            try {
                RNCryptorNative.decryptFile(inputfile, decryptedFile, "mahesh");


                inputfile.delete();
            } catch (IOException e) {
                Log.e("io_2", e.getMessage());
            }


            try {
                classifier = Classifier.classifier(decryptedFile);
            } catch (IOException e) {
                Log.e("io_3", e.getMessage());


            }
        }


    }


}
