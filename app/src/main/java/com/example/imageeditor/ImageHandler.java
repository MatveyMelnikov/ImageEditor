package com.example.imageeditor;

import android.graphics.Bitmap;

import androidx.core.util.Pair;

public class ImageHandler {
    public static int newPixelSideSize = 0;
    // Approximate size of the larger side of the final image
    public static int bigSideSize = 1040;
    // Alpha channel that is used when adding and getting colors
    public static int defaultAlpha = 255;
    // The number of pixels on the larger side
    public static int pixelsInBigSide = 10;

    // Calculates the exact size of the resulting image
    // (the big side of the final image is not always equal to bigSideSize)
    protected static Pair<Integer, Integer> getBitmapSize(int width, int height) {
        int initialBigSideSize = Math.max(width, height);
        int ratio = bigSideSize / initialBigSideSize;
        return new Pair<>(
                width * ratio,
                height * ratio
        );
    }

    // Gets the color in an int and multiplies it by the multiplier (needed to use weights)
    protected static int getColorOnPosition(Bitmap bitmap, int x, int y, float multiplier) {
        int color = bitmap.getPixel(x, y);
        int r = (int)(((color >> 16) & 0xff) * multiplier);
        int g = (int)(((color >>  8) & 0xff) * multiplier);
        int b = (int)((color & 0xff) * multiplier);
        return (defaultAlpha & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    protected static int foldColors(int ... colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int color : colors) {
            r += (color >> 16) & 0xff;
            g += (color >> 8) & 0xff;
            b += color & 0xff;
        }
        return (defaultAlpha & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    // Returns a small pixelated bitmap
    public static Bitmap getPixelatedBitmap(Bitmap bitmap) {
        int initialBigSideSize = Math.max(bitmap.getHeight(), bitmap.getWidth());
        float ratio = (float)pixelsInBigSide / (float)initialBigSideSize;
        int initialPixelSideSize = (int)(1.0F / ratio);
        Pair<Integer, Integer> newSize = new Pair<>(
                (int) (bitmap.getWidth() * ratio),
                (int) (bitmap.getHeight() * ratio)
        );

        Bitmap resultBitmap = Bitmap.createBitmap(
                newSize.first,
                newSize.second,
                Bitmap.Config.ARGB_8888
        );

        for (int y = 0; y < newSize.second; y++)
        {
            for (int x = 0; x < newSize.first; x++)
            {
                int leftUpCornerX = x * initialPixelSideSize;
                int leftUpCornerY = y * initialPixelSideSize;

                resultBitmap.setPixel(
                        x,
                        y,
                        getColorOnPosition(
                                bitmap,
                                leftUpCornerX + (int)(initialPixelSideSize * 0.5F),
                                leftUpCornerY + (int)(initialPixelSideSize * 0.5F),
                                1.0f
                        )
                );
            }
        }

        return resultBitmap;
    }

    // Expands the pixel image to the length we specify
    public static Bitmap getExpandedBitmap(Bitmap bitmap) {
        int initialBigSideSize = Math.max(bitmap.getHeight(), bitmap.getWidth());
        if (initialBigSideSize > bigSideSize)
            return bitmap;

        float ratio = (float)bigSideSize / (float)initialBigSideSize;
        Pair<Integer, Integer> newSize = getBitmapSize(
                bitmap.getWidth(),
                bitmap.getHeight()
        );
        newPixelSideSize = (int)ratio;
        Bitmap resultBitmap = Bitmap.createBitmap(
                newSize.first,
                newSize.second,
                Bitmap.Config.ARGB_8888
        );

        for (int y = 0; y < newSize.second; y++)
        {
            for (int x = 0; x < newSize.first; x++)
            {
                resultBitmap.setPixel(
                        x,
                        y,
                        bitmap.getPixel(x / newPixelSideSize, y / newPixelSideSize)
                );
            }
        }

        return resultBitmap;
    }
}
