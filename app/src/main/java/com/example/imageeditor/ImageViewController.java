package com.example.imageeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public class ImageViewController {
    ImageHandler imageHandler;
    private final int STACK_SIZE = 10;
    private final WeakReference<ImageView> imageViewReference;
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
    private boolean isActivating = true;
    private ActionsStack actionsStack;
    // Needed for the same approximation at different image sizes (and pixelsInBigSide)
    private final float scaleRatio;

    private final PointF scaleTarget = new PointF();
    private float lastFactor = 1.0F;

    private final PointF scaleBorder = new PointF(0.5F, 3.5F);

    public ImageViewController(
            Context context,
            WeakReference<ImageView> imageViewReference,
            Bitmap bitmap,
            int bigSideSize,
            int defaultAlpha,
            int pixelsInBigSide,
            Point screenSize
    ) {
        // Set the final image width to the width of the screen
        this.screenSize = screenSize;
        imageHandler = new ImageHandler(
                bitmap,
                bigSideSize,
                defaultAlpha,
                pixelsInBigSide,
                3
        );

        scaleRatio = imageHandler.getPixelSideSize() / 27.0F;
        scaleBorder.set(
                scaleBorder.x * scaleRatio * (40.0F / pixelsInBigSide),
                scaleBorder.y * scaleRatio * (pixelsInBigSide / 40.0F)
        );

        this.imageViewReference = imageViewReference;
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        scaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        if (detector.getTimeDelta() == 0) {
                            lastFactor = factor;
                            scaleTarget.set(
                                    imageView.getX() - initialPosition.x,
                                    imageView.getY() - initialPosition.y
                            );
                        }

                        // Zoom speed limit
                        factor *= Math.max(0.95F, Math.min(1.05F, detector.getScaleFactor()));
                        // Zoom level control
                        factor = Math.max(scaleBorder.x, Math.min(scaleBorder.y, factor));

                        ImageView imageView = imageViewReference.get();
                        imageView.setScaleX(factor);
                        imageView.setScaleY(factor);

                        // Zoom motion
                        float factorMultiplier = factor / lastFactor;

                        imageView.setX(scaleTarget.x * factorMultiplier + initialPosition.x);
                        imageView.setY(scaleTarget.y * factorMultiplier + initialPosition.y);

                        return super.onScale(detector);
                    }
                }
        );
        scaleGestureDetector.setQuickScaleEnabled(false);

        imageHandler.setBitmapToImageView(imageView);
        actionsStack = new ActionsStack(STACK_SIZE);
    }

    public ImageViewController(
            Context context,
            WeakReference<ImageView> imageViewReference,
            Bitmap pixelatedBitmap,
            boolean[] activatedPixels,
            int bigSideSize,
            int defaultAlpha,
            Point screenSize
    ) {
        // Set the final image width to the width of the screen

        this.screenSize = screenSize;
        imageHandler = new ImageHandler(
                pixelatedBitmap,
                activatedPixels,
                bigSideSize,
                defaultAlpha,
                3
        );
        int pixelsInBigSide = Math.max(pixelatedBitmap.getHeight(), pixelatedBitmap.getWidth());
        scaleRatio = imageHandler.getPixelSideSize() / 27.0F;
        scaleBorder.set(
                scaleBorder.x * scaleRatio * (40.0F / pixelsInBigSide),
                scaleBorder.y * scaleRatio * (pixelsInBigSide / 40.0F)
        );

        this.imageViewReference = imageViewReference;
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        scaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        if (detector.getTimeDelta() == 0) {
                            lastFactor = factor;
                            scaleTarget.set(
                                    imageView.getX() - initialPosition.x,
                                    imageView.getY() - initialPosition.y
                            );
                        }

                        // Zoom speed limit
                        factor *= Math.max(0.95F, Math.min(1.05F, detector.getScaleFactor()));
                        // Zoom level control
                        factor = Math.max(scaleBorder.x, Math.min(scaleBorder.y, factor));

                        ImageView imageView = imageViewReference.get();
                        imageView.setScaleX(factor);
                        imageView.setScaleY(factor);

                        // Zoom motion
                        float factorMultiplier = factor / lastFactor;

                        imageView.setX(scaleTarget.x * factorMultiplier + initialPosition.x);
                        imageView.setY(scaleTarget.y * factorMultiplier + initialPosition.y);

                        return super.onScale(detector);
                    }
                }
        );
        scaleGestureDetector.setQuickScaleEnabled(false);

        imageHandler.setBitmapToImageView(imageView);
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

        // Prevent pixel activation while scrolling
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

        if (activatePixel(imageX, imageY, isActivating)) {
            imageHandler.setBitmapToImageView(imageView);

            if (!actionsStack.isStartPosition())
                actionsStack.clear();
            // Add action to stack
            actionsStack.push(new ImageAction(
                            imageX / imageHandler.getPixelSideSize(),
                            imageY / imageHandler.getPixelSideSize(),
                            isActivating
                    )
            );
        }
    }

    public void setMode(boolean isActivating) {
        this.isActivating = isActivating;
    }

    private void controlBorders(View view) {
        PointF viewCenter = new PointF(
                view.getX() - initialPosition.x,
                view.getY() - initialPosition.y
        );

        // Distance from the center of the imageView to the edge of the screen
        PointF indent = new PointF(
                screenSize.x * 0.5F + imageHandler.getBitmapWidth() * 0.25F * factor,
                screenSize.y * 0.5F + imageHandler.getBitmapHeight() * 0.25F * factor
        );

        if (viewCenter.y < -indent.y && offset.y < 0.0F)
            offset.y = 0.0F;
        if (viewCenter.x < -indent.x && offset.x < 0.0F)
            offset.x = 0.0F;
        if (viewCenter.y > indent.y && offset.y > 0.0F)
            offset.y = 0.0F;
        if (viewCenter.x > indent.x && offset.x > 0.0F)
            offset.x = 0.0F;
    }

    protected void scrollImageView(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPosition.set(motionEvent.getRawX(), motionEvent.getRawY());
                currentPosition.set(startPosition);
                break;
            case MotionEvent.ACTION_MOVE:
                // Prevent sharp movement when using multitouch
                if (motionEvent.getPointerId(0) != 0)
                    break;
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

    protected boolean activatePixel(int x, int y, boolean isActivated) {
        return imageHandler.setPixel(x, y, isActivated);
    }

    public void undo() {
        ImageAction imageAction = actionsStack.peekWithPosition();
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        if (imageAction != null) {
            activatePixel(
                    imageAction.x * imageHandler.getPixelSideSize(),
                    imageAction.y * imageHandler.getPixelSideSize(),
                    !imageAction.isActivated
            );
            imageHandler.setBitmapToImageView(imageView);
        }
    }

    public void redo() {
        ImageAction imageAction = actionsStack.reversePeekWithPosition();
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        if (imageAction != null) {
            activatePixel(
                    imageAction.x * imageHandler.getPixelSideSize(),
                    imageAction.y * imageHandler.getPixelSideSize(),
                    imageAction.isActivated
            );
            imageHandler.setBitmapToImageView(imageView);
        }
    }

    public void highLightAllPixelsWithColor(int color) {
        ImageView imageView = imageViewReference.get();
        if (imageView == null)
            return;

        imageHandler.highLightAllPixelsWithColor(color);
        imageHandler.setBitmapToImageView(imageView);
    }

    public HashSet<Integer> getAllColors() {
        return imageHandler.colors;
    }

    public Bitmap getPixelatedBitmap()
    {
        return imageHandler.pixelatedBitmap;
    }

    public boolean[] getActivatedPixels()
    {
        return imageHandler.activatedPixels;
    }
}
