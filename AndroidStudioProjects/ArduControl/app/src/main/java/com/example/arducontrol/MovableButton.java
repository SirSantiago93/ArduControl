package com.example.arducontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;

public class MovableButton extends AppCompatButton {

    private boolean isBlocked = false;
    private float dX, dY;
    private float initialX, initialY;
    private static final int CLICK_ACTION_THRESHOLD = 10;
    private boolean isDragging;

    public MovableButton(Context context) {
        super(context);
        init();
    }

    public MovableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener((view, event) -> { // interface OnTouchListener implemented with lambda
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getRawX();
                    initialY = event.getRawY();
                    dX = view.getX() - initialX; // Keep the same X distance along the movement
                    dY = view.getY() - initialY; // Keep the same Y distance along the movement
                    isDragging = false;
                    scaleButton(view, 0.9f);
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (isBlocked){
                        break;
                    }

                    float deltaX = event.getRawX() + dX;
                    float deltaY = event.getRawY() + dY;

                    // Container width and height
                    int containerWidth = ((View) getParent()).getWidth();
                    int containerHeight = ((View) getParent()).getHeight();

                    // Button width and height
                    int buttonWidth = view.getWidth();
                    int buttonHeight = view.getHeight();

                    // Restrict movement inside the container (Relative position to the container)
                    if (deltaX < 0) {
                        deltaX = 0; // Do not allow it to move to the left
                    } else if (deltaX + buttonWidth > containerWidth) {
                        deltaX = containerWidth - buttonWidth; // Do not allow it to move to the right
                    }

                    if (deltaY < 0) {
                        deltaY = 0; // Do not allow it to move upwards
                    } else if (deltaY + buttonHeight > containerHeight) {
                        deltaY = containerHeight - buttonHeight; // Do not allow it to slide down
                    }

                    view.animate()
                            .x(deltaX)
                            .y(deltaY)
                            .setDuration(0)
                            .start();

                    float deltaXMovement = Math.abs(event.getRawX() - initialX);
                    float deltaYMovement = Math.abs(event.getRawY() - initialY);
                    if (deltaXMovement > CLICK_ACTION_THRESHOLD || deltaYMovement > CLICK_ACTION_THRESHOLD) {
                        isDragging = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    scaleButton(view, 1.0f);
                    if (!isDragging) {
                        performClick();
                    }
                    break;

                default:
                    return false;
            }
            return true;
        });
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void scaleButton(View view, float scale) {
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    public void setIsBlocked(Boolean value){
        this.isBlocked = value;
    }

    public boolean getIsBlocked(){
        return this.isBlocked;
    }
}


