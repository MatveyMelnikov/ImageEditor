package com.example.imageeditor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class ImageHandler {
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
        this.bitmap = getExpandedBitmap(getPixelatedBitmap(bitmap));
        canvas = new Canvas(this.bitmap);
    }

    public void setPixel(int x, int y, int color) {
        int leftTopX = (x / newPixelSideSize) * newPixelSideSize;
        int leftTopY = (y / newPixelSideSize) * newPixelSideSize;

        if (leftTopX < 0 || leftTopY < 0 ||
                leftTopX + gridWidth > bitmap.getWidth() ||
                leftTopY + gridWidth > bitmap.getHeight())
            return;

        paint.setColor(color);

        canvas.drawRect(
                leftTopX + gridWidth,
                leftTopY + gridWidth,
                leftTopX + newPixelSideSize,
                leftTopY + newPixelSideSize,
                paint
        );
    }

    @Nullable
    public Integer getPixel(int x, int y) {
        int leftTopX = (x / newPixelSideSize) * newPixelSideSize;
        int leftTopY = (y / newPixelSideSize) * newPixelSideSize;

        if (leftTopX < 0 || leftTopY < 0 ||
                leftTopX + gridWidth > bitmap.getWidth() ||
                leftTopY + gridWidth > bitmap.getHeight())
            return null;

        return bitmap.getPixel(leftTopX + gridWidth, leftTopY + gridWidth);
    }

    public int getPixelSideSize() {
        return newPixelSideSize;
    }

    public int getDefaultAlpha() {
        return defaultAlpha;
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

    // Calculates the exact size of the resulting image
    // (the big side of the final image is not always equal to bigSideSize)
    protected Point getBitmapSize(int width, int height) {
        int initialBigSideSize = Math.max(width, height);
        int ratio = bigSideSize / initialBigSideSize;
        return new Point(width * ratio, height * ratio);
    }

    // Gets the color in an int and multiplies it by the multiplier (needed to use weights)
    protected int getColorOnPosition(Bitmap bitmap, int x, int y) {
        int color = bitmap.getPixel(x, y);
        int r = (color >> 16) & 0xff;
        int g = (color >>  8) & 0xff;
        int b = color & 0xff;
        return (defaultAlpha & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
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
                                leftUpCornerY + (int)(initialPixelSideSize * 0.5F)
                        )
                );
            }
        }

        return resultBitmap;
    }

    // Expands the pixel image to the length we specify
    protected Bitmap getExpandedBitmap(Bitmap bitmap) {
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
