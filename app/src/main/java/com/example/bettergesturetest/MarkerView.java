package com.example.bettergesturetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.Collection;
import java.util.HashMap;
import androidx.core.view.MotionEventCompat;



public class MarkerView extends View {
        String TAG = "MyActivity";
        int RADIUS = 100;
        int pointerCount = 0;

        // Bitmap is used to draw image on screen - currently set to a picture of my face
        //Bitmap b1 = BitmapFactory.decodeResource(getResources(), R.drawable.circle_icon);
        //Bitmap b = Bitmap.createScaledBitmap(b1, RADIUS,RADIUS, false);

        HashMap<Integer, Marker> MarkerList = new HashMap<Integer, Marker>();
        private Paint paint;

        // parameters for drawing a circle instead of image. if image, comment out bitmap stuff (ln 25 & 26) and uncomment paint in onDraw (ln 119)
        public MarkerView(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
        }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int myAction = event.getActionMasked();
        int Index = event.getActionIndex();

        switch (myAction) {
            case (MotionEvent.ACTION_UP):
                MarkerList.remove(Index);
                pointerCount = 0;
                invalidate();

                return true;

            case (MotionEvent.ACTION_DOWN):
                Marker m = new Marker((int) event.getX(),(int) event.getY(), RADIUS);
                MarkerList.put(Index, m);
                pointerCount = event.getPointerCount();
                invalidate();

                return true;

            case (MotionEvent.ACTION_POINTER_DOWN):
                int x = (int) MotionEventCompat.getX(event, Index);
                int y = (int) MotionEventCompat.getY(event, Index);
                m = new Marker(x, y, RADIUS);
                MarkerList.put(Index, m);
                pointerCount = event.getPointerCount();
                invalidate();

                return true;

            case (MotionEvent.ACTION_POINTER_UP):
                MarkerList.remove(Index);
                pointerCount = event.getPointerCount();
                invalidate();

                return true;

            case (MotionEvent.ACTION_MOVE):
                pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    x = (int) event.getX(i);
                    y = (int) event.getY(i);
                    m = new Marker(x, y, RADIUS);
                    MarkerList.put(i, m);
                }
                pointerCount = event.getPointerCount();
                invalidate();

                return true;

            case (MotionEvent.ACTION_CANCEL):
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }


        @Override
        protected void onDraw(Canvas canvas) {
            // fixes bug of "ghost" markers staying on screen after release of all pointers
            if (pointerCount == 0) {
                MarkerList.clear();
            }

            if (pointerCount > 0) {
                Collection values =  MarkerList.values();
                int j = 0;

                for (Object i : values) {
                    Marker m = (Marker) i;

                    // fixes bug of "ghost" markers when releasing two fingers simultaneously while one remains on screen
                    if (j < pointerCount) {
                        //canvas.drawBitmap(b, m.getX() - RADIUS / 2, m.getY() - RADIUS / 2, paint);
                        canvas.drawCircle(m.getX(), m.getY(), m.getRadius(), paint);
                    }

                    j++;
                }
            }
        }
    }
