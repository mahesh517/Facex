
package com.app.Utils;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.os.Environment;

import androidx.annotation.Keep;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class PhotoUtils {
    private static final String TAG = "face_rec";
    static final int kMaxChannelValue = 262143;


    public static byte[]  YUV420toNV21(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }
    public static void convertYUV420ToARGB8888(
            byte[] yData,
            byte[] uData,
            byte[] vData,
            int width,
            int height,
            int yRowStride,
            int uvRowStride,
            int uvPixelStride,
            int[] out) {
        int yp = 0;
        for (int j = 0; j < height; j++) {
            int pY = yRowStride * j;
            int pUV = uvRowStride * (j >> 1);

            for (int i = 0; i < width; i++) {
                int uv_offset = pUV + (i >> 1) * uvPixelStride;

                out[yp++] = YUV2RGB(0xff & yData[pY + i], 0xff & uData[uv_offset], 0xff & vData[uv_offset]);
            }
        }
    }
    public static void convertYUV420SPToARGB8888(byte[] input, int width, int height, int[] output) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width;
            int u = 0;
            int v = 0;

            for (int i = 0; i < width; i++, yp++) {
                int y = 0xff & input[yp];
                if ((i & 1) == 0) {
                    v = 0xff & input[uvp++];
                    u = 0xff & input[uvp++];
                }

                output[yp] = YUV2RGB(y, u, v);
            }
        }
    }
    private static int YUV2RGB(int y, int u, int v) {
        // Adjust and check YUV values
        y = (y - 16) < 0 ? 0 : (y - 16);
        y = (y - 16) < 0 ? 0 : (y - 16);
        u -= 128;
        v -= 128;

        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
        r = r > kMaxChannelValue ? kMaxChannelValue : (r < 0 ? 0 : r);
        g = g > kMaxChannelValue ? kMaxChannelValue : (g < 0 ? 0 : g);
        b = b > kMaxChannelValue ? kMaxChannelValue : (b < 0 ? 0 : b);

        return 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
    }
    public static byte[] convertYUV420ToNV21(Image imgYUV420) {

        byte[] rez;

        ByteBuffer buffer0 = imgYUV420.getPlanes()[0].getBuffer();
        ByteBuffer buffer1 = imgYUV420.getPlanes()[1].getBuffer();
        ByteBuffer buffer2 = imgYUV420.getPlanes()[2].getBuffer();

        int buffer0_size = buffer0.remaining();
        int buffer1_size = buffer1.remaining();
        int buffer2_size = buffer2.remaining();

        byte[] buffer0_byte = new byte[buffer0_size];
        byte[] buffer1_byte = new byte[buffer1_size];
        byte[] buffer2_byte = new byte[buffer2_size];
        buffer0.get(buffer0_byte, 0, buffer0_size);
        buffer1.get(buffer1_byte, 0, buffer1_size);
        buffer2.get(buffer2_byte, 0, buffer2_size);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write( buffer0_byte );
            outputStream.write( buffer1_byte );
            outputStream.write( buffer2_byte );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        rez = outputStream.toByteArray( );

        return rez;
    }
    public static int getYUVByteSize(final int width, final int height) {
        // The luminance plane requires 1 byte per pixel.
        final int ySize = width * height;

        final int uvSize = ((width + 1) / 2) * ((height + 1) / 2) * 2;

        return ySize + uvSize;
    }


    public static void saveBitmap(final Bitmap bitmap) {
        final String root =
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dlib";
        final File myDir = new File(root);

        if (!myDir.mkdirs()) {
        }

        final String fname = "preview.png";
        final File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 99, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
        }
    }

    public static native void convertYUV420SPToARGB8888(
            byte[] input, int[] output, int width, int height, boolean halfSize);


    @Keep
    public static native void convertYUV420SPToRGB565(
            byte[] input, byte[] output, int width, int height);


    @Keep
    public static native void convertARGB8888ToYUV420SP(
            int[] input, byte[] output, int width, int height);

    @Keep
    public static native void convertRGB565ToYUV420SP(
            byte[] input, byte[] output, int width, int height);
}
