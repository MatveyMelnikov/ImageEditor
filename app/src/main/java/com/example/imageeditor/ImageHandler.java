package com.example.imageeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.HashSet;

public class ImageHandler {
    public boolean[] activatedPixels;
    public HashSet<Integer> colors;
    public Bitmap pixelatedBitmap;
    protected int newPixelSideSize;
    // Approximate size of the larger side of the final image
    protected int bigSideSize;
    // Alpha channel that is used when adding and getting colors
    protected int defaultAlpha;
    // The number of pixels on the larger side
    protected int pixelsInBigSide;
    protected int gridWidth;
    protected Bitmap bitmap;
    protected Paint paint;
    protected Canvas canvas;
    protected Integer highlightedColor;

    public ImageHandler(
            Bitmap bitmap,
            int bigSideSize,
            int defaultAlpha,
            int pixelsInBigSide,
            int gridWidth
    ) {
        this.bigSideSize = bigSideSize;
        this.defaultAlpha = defaultAlpha;
        this.pixelsInBigSide = pixelsInBigSide;
        this.gridWidth = gridWidth;

        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setStrokeWidth(gridWidth);

        colors = new HashSet<>();
        pixelatedBitmap = getPixelatedBitmap(bitmap);
        activatedPixels = new boolean[pixelatedBitmap.getWidth() *
                pixelatedBitmap.getHeight()];
        this.bitmap = getExpandedBitmap(pixelatedBitmap);
        canvas = new Canvas(this.bitmap);
    }

    public ImageHandler(
            Bitmap pixelatedBitmap,
            boolean[] activatedPixels,
            int bigSideSize,
            int defaultAlpha,
            int gridWidth
    ) {
        this.bigSideSize = bigSideSize;
        this.defaultAlpha = defaultAlpha;
        this.gridWidth = gridWidth;

        paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setStrokeWidth(gridWidth);

        colors = new HashSet<>();
        this.activatedPixels = activatedPixels;
        this.pixelatedBitmap = pixelatedBitmap;
        this.bitmap = getExpandedBitmap(pixelatedBitmap);
        canvas = new Canvas(this.bitmap);
        printMarks();
    }

    public void printMark(int x, int y)
    {
        // cross
        float[] lines = new float[8];

        int leftTopX = (x / newPixelSideSize) * newPixelSideSize;
        int leftTopY = (y / newPixelSideSize) * newPixelSideSize;
        lines[0] = leftTopX + gridWidth; // left top x
        lines[1] = leftTopY + gridWidth; // left top y
        lines[2] = leftTopX + newPixelSideSize; // right bottom x
        lines[3] = leftTopY + newPixelSideSize; // right bottom y

        lines[4] = leftTopX + gridWidth; // left top x
        lines[5] = leftTopY + newPixelSideSize; // left top y
        lines[6] = leftTopX + newPixelSideSize; // right bottom x
        lines[7] = leftTopY + gridWidth; // right bottom y

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
        canvas.drawLines(lines, paint);
        paint.setStrokeWidth(gridWidth);
        paint.setAntiAlias(false);
    }

    protected void printMarks()
    {
        for (int i = 0; i < activatedPixels.length; i++) {
            // i % pixelatedBitmap.getWidth() - x
            // i / pixelatedBitmap.getWidth() - y
            if (activatedPixels[i]) {
                printMark(
                        i % pixelatedBitmap.getWidth() * newPixelSideSize,
                        i / pixelatedBitmap.getWidth() * newPixelSideSize
                        );
            }
        }
    }

    protected int getColorWithoutAlpha(int color) {
        return color & 0x00ffffff;
    }

    protected int getColor(int color, boolean isTransparent) {
        return ((isTransparent ? defaultAlpha : 255) << 24) | getColorWithoutAlpha(color);
    }

    public boolean setPixel(int x, int y, boolean isActivated) {
        int leftTopX = (x / newPixelSideSize) * newPixelSideSize;
        int leftTopY = (y / newPixelSideSize) * newPixelSideSize;

        Integer color = getPixel(x, y);

        if (color == null)
            return false;
        else if (!isActivated && highlightedColor != null &&
                getColorWithoutAlpha(highlightedColor) == getColorWithoutAlpha(color)) {
            // If there is a color highlight, then do not erase it
            color = getColor(highlightedColor, false);
        }
        else {
            color = getColor(color, !isActivated);
        }
        paint.setColor(color);

        canvas.drawRect(
                leftTopX + gridWidth,
                leftTopY + gridWidth,
                leftTopX + newPixelSideSize,
                leftTopY + newPixelSideSize,
                paint
        );
        if (isActivated)
            printMark(x, y);

        activatedPixels[
                (x / newPixelSideSize) + (y / newPixelSideSize) * pixelatedBitmap.getWidth()
                ] = isActivated;

        return true;
    }

    @Nullable
    protected Integer getPixel(int x, int y) {
        int convertedX = x / newPixelSideSize;
        int convertedY = y / newPixelSideSize;

        if (convertedX < 0 || convertedY < 0 ||
                convertedX >= pixelatedBitmap.getWidth() ||
                convertedY >= pixelatedBitmap.getHeight())
            return null;

        return pixelatedBitmap.getPixel(convertedX, convertedY);
    }

