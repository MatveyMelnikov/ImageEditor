package com.example.imageeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class ImageHandler {
    public static int newPixelSideSize = 0;
    // Approximate size of the larger side of the final image
    public static int bigSideSize = 1040;
    // Alpha channel that is used when adding and getting colors
    public static int defaultAlpha = 255;
    // The number of pixels on the larger side
    public static int pixelsInBigSide = 10;
    public static int gridWidth = 3;

    // Calculates the exact size of the resulting image
    // (the big side of the final image is not always equal to bigSideSize)
    protected static Point getBitmapSize(int width, int height) {
        int initialBigSideSize = Math.max(width, height);
        int ratio = bigSideSize / initialBigSideSize;
        return new Point(width * ratio, height * ratio);
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
        Point newSize = new Point(
                (int) (bitmap.getWidth() * ratio),
                (int) (bitmap.getHeight() * ratio)
        );

        Bitmap resultBitmap = Bitmap.createBitmap(
                newSize.x,
                newSize.y,
                Bitmap.Config.ARGB_8888
        );

        for (int y = 0; y < newSize.y; y++)
        {
            for (int x = 0; x < newSize.x; x++)
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
        newPixelSideSize = (int)ratio;
        Point newSize = getBitmapSize(bitmap.getWidth(), bitmap.getHeight());

        Bitmap resultBitmap = Bitmap.createBitmap(
                newSize.x,
                newSize.y,
                Bitmap.Config.ARGB_8888
        );

        // Pixel bitmap extension
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setBlendMode(BlendMode.SRC);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(
                bitmap,
                null,
                new Rect(0, 0, newSize.x, newSize.y),
                paint
        );

        // Grid drawing
        float[] lines = new float[bitmap.getWidth() * 4 + bitmap.getHeight() * 4];
        paint.setStrokeWidth(gridWidth);

        // Horizontal lines
        int positionIndex = 1;
        for (int i = 0; i < bitmap.getWidth() * 4; i += 4)
        {
            lines[i] = 0; // start x
            lines[i + 1] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // start y
            lines[i + 2] = newSize.x - 1; // end x
            lines[i + 3] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // end y
            positionIndex++;
        }

        // Vertical lines
        positionIndex = 1;
        for (int i = bitmap.getWidth() * 4; i < lines.length; i += 4)
        {
            lines[i] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // start x
            lines[i + 1] = 0; // start y
            lines[i + 2] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // end x
            lines[i + 3] = newSize.y - 1; // end y
            positionIndex++;
        }
        canvas.drawLines(lines, paint);

        return resultBitmap;
    }
}
