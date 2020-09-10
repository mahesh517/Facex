package com.app.MlKitUtils;

public class ModelConfig {
    public static String MODEL_FILENAME = "face_encrypt.tflite";

    public static final int INPUT_IMG_SIZE_WIDTH = 112;
    public static final int INPUT_IMG_SIZE_HEIGHT = 112;
    public static final int FLOAT_TYPE_SIZE = 4;
    public static final int PIXEL_SIZE = 3;
    public static final int MODEL_INPUT_SIZE = FLOAT_TYPE_SIZE * INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * PIXEL_SIZE;
    public static float IMAGE_MEAN = 0.0f;
    public static float IMAGE_STD = 1.0f;


}
