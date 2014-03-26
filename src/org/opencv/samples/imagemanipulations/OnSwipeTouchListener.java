package org.opencv.samples.imagemanipulations;

/**
 * Cut and pasted straight off the internet... 3/24/14
 * TODO: credit http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 */

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    protected int index = -1;
    //private boolean foundSwiped = false;

    ArrayList<MotionEvent.PointerCoords> coords;
    public void putIndex(int index) {
        this.index = index;
        //foundSwiped = true;
    }

    //public int getIndex() {return index; }

    //public boolean foundSwiped() {return foundSwiped; }

    public OnSwipeTouchListener (Context ctx, LinearLayout linear){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        coords = new ArrayList<MotionEvent.PointerCoords>();
    }

    //public void addCoord(MotionEvent.PointerCoords coord) {
    //    coords.add(coord);
    //}

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                            result=true;
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (!result) index = -1;
            return result;
        }
    }

    //code for swipes should be implemented in the activities
    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public GestureDetector getGestureDetector(){
        return  gestureDetector;
    }
}