package com.example.imageeditor;

import android.graphics.Bitmap;

import androidx.core.util.Pair;

public class ImageHandler {
    public static int newPixelSideSize = 0;
    // Приблизительный размер большей стороны изображения на выходе
    public static int bigSideSize = 1040;
    // Альфа канал, который используется при сложении цветов
    public static int defaultAlpha = 100;
    // Количество пикселей на большей стороне
    protected static int pixelsInBigSide = 10;

    // Вычисляет точный размер итогового изображения
    // (большая сторона не всегда равна bigSideSize)
    protected static Pair<Integer, Integer> getBitmapSize(int width, int height) {
        int initialBigSideSize = Math.max(width, height);
        int ratio = bigSideSize / initialBigSideSize;
        return new Pair<>(
                width * ratio,
                height * ratio
        );
    }

    // Получает цвет в int и умножает его на множитель (нужно для ввода весов)
    protected static int getColorOnPosition(Bitmap bitmap, int x, int y, float multiplier) {
        int color = bitmap.getPixel(x, y);
        int r = (int)(((color >> 16) & 0xff) * multiplier);
        int g = (int)(((color >>  8) & 0xff) * multiplier);
        int b = (int)((color & 0xff) * multiplier);
        return 0xff << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    // Складывает цвета
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

    // Возвращает пикселизированный bitmap маленького размера
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

                // Берем образцы цвета и умножаем их на веса
                int leftUpCorner =
                        getColorOnPosition(bitmap, leftUpCornerX, leftUpCornerY, 0.1f);
                int rightUpCorner = getColorOnPosition(
                        bitmap,
                        leftUpCornerX  + initialPixelSideSize,
                        leftUpCornerY,
                        0.1f
                );
                int rightDownCorner = getColorOnPosition(
                        bitmap,
                        leftUpCornerX  + initialPixelSideSize,
                        leftUpCornerY  + initialPixelSideSize,
                        0.1f
                );
                int leftDownCorner = getColorOnPosition(
                        bitmap,
                        leftUpCornerX,
                        leftUpCornerY  + initialPixelSideSize,
                        0.1f
                );
                int center = getColorOnPosition(
                        bitmap,
                        leftUpCornerX + (int)(initialPixelSideSize * 0.5F),
                        leftUpCornerY + (int)(initialPixelSideSize * 0.5F),
                        0.6f
                );

                resultBitmap.setPixel(x, y, foldColors(
                        leftUpCorner, rightUpCorner, rightDownCorner, leftDownCorner, center
                ));
            }
        }

        return resultBitmap;
    }

    // Расширяет пиксельное изображение до указанного нами
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
