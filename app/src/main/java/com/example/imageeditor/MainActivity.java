package com.example.imageeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Bitmap currentBitmap;
    ConstraintLayout.LayoutParams layoutParams;
    float initialX = 0.0F, initialY = 0.0F;

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
        layoutParams = (ConstraintLayout.LayoutParams)imageView.getLayoutParams();
        //Object a = imageView.getLayoutParams();

        imageView.setImageBitmap(currentBitmap);

        imageView.setOnTouchListener((view, motionEvent) -> {
            // scroll
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = motionEvent.getRawX();
                    initialY = motionEvent.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float offsetX = motionEvent.getRawX() - initialX;
                    float offsetY = motionEvent.getRawY() - initialY;

                    if (layoutParams.topMargin < -view.getHeight() * 1.75f &&
                            offsetY < 0.0F)
                        break;
                    if (layoutParams.leftMargin < -view.getWidth() * 0.75f &&
                            offsetX < 0.0F)
                        break;
                    if (layoutParams.topMargin > view.getHeight() * 1.75f &&
                            offsetY > 0.0F)
                        break;
                    if (layoutParams.leftMargin > view.getWidth() * 0.75f &&
                            offsetX > 0.0F)
                        break;

                    layoutParams.leftMargin = (int) (layoutParams.leftMargin + offsetX);
                    layoutParams.topMargin = (int) (layoutParams.topMargin + offsetY);
                    view.setLayoutParams(layoutParams);

                    initialX = motionEvent.getRawX();
                    initialY = motionEvent.getRawY();
                    break;
            }

            // paint
            /*if (motionEvent.getAction() != MotionEvent.ACTION_DOWN)
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
            */
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