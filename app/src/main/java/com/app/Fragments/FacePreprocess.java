package com.app.Fragments;

import android.util.Log;

import org.opencv.BuildConfig;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import static org.opencv.core.Mat.diag;
import static org.opencv.core.Mat.eye;

public class FacePreprocess {


    public static Mat elementwiseMinus(Mat a, Mat b) {
        Mat output = new Mat(a.rows(), a.cols(), a.type());
        for (int i = 0; i < a.rows(); i++) {
            for (int j = 0; j < b.cols(); j++) {
                output.put(i, j, a.get(i, j)[0] - b.get(0, j)[0]);
            }
        }
        return output;
    }

    public static Mat meanAxis0(Mat src) {
        int num = src.rows();
        int dim = src.cols();
        Mat output = new Mat(1, dim, CvType.CV_32F);

        for (int i = 0; i < dim; i++) {
            float sum = 0;
            for (int j = 0; j < num; j++) {
                sum += src.get(j, i)[0];
            }
            output.put(0, i, sum / num);
        }
        return output;
    }

    public static Mat varAxis0(Mat src) {
        Mat temp_ = elementwiseMinus(src, meanAxis0(src));
        Core.multiply(temp_, temp_, temp_);
        return meanAxis0(temp_);
    }

    public static int MatrixRank(Mat M) {
        Mat w = new Mat(), u = new Mat(), vt = new Mat();
        Core.SVDecomp(M, w, u, vt);
        int rank = 0;

//        Log.e("w", w.dump());
//        Log.e("u", u.dump());
//        Log.e("vt", vt.dump());

        for (int i = 0; i < w.rows(); i++) {
            if (w.row(i).get(0, 0)[0] > 0.0001) {
                rank++;
            }
        }
        return rank;
    }


    public static Mat similarTransform(Mat src, Mat dst) {
        int num = src.rows();
        int dim = src.cols();

        Mat src_mean = meanAxis0(src);
        Mat dst_mean = meanAxis0(dst);

        Mat src_demean = elementwiseMinus(src, src_mean);
        Mat dst_demean = elementwiseMinus(dst, dst_mean);


        Mat A = new Mat();
        Core.gemm(dst_demean.t(), src_demean, 1, new Mat(), 0, A);
        Core.divide(A, new Scalar(num), A);

        Mat d = new Mat(dim, 1, CvType.CV_32F);
        d.setTo(new Scalar(1.0));


        if (Core.determinant(A) < 0) {
            d.put(dim - 1, 1, -1);
        }

        Mat T = eye(dim + 1, dim + 1, CvType.CV_32F);

        Mat U = new Mat(), S = new Mat(), V = new Mat();
        Core.SVDecomp(A, S, U, V);

        int rank = MatrixRank(A);
        if (rank == 0) {

            if (BuildConfig.DEBUG && !(rank == 0)) {
                throw new AssertionError("Assertion failed");
            }

        } else if (rank == dim - 1) {
            if (Core.determinant(U) * Core.determinant(V) > 0) {
                Mat temp = new Mat();
                Core.gemm(U, V, 1, new Mat(), 0, temp);
                for (int i = 0; i < dim; i++) {
                    for (int j = 0; j < dim; j++) {
                        T.put(i, j, temp.get(i, j)[0]);
                    }
                }

            } else {
                Mat temp = new Mat();
                Core.gemm(U, diag(d), 1, new Mat(), 0, temp);
                Core.gemm(temp, V, 1, new Mat(), 0, temp);
                for (int i = 0; i < dim; i++) {
                    for (int j = 0; j < dim; j++) {
                        T.put(i, j, temp.get(i, j)[0]);
                    }
                }
            }

        } else {
            Mat temp = new Mat();
            Core.gemm(U, diag(d), 1, new Mat(), 0, temp);
            Core.gemm(temp, V, 1, new Mat(), 0, temp);
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    T.put(i, j, temp.get(i, j)[0]);
                }
            }

        }

//        Log.e("T", T.dump());

        Mat var_ = varAxis0(src_demean);

//        Log.e("var_", var_.dump());

        float val = (float) Core.sumElems(var_).val[0];

        Log.e("val", String.valueOf(val));
        Mat res = new Mat();
        Log.e("S", S.dump());

        Log.e("d", d.dump());

        Core.gemm(S.t(), d, 1, new Mat(), 0, res);

//        Log.e("var_", res.dump());
//        float scale =   0.39058960128855924f;
        float scale = (float) (1.0 / val * Core.sumElems(res).val[0]);


//        Log.e("scale", String.valueOf(scale));

        Mat t_temp = T.submat(new Rect(0, 0, dim, dim));
        Mat temp = new Mat();
        Mat temp0 = new Mat();

//        Log.e("t_temp ", t_temp.dump());
//        Log.e("src_mean ", src_mean.t().dump());

        Core.gemm(t_temp, src_mean.t(), 1, new Mat(), 0, temp0);
        Core.multiply(temp0, new Scalar(scale), temp0);

//        Log.e("temp0 ", temp0.dump());
//        Log.e("dst_mean ", dst_mean.dump());


        Core.subtract(dst_mean.t(), temp0, temp);


        for (int i = 0; i < dim; i++) {
            T.put(i, dim, temp.get(i, 0)[0]);
        }


        t_temp = T.submat(new Rect(0, 0, dim, dim));
        Core.multiply(t_temp, new Scalar(scale), t_temp);


//        Log.e("temp ", temp.dump());
//        Log.e("T ", T.dump());

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                T.put(i, j, t_temp.get(i, j)[0]);
            }
        }

        return T;
    }


}




