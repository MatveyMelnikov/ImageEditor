package com.example.imageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ImageViewController {
    private final int STACK_SIZE = 10;
    private final WeakReference<ImageView> imageViewReference;
    private final Bitmap currentBitmap;
    private ScaleGestureDetector scaleGestureDetector;
    // The position from which the swipe starts
    private final PointF startPosition = new PointF(0.0F, 0.0F);
    // Current position when moving
    private final PointF currentPosition = new PointF(0.0F, 0.0F);
    // Difference between the current swipe position and the previous one
    private final PointF offset = new PointF(0.0F, 0.0F);
    // Starting position of image view
    private PointF initialPosition = null;
    private final Point screenSize;
    // Image zoom multiplier
    private float factor = 1.0F;
    private int currentAlpha = 255;
    private ActionsStack actionsStack;

    public ImageViewController(
            Context context,
            WeakReference<ImageView> imageViewReference,
            Bitmap bitmap,
            int bigSideSize,
            int defaultAlpha,
            int pixelsInBigSide,
            Point screenSize
    ) {
        // Устанавливаем итоговую ширину изображения в ширину экрана
        ImageHandler.bigSideSize = bigSideSize;
        ImageHandler.defaultAlpha = defaultAlpha;
        ImageHandler.pixelsInBigSide = pixelsInBigSide;
        currentBitmap = ImageHandler.getExpandedBitmap(ImageHandler.getPixelatedBitmap(bitmap));
        this.screenSize = screenSize;

        this.imageViewReference = imageViewReference;
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        scaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        factor *= Math.max(0.95f, Math.min(1.05f, detector.getScaleFactor()));
                        factor = Math.max(0.5f, Math.min(2.0f, factor));
                        ImageView imageView = imageViewReference.get();

                        imageView.setScaleX(factor);
                        imageView.setScaleY(factor);

                        return super.onScale(detector);
                    }
                }
        );

        imageView.setImageBitmap(currentBitmap);

        actionsStack = new ActionsStack(STACK_SIZE);
    }

    public void onTouchEvent(MotionEvent event) {
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        // The initial position of the view cannot be obtained in the OnCreate method,
        // so we get it when processing events
        if (initialPosition == null)
            initialPosition = new PointF(imageView.getX(), imageView.getY());

        // scroll
        scrollImageView(imageView, event);

        // zoom
        scaleGestureDetector.onTouchEvent(event);

        // paint
        if (event.getAction() != MotionEvent.ACTION_UP)
            return;

        if (Math.sqrt(Math.pow(currentPosition.x - startPosition.x, 2) +
                Math.pow(currentPosition.y - startPosition.y, 2)) > 30.0F) {
            startPosition.set(currentPosition);
            return;
        }

        int[] posXY = new int[2]; // top left corner
        imageView.getLocationInWindow(posXY);
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        // Zoom does not affect on image view size
        int imageX = (int)((touchX - posXY[0]) / factor);
        int imageY = (int)((touchY - posXY[1]) / factor);

        if (imageX < 0 || imageX > imageView.getWidth() ||
                imageY < 0 || imageY > imageView.getHeight())
            return;

        activatePixel(imageView, currentBitmap, imageX, imageY, currentAlpha);
        if (!actionsStack.isStartPosition())
            actionsStack.clear();
        // Add action to stack
        actionsStack.push(new ImageAction(
                imageX / ImageHandler.newPixelSideSize,
                imageY / ImageHandler.newPixelSideSize,
                currentAlpha == 255)
        );
    }

    public void setMode(boolean isActivate) {
        if (isActivate)
            currentAlpha = 255;
        else
            currentAlpha = ImageHandler.defaultAlpha;
    }

    private void controlBorders(View view) {
        if (view.getY() - initialPosition.y < -screenSize.y * 0.5F && offset.y < 0.0F)
            offset.y = 0.0F;
        if (view.getX() - initialPosition.x < -screenSize.x * 0.5F && offset.x < 0.0F)
            offset.x = 0.0F;
        if (view.getY() - initialPosition.y > screenSize.y * 0.5f && offset.y > 0.0F)
            offset.y = 0.0F;
        if (view.getX() - initialPosition.x > screenSize.x * 0.5F && offset.x > 0.0F)
            offset.x = 0.0F;
    }

    protected void scrollImageView(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPosition.set(motionEvent.getRawX(), motionEvent.getRawY());
                currentPosition.set(startPosition);
                break;
            case MotionEvent.ACTION_MOVE:
                offset.set(
                        motionEvent.getRawX() - currentPosition.x,
                        motionEvent.getRawY() - currentPosition.y
                );

                controlBorders(view);

                view.setX(view.getX() + offset.x);
                view.setY(view.getY() + offset.y);
                currentPosition.set(motionEvent.getRawX(), motionEvent.getRawY());
                break;
        }
    }

    protected void activatePixel(ImageView imageView, Bitmap bitmap, int x, int y, int alpha) {
        int leftTopX = (x / ImageHandler.newPixelSideSize) * ImageHandler.newPixelSideSize;
        int leftTopY = (y / ImageHandler.newPixelSideSize) * ImageHandler.newPixelSideSize;

        int color = bitmap.getPixel(
                leftTopX + ImageHandler.gridWidth,
                leftTopY + ImageHandler.gridWidth
        );
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;
        int activeColor = (alpha & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);

        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);
        paint.setColor(activeColor);
        paint.setBlendMode(BlendMode.SRC);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(
                leftTopX + ImageHandler.gridWidth,
                leftTopY + ImageHandler.gridWidth,
                leftTopX + ImageHandler.newPixelSideSize,
                leftTopY + ImageHandler.newPixelSideSize,
                paint
        );
        imageView.setImageBitmap(currentBitmap);
    }

    public void undo() {
        ImageAction imageAction = actionsStack.peekWithPosition();
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        if (imageAction != null) {
            activatePixel(
                    imageView,
                    currentBitmap,
                    imageAction.x * ImageHandler.newPixelSideSize,
                    imageAction.y * ImageHandler.newPixelSideSize,
                    imageAction.isActivated ? ImageHandler.defaultAlpha : 255
            );
        }
    }

    public void redo() {
        ImageAction imageAction = actionsStack.reversePeekWithPosition();
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        if (imageAction != null) {
            activatePixel(
                    imageView,
                    currentBitmap,
                    imageAction.x * ImageHandler.newPixelSideSize,
                    imageAction.y * ImageHandler.newPixelSideSize,
                    imageAction.isActivated ? 255 : ImageHandler.defaultAlpha
            );
        }
    }
}
