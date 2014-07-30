/*
 * Copyright 2014 Thomas Hoffmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.j4velin.picturechooser.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * CropView lays above the ImageView containing the image to crop.
 * It lets the user select the cropping area and will draw a dark tint above the area,
 * which will be cropped out.
 */
public class CropView extends View {

    private RectF imageArea; // the area of the imageview
    private final RectF hightlightArea = new RectF(); // the highlighted area
    private final Paint darkAreaPaint = new Paint();
    private final Paint hightlightPaint = new Paint();
    private final Paint textPaint = new Paint();
    private float scale; // the scale factor: original image size / imageview size

    public CropView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        darkAreaPaint.setColor(Color.argb(128, 0, 0, 0));
        hightlightPaint.setStyle(Paint.Style.STROKE);
        hightlightPaint.setColor(Color.WHITE);
        hightlightPaint.setStrokeWidth(3f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(25);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        // above hightlight
        canvas.drawRect(0, 0, canvas.getWidth(), hightlightArea.top, darkAreaPaint);

        // below
        canvas.drawRect(0, hightlightArea.bottom, canvas.getWidth(), canvas.getHeight(),
                darkAreaPaint);

        // right
        canvas.drawRect(hightlightArea.right, hightlightArea.top, canvas.getWidth(),
                hightlightArea.bottom, darkAreaPaint);

        // left
        canvas.drawRect(0, hightlightArea.top, hightlightArea.left, hightlightArea.bottom,
                darkAreaPaint);

        // highlighted area
        canvas.drawRect(hightlightArea, hightlightPaint);

        canvas.drawText((int) (hightlightArea.width() * scale) + " x " +
                        (int) (hightlightArea.height() * scale) + " px", hightlightArea.left + 10,
                hightlightArea.bottom - 10, textPaint);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (event.getX() > hightlightArea.width() / 2 + hightlightArea.left)
                hightlightArea.right = Math.min(event.getX(), imageArea.right);
            else hightlightArea.left = Math.max(event.getX(), imageArea.left);

            if (event.getY() > hightlightArea.height() / 2 + hightlightArea.top)
                hightlightArea.bottom = Math.min(event.getY(), imageArea.bottom);
            else hightlightArea.top = Math.max(event.getY(), imageArea.top);

            invalidate();
        }
        return true;
    }

    /**
     * Sets the position of the imageView.
     * Highlighted area is centered in this rectangle, with a 10% margin on each side.
     *
     * @param imagePosition the position of the imageview
     */
    public void setImagePosition(final RectF imagePosition) {
        imageArea = imagePosition;
        hightlightArea.top = imageArea.top + imageArea.height() * 0.1f;
        hightlightArea.left = imageArea.left + imageArea.width() * 0.1f;
        hightlightArea.right = imageArea.right - imageArea.width() * 0.1f;
        hightlightArea.bottom = imageArea.bottom - imageArea.height() * 0.1f;
    }

    /**
     * Gets the highlighted area (= the area, the user wants to crop)
     *
     * @return the resulting are of the cropping
     */
    RectF getCropArea() {
        return hightlightArea;
    }


    /**
     * Set the scale of how much the original image is bigger than the imageview
     *
     * @param s the scale
     */
    void setScale(float s) {
        scale = s;
    }

    /**
     * @return the scale
     * @see #setScale
     */
    float getScale() {
        return scale;
    }
}
