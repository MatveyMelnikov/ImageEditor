package com.example.imageeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;

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
                BitmapFactory.decodeResource(getResources(), R.drawable.example_wide),
                getResources().getDisplayMetrics().widthPixels,
                60,
                40,
                new Point(
                        getResources().getDisplayMetrics().widthPixels,
                        getResources().getDisplayMetrics().heightPixels
                )
        );

        SwitchCompat switchButton = findViewById(R.id.switch1);
        switchButton.setOnCheckedChangeListener((compoundButton, b) ->
                imageViewController.setMode(b)
        );

        Button undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(
                view -> imageViewController.undo()
        );

        Button redoButton = findViewById(R.id.redoButton);
        redoButton.setOnClickListener(
                view -> imageViewController.redo()
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        imageViewController.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}