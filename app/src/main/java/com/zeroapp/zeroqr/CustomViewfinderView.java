package com.zeroapp.zeroqr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.List;

public class CustomViewfinderView extends ViewfinderView {

    // Line depth
    private final float mLineDepth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

    // Line color
    private final int mLineColor = Color.parseColor(getResources().getString(R.color.colorPrimary));
    private Rect previewFramingRect;

    public CustomViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void refreshSizes() {
        if (cameraPreview == null) {
            return;
        }
        framingRect = cameraPreview.getFramingRect();

        Rect previewFramingRect = cameraPreview.getPreviewFramingRect();
        if (framingRect != null && previewFramingRect != null) {
            this.previewFramingRect = previewFramingRect;
        }
    }
    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }

        final Rect frame = framingRect;
        final Rect previewFrame = previewFramingRect;

        final int width = getWidth();
        final int height = getHeight();

        // [Custom start] Draw 4 corner lines
        paint.setColor(mLineColor);
        // Length rate of line and frame
        float mLineRate = 0.2f;
        canvas.drawRect(
                frame.left,
                frame.top,
                frame.left + frame.width() * mLineRate,
                frame.top + mLineDepth,
                paint);
        canvas.drawRect(
                frame.left,
                frame.top,
                frame.left + mLineDepth,
                frame.top + frame.height() * mLineRate,
                paint);

        canvas.drawRect(
                frame.right - frame.width() * mLineRate,
                frame.top,
                frame.right,
                frame.top + mLineDepth,
                paint);
        canvas.drawRect(
                frame.right - mLineDepth,
                frame.top,
                frame.right,
                frame.top + frame.height() * mLineRate,
                paint);

        canvas.drawRect(
                frame.left,
                frame.bottom - mLineDepth,
                frame.left + frame.width() * mLineRate,
                frame.bottom,
                paint);
        canvas.drawRect(
                frame.left,
                frame.bottom - frame.height() * mLineRate,
                frame.left + mLineDepth,
                frame.bottom,
                paint);

        canvas.drawRect(
                frame.right - frame.width() * mLineRate,
                frame.bottom - mLineDepth,
                frame.right,
                frame.bottom,
                paint);
        canvas.drawRect(
                frame.right - mLineDepth,
                frame.bottom - frame.height() * mLineRate,
                frame.right,
                frame.bottom,
                paint);
        // [Custom end]

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {

            // Draw a red "laser scanner" line through the middle to show decoding is active
            paint.setColor(laserColor);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            final int middle = frame.height() / 2 + frame.top;
            canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);

            final float scaleX = frame.width() / (float) previewFrame.width();
            final float scaleY = frame.height() / (float) previewFrame.height();

            final int frameLeft = frame.left;
            final int frameTop = frame.top;

            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);
                float radius = POINT_SIZE / 2.0f;
                for (final ResultPoint point : lastPossibleResultPoints) {
                    canvas.drawCircle(
                            frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint
                    );
                }
                lastPossibleResultPoints.clear();
            }

            // draw current possible result points
            if (!possibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);
                for (final ResultPoint point : possibleResultPoints) {
                    canvas.drawCircle(
                            frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint
                    );
                }

                // swap and clear buffers
                final List<ResultPoint> temp = possibleResultPoints;
                possibleResultPoints = lastPossibleResultPoints;
                lastPossibleResultPoints = temp;
                possibleResultPoints.clear();
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }
}