    public int getPixelSideSize() {
        return newPixelSideSize;
    }

    public int getBitmapWidth() {
        return bitmap.getWidth();
    }

    public int getBitmapHeight() {
        return bitmap.getHeight();
    }

    public void setBitmapToImageView(ImageView imageView) {
        imageView.setImageBitmap(bitmap);
    }

    public void highLightAllPixelsWithColor(int color) {
        int colorWithoutAlpha = getColorWithoutAlpha(color);

        int[] pixels = new int[pixelatedBitmap.getWidth() * pixelatedBitmap.getHeight()];
        pixelatedBitmap.getPixels(
                pixels,
                0,
                pixelatedBitmap.getWidth(),
                0,
                0,
                pixelatedBitmap.getWidth(), pixelatedBitmap.getHeight()
        );

        // Whether to remove highlight
        boolean isClearingHighLight = highlightedColor != null && highlightedColor == colorWithoutAlpha;
        // Leave the borders opaque and highlight the desired color
        for (int i = 0; i < pixels.length; i++) {
            // i % bitmap.getWidth() - x
            // y = i / bitmap.getWidth() - y
            if (activatedPixels[i])
                continue;

            if (getColorWithoutAlpha(pixels[i]) == colorWithoutAlpha && !isClearingHighLight)
                paint.setColor(color);
            else
                paint.setColor(getColor(pixels[i], true));

            int leftTopX = i % pixelatedBitmap.getWidth() * newPixelSideSize;
            int leftTopY = i / pixelatedBitmap.getWidth() * newPixelSideSize;

            canvas.drawRect(
                    leftTopX + gridWidth,
                    leftTopY + gridWidth,
                    leftTopX + newPixelSideSize,
                    leftTopY + newPixelSideSize,
                    paint
            );
        }

        if (highlightedColor != null && highlightedColor == colorWithoutAlpha)
            highlightedColor = null;
        else
            highlightedColor = colorWithoutAlpha;
    }

    // Calculates the exact size of the resulting image
    // (the big side of the final image is not always equal to bigSideSize)
    protected Point getBitmapSize(int width, int height) {
        int initialBigSideSize = Math.max(width, height);
        int ratio = bigSideSize / initialBigSideSize;
        return new Point(width * ratio, height * ratio);
    }

    // Returns a small pixelated bitmap
    protected Bitmap getPixelatedBitmap(Bitmap bitmap) {
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

        int halfOfInitialPixelSide = (int)(initialPixelSideSize * 0.5F);
        for (int y = 0; y < newSize.y; y++)
        {
            for (int x = 0; x < newSize.x; x++)
            {
                // x * initialPixelSideSize - LeftUp corner X
                // y * initialPixelSideSize - LeftUp corner Y
                int color = bitmap.getPixel(
                        (x * initialPixelSideSize) + halfOfInitialPixelSide,
                        (y * initialPixelSideSize) + halfOfInitialPixelSide
                );

                resultBitmap.setPixel(x, y, getColor(color, true));
                colors.add(getColor(resultBitmap.getPixel(x, y), false));
            }
        }

        return resultBitmap;
    }

    // Expands the pixel image to the length we specify
    protected Bitmap getExpandedBitmap(Bitmap bitmap) {
        int initialBigSideSize = Math.max(bitmap.getHeight(), bitmap.getWidth());

        float ratio = (float)bigSideSize / (float)initialBigSideSize;
        newPixelSideSize = (int)ratio;
        Point newSize = getBitmapSize(bitmap.getWidth(), bitmap.getHeight());

        Bitmap resultBitmap = Bitmap.createBitmap(
                newSize.x,
                newSize.y,
                Bitmap.Config.ARGB_8888
        );

        Canvas resultCanvas = new Canvas(resultBitmap);
        resultCanvas.drawBitmap(
                bitmap,
                null,
                new Rect(0, 0, newSize.x, newSize.y),
                paint
        );

        // Grid drawing
        float[] lines = new float[bitmap.getWidth() * 4 + bitmap.getHeight() * 4];

        // Vertical lines
        int positionIndex = 1;
        for (int i = 0; i < bitmap.getWidth() * 4; i += 4)
        {
            lines[i] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // start x
            lines[i + 1] = 0; // start y
            lines[i + 2] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // end x
            lines[i + 3] = newSize.y - 1; // end y
            positionIndex++;
        }

        // Horizontal lines
        positionIndex = 1;
        for (int i = bitmap.getWidth() * 4; i < lines.length; i += 4)
        {
            lines[i] = 0; // start x
            lines[i + 1] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // start y
            lines[i + 2] = newSize.x - 1; // end x
            lines[i + 3] = positionIndex * newPixelSideSize + gridWidth * 0.5F; // end y
            positionIndex++;
        }
        resultCanvas.drawLines(lines, paint);

        return resultBitmap;
    }
}
