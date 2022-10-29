package com.example.imageeditor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import com.example.imageeditor.recyclerview.CustomRecyclerAdapter;
import com.example.imageeditor.recyclerview.RecyclerListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerListener {
    ImageViewController imageViewController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // form test color image
        Bitmap bitmap = Bitmap.createBitmap(40, 40,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.RED);

        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        paint.setColor(Color.BLUE);
        canvas.drawRect(20, 0, 40, 20, paint);
        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 20, 20, 40, paint);
        paint.setColor(Color.MAGENTA);
        canvas.drawRect(20, 20, 40, 40, paint);
        //

        imageViewController = new ImageViewController(
                this,
                new WeakReference<>(findViewById(R.id.imageView)),
                //BitmapFactory.decodeResource(getResources(), R.drawable.example_color),
                bitmap,
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

        ArrayList<Integer> colors = new ArrayList<>(imageViewController.getAllColors());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new GridLayoutManager(
                        this,
                        2,
                        GridLayoutManager.HORIZONTAL,
                        false
                )
        );
        recyclerView.setAdapter(new CustomRecyclerAdapter(this, colors));

        // To automatically adjust when scrolling through a list
        LinearSnapHelper pagerSnapHelper = new LinearSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        imageViewController.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onElementClick(int color) {
        imageViewController.highLightAllPixelsWithColor(color);
    }
}