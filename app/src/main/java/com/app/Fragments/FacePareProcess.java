package com.app.Fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

public class FacePareProcess {


    public static Bitmap Facerotation(Bitmap faceBitmap, Bitmap original, Face newFace) {


        float right_eye_x_sum = 0;
        float right_eye_y_sum = 0;
        float left_eye_x_sum = 0;
        float left_eye_y_sum = 0;

        float mouth_right_x = 0;
        float mouth_right_y = 0;
        float mouth_left_x = 0;
        float mouth_left_y = 0;
        float nose_x = 0;
        float nose_y = 0;


        float right_eye_avg_x = 0;
        float right_eye_avg_y = 0;

        float left_eye_avg_x = 0;
        float left_eye_avg_y = 0;


        Paint facePositionPaint1;

        facePositionPaint1 = new Paint();
        facePositionPaint1.setColor(Color.BLUE);
        facePositionPaint1.setTextSize(10.0f);


//        Log.e("right_eye_contour", "--" + right_eye_contour.getPoints().size());
//
//        for (int j = 0; j < right_eye_contour.getPoints().size(); j++) {
//            FirebaseVisionPoint point = right_eye_contour.getPoints().get(j);
//            right_eye_x_sum = right_eye_x_sum + point.getX();
//            right_eye_y_sum = right_eye_y_sum + point.getY();
//        }
//        for (int j = 0; j < left_eye_contour.getPoints().size(); j++) {
//            FirebaseVisionPoint point = left_eye_contour.getPoints().get(j);
//            left_eye_x_sum = left_eye_x_sum + point.getX();
//            left_eye_y_sum = left_eye_y_sum + point.getY();
//        }
//
//
//        for (int j = 0; j < contour.getPoints().size(); j++) {
//
//            FirebaseVisionPoint point = contour.getPoints().get(j);
//
//
//            if (j == 88) {
//
//                mouth_right_x = point.getX();
//                mouth_right_y = point.getY();
//
//            }
//            if (j == 98) {
//                mouth_left_x = point.getX();
//                mouth_left_y = point.getY();
//            }
//            if (j == 129) {
//                nose_x = point.getX();
//                nose_y = point.getY();
//            }

        for (int i = 0; i < newFace.getLandmarks().size(); i++) {

            Landmark landmark = newFace.getLandmarks().get(i);
            float px = landmark.getPosition().x;
            float py = landmark.getPosition().y;

            if (i == 0) {
                left_eye_avg_x = px;
                left_eye_avg_y = py;
            } else if (i == 1) {
                right_eye_avg_x = px;
                right_eye_avg_y = py;
            } else if (i == 2) {
                nose_x = px;
                nose_y = py;
            } else if (i == 4) {
                mouth_right_x = px;
                mouth_right_y = py;
            } else if (i == 5) {
                mouth_left_x = px;
                mouth_left_y = py;
            }


        }


        float[][] v1 = new float[][]{{left_eye_avg_x, left_eye_avg_y}, {right_eye_avg_x, right_eye_avg_y}, {nose_x, nose_y}, {mouth_right_x, mouth_right_y}, {mouth_left_x, mouth_left_y}};


//
        Mat dst = new Mat(5, 2, CV_32FC1);
        Mat src = new Mat(5, 2, CV_32FC1);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++)
                src.put(row, col, v1[row][col]);
        }


        float[][] v2 = new float[][]{{30.2946f + 8, 51.6963f},
                {65.5318f + 8, 51.5014f},
                {48.0252f + 8, 71.7366f},
                {33.5493f + 8, 92.3655f},
                {62.7299f + 8, 92.2041f}};

//        Log.e("v2", new Gson().toJson(v2));

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++)
                dst.put(row, col, v2[row][col]);
        }


//        Log.e("dst", dst.dump());
//        Log.e("src", src.dump());
        Mat M = FacePreprocess.similarTransform(src, dst);

//        Log.e("M_MAT", M.dump());


        Mat m = new Mat(2, 3, CV_32FC1);
        M.row(0).copyTo(m.row(0));
        M.row(1).copyTo(m.row(1));
        Mat Rotated = new Mat();
        Size size = new Size(112, 112);
        Mat frame = new Mat();
        Utils.bitmapToMat(faceBitmap, frame);
        Imgproc.warpAffine(frame, Rotated, m, size, INTER_CUBIC);
        Imgproc.resize(Rotated, Rotated, new Size(160, 160));
        Bitmap bmp = Bitmap.createBitmap(Rotated.cols(), Rotated.rows(), faceBitmap.getConfig());


        Utils.matToBitmap(Rotated, bmp);



        return bmp;


    }


}


