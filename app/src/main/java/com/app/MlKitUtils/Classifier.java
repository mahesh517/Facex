package com.app.MlKitUtils;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static com.app.MlKitUtils.ModelConfig.IMAGE_MEAN;
import static com.app.MlKitUtils.ModelConfig.IMAGE_STD;
import static com.app.MlKitUtils.ModelConfig.INPUT_IMG_SIZE_HEIGHT;
import static com.app.MlKitUtils.ModelConfig.INPUT_IMG_SIZE_WIDTH;
import static com.app.MlKitUtils.ModelConfig.MODEL_INPUT_SIZE;


public class Classifier {
    private final Interpreter interpreter;

    private Classifier(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public static Classifier classifier(AssetManager assetManager, String modelPath) throws IOException {
        ByteBuffer byteBuffer = loadModelFile(assetManager, modelPath);
        Interpreter interpreter = new Interpreter(byteBuffer);
        return new Classifier(interpreter);
    }

    public static Classifier classifier(File file) throws IOException {
        ByteBuffer byteBuffer = loadMapFile(file);
        Interpreter interpreter = new Interpreter(byteBuffer);

        if (interpreter != null) {
            file.delete();
        }
        return new Classifier(interpreter);
    }


    private static ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private static ByteBuffer loadMapFile(File file) {


        byte[] bytes = readBytes(file);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.put(bytes);
        Interpreter mTFLiteInterpreter = new Interpreter(byteBuffer);

        return byteBuffer;
    }

    public float[] recognizeImage(Bitmap bitmap) {

        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        long startTimeForReference = SystemClock.uptimeMillis();
        Log.e("bytebuffer", "--" + byteBuffer.array().length);
        float[][] result = new float[1][128];
        interpreter.run(byteBuffer, result);

        long endTimeForReference = SystemClock.uptimeMillis();

        Log.e("Timecost", String.valueOf(endTimeForReference - startTimeForReference));
        return normalize(result[0]);
    }

    private static float[] normalize(float[] _vector) {
        float toal_sum;


        float[] result = new float[_vector.length];
        float sum = 0.0f;
        for (float value : _vector) {

            sum = sum + (value * value);
        }
        toal_sum = (float) (Math.sqrt(sum));

        for (int i = 0; i < _vector.length; i++) {
            float values = _vector[i] / toal_sum;
            result[i] = values;
        }


        return result;
    }


    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(MODEL_INPUT_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
//        int[] intValues = getPixel(bitmap);


        IMAGE_MEAN = 0;
        IMAGE_STD = 255;
//IMAGE_MEAN = getMeans(intValues);
//        IMAGE_STD = getStd(intValues, IMAGE_MEAN);


        Log.e("IMAGE_MEAN", String.valueOf(IMAGE_MEAN));
        Log.e("IMAGE_STD", String.valueOf(IMAGE_STD));

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < INPUT_IMG_SIZE_WIDTH; ++i) {
            for (int j = 0; j < INPUT_IMG_SIZE_HEIGHT; ++j) {
                final int val = bitmap.getPixel(i, j);


                int red1 = Color.red(val);
                int blue1 = Color.blue(val);
                int green1 = Color.green(val);


                stringBuilder.append(red1);
                stringBuilder.append("\n");

                if (i == 0 && j == 0) {

                    Log.e("color1", red1 + "/" + green1 + "/" + blue1);

                }
                if (i == 0 && j == 111) {
                    Log.e("color2", red1 + "/" + green1 + "/" + blue1);
                }
                if (i == 111 && j == 0) {
                    Log.e("color3", red1 + "/" + green1 + "/" + blue1);
                }
                if (i == 111 && j == 111) {
                    Log.e("color4", red1 + "/" + green1 + "/" + blue1);
                }


//
                byteBuffer.putFloat(((red1) - IMAGE_MEAN) / IMAGE_STD);
                byteBuffer.putFloat(((green1) - IMAGE_MEAN) / IMAGE_STD);
                byteBuffer.putFloat(((blue1) - IMAGE_MEAN) / IMAGE_STD);
            }


        }


        writeToFile(stringBuilder.toString(), "sury_data");


        return byteBuffer;
    }

    private float getMeans(int[] input) {
        double sum = 0f;
        for (float value : input) {
            sum = sum + value;
        }
        return (float) (sum / (112 * 112 * 3));
    }


    private int[] getPixel(Bitmap bitmap) {


        int[] pixel_array = new int[INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * 3];
        int pixel = 0;
        for (int i = 0; i < INPUT_IMG_SIZE_WIDTH; ++i) {
            for (int j = 0; j < INPUT_IMG_SIZE_HEIGHT; ++j) {

                int colour = bitmap.getPixel(i, j);
                int red = Color.red(colour);
                int blue = Color.blue(colour);
                int green = Color.green(colour);


                pixel_array[pixel] = red;
                pixel += 1;
                pixel_array[pixel] = green;
                pixel += 1;
                pixel_array[pixel] = blue;
                pixel += 1;

            }
        }

        return pixel_array;
    }


    private float getStd(int[] input, double mean) {

        double sum = 0f;

        for (int i = 0; i < input.length; ++i) {
            input[i] -= mean;
            sum += Math.pow(input[i], 2);
        }
        double std = Math.sqrt(sum / input.length);

        return (float) Math.max(std, 1.0 / Math.sqrt(input.length));
    }


    private void writeToFile(String content, String file_name) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/" + file_name + ".txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
        }
    }

    private static byte[] readBytes(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }


}
