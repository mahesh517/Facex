package com.app.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import tgio.rncryptor.RNCryptorNative;

public class FileUtils {

    Context context;

    public FileUtils(Context context) {
        this.context = context;
    }

    public static byte[] getFile(File f) {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
        } catch (FileNotFoundException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        byte[] content = null;
        try {
            content = new byte[is.available()];
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            is.read(content);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return content;
    }

    public static byte[] encryptPdfFile(Key key, byte[] content) {
        Cipher cipher;
        byte[] encrypted = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypted = cipher.doFinal(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public static byte[] decryptPdfFile(Key key, byte[] textCryp) {
        Cipher cipher;
        byte[] decrypted = null;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypted = cipher.doFinal(textCryp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public static void saveFile(byte[] bytes) throws IOException {
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/decrypted_new.tflite");
        fos.write(bytes);
        fos.close();
    }

    public static File createFileFromInputStream(InputStream inputStream) {

        try {
            File f = new File(Environment.getExternalStorageDirectory() + "/face_new-encrypted.tflite");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {

            Log.e("IoExce", e.getMessage());
        }

        return null;
    }


    public static List<float[]> decryptVectors(List<String> usersList) {


        List<float[]> vector_List = new ArrayList<>();

        for (int i = 0; i < usersList.size(); i++) {
            final String[] vector = {usersList.get(i)};

            RNCryptorNative.decryptAsync(vector[0], "mahesh", new RNCryptorNative.RNCryptorNativeCallback() {
                @Override
                public void done(String result, Exception e) {
                    vector[0] = result;
                }
            });
            float[] vector_array = convertfloat(vector[0]);
            vector_List.add(vector_array);
        }

        return vector_List;
    }


    public static float[] convertfloat(String s) {
        String[] arr = s.split(",");
        float[] intarray = null;
        if (arr != null) {
            intarray = new float[arr.length];

            try {
                for (int i = 0; i < arr.length; i++) {
                    intarray[i] = Float.parseFloat(arr[i]);
                }
            } catch (NumberFormatException e) {

            }
        }

        return intarray;
    }

    public static List<String> searchUser(float[] current_user, List<float[]> vector_List) {
        List<String> result = new ArrayList<>();
        List<Double> distance_list = new ArrayList<>();

        for (int k = 0; k < vector_List.size(); k++) {
            Double distance = getDistance(current_user, vector_List.get(k));
            distance_list.add(distance);
        }

        if (vector_List.size() > 0) {
            double min = distance_list.get(0);
            int index = 0;

            for (int i = 0; i < distance_list.size(); i++) {


                if (min > distance_list.get(i)) {
                    min = distance_list.get(i);
                    index = i;
                }
            }


            Log.e("min", String.valueOf(min));

            if (min <= 0.7) {
                result.add(String.valueOf(index));
                result.add(String.valueOf(min));
            } else {
                result.add("-1");
            }
        }

        return result;

    }

    private static Double getDistance(float[] a, float[] b) {
        double diff_square_sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            diff_square_sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(diff_square_sum);
    }


}
