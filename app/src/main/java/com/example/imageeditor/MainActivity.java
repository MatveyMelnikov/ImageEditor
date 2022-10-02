package com.example.imageeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;

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

        SwitchCompat switchButton = findViewById(R.id.switch1);
        switchButton.setOnCheckedChangeListener((compoundButton, b) ->
                imageViewController.setMode(b)
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        imageViewController.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}