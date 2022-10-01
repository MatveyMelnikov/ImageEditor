package com.example.imageeditor;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    ImageViewController imageViewController;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewController = new ImageViewController(
                this,
                new WeakReference<>(findViewById(R.id.imageView)),
                BitmapFactory.decodeResource(getResources(), R.drawable.example),
                getResources().getDisplayMetrics().widthPixels,
                60,
                20,
                new Point(
                        getResources().getDisplayMetrics().widthPixels,
                        getResources().getDisplayMetrics().heightPixels
                )
        );

        //int a = getResources().getColor(R.color.black);

        /*Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(getResources().getColor(R.color.white));
        bitmap.setPixel(1, 1, getResources().getColor(R.color.black));
        bitmap.setPixel(4, 4, getResources().getColor(R.color.black));

        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);

        Bitmap bitmap1 = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap1);
        canvas.drawBitmap(bitmap, null, new Rect(0, 0, 1000, 1000), paint);
        ImageView i = (ImageView)(findViewById(R.id.imageView));
        i.setImageBitmap(bitmap1);*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        imageViewController.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}