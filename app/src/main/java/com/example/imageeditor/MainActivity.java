package com.example.imageeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Bitmap currentBitmap;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Устанавливаем итоговую ширину изображения в ширину экрана
        ImageHandler.bigSideSize = getResources().getDisplayMetrics().widthPixels;
        ImageHandler.defaultAlpha = 80;
        currentBitmap = ImageHandler.getExpandedBitmap(
                ImageHandler.getPixelatedBitmap(
                        BitmapFactory.decodeResource(getResources(), R.drawable.example)
                )
        );

        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(currentBitmap);

        imageView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() != MotionEvent.ACTION_DOWN)
                return true;

            int[] posXY = new int[2]; // top left corner
            imageView.getLocationInWindow(posXY);
            int touchX = (int) motionEvent.getX();
            int touchY = (int) motionEvent.getY();

            int top = view.getTop();

            int imageX = touchX / ImageHandler.newPixelSideSize;
            int imageY = touchY / ImageHandler.newPixelSideSize;

            activatePixel(currentBitmap, imageX, imageY);
            imageView.setImageBitmap(currentBitmap);

            return true;
        });
    }

    protected static void activatePixel(Bitmap bitmap, int x, int y) {
        int color = bitmap.getPixel(
                x * ImageHandler.newPixelSideSize,
                y * ImageHandler.newPixelSideSize
        );
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;
        int activeColor = 0xff << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);

        for (int i = y * ImageHandler.newPixelSideSize;
             i < (y + 1) * ImageHandler.newPixelSideSize; i++)
        {
            for (int j = x * ImageHandler.newPixelSideSize;
                 j < (x + 1) * ImageHandler.newPixelSideSize; j++)
            {
                bitmap.setPixel(j, i, activeColor);
            }
        }
    }
}