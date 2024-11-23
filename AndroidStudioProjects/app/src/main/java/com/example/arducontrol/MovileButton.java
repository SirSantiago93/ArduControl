package com.example.arducontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;

public class MovileButton extends AppCompatButton {

    private float positionX, positionY;
    private float defaultX, defaultY;
    private String name = "BTN_A";
    private char character = 'A';
    private boolean isVisible = true;

    // Define si el control esta bloqueado o no
    private static boolean isBlocked = true;
    private float dX, dY;
    private float initialX, initialY;
    private static final int CLICK_ACTION_THRESHOLD = 10;
    private boolean isDragging;

    public MovileButton(Context context) {
        super(context);
        init();
    }

    public MovileButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MovileButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Interfaz OnTouchListener implementada con lambda
        setOnTouchListener((view, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    initialX = event.getRawX();
                    initialY = event.getRawY();
                    dX = view.getX() - initialX; // Mantener la misma distancia X a lo largo del movimiento
                    dY = view.getY() - initialY; // Mantener la misma distancia Y a lo largo del movimiento
                    isDragging = false;
                    scaleSize(view, 0.9f);
                    break;

                case MotionEvent.ACTION_MOVE:

                    if (isBlocked){
                        break;
                    }

                    float deltaX = event.getRawX() + dX;
                    float deltaY = event.getRawY() + dY;

                    // Ancho y alto del contenedor
                    int containerWidth = ((View) getParent()).getWidth();
                    int containerHeight = ((View) getParent()).getHeight();

                    // Ancho y alto del botón
                    int buttonWidth = view.getWidth();
                    int buttonHeight = view.getHeight();

                    // Restringir el movimiento dentro del contenedor (Posición relativa al contenedor)
                    if (deltaX < 0) {
                        deltaX = 0;
                    } else if (deltaX + buttonWidth > containerWidth) {
                        deltaX = containerWidth - buttonWidth;
                    }

                    if (deltaY < 0) {
                        deltaY = 0;
                    } else if (deltaY + buttonHeight > containerHeight) {
                        deltaY = containerHeight - buttonHeight;
                    }

                    view.animate()
                            .x(deltaX)
                            .y(deltaY)
                            .setDuration(0)
                            .start();

                    // Verificar si se esta arrastrando el boton
                    float deltaXMovement = Math.abs(event.getRawX() - initialX);
                    float deltaYMovement = Math.abs(event.getRawY() - initialY);
                    if (deltaXMovement > CLICK_ACTION_THRESHOLD || deltaYMovement > CLICK_ACTION_THRESHOLD) {
                        isDragging = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    scaleSize(view, 1.0f);
                    if (!isDragging) {
                        performClick();
                    }
                    setPositionX(this.getX());
                    setPositionY(this.getY());
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

    private void scaleSize(View view, float scale) {
        view.setScaleX(scale);
        view.setScaleY(scale);
    }

    public static void setIsBlocked(Boolean value){
        isBlocked = value;
    }

    public static boolean getIsBlocked(){
        return isBlocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public void saveDefaultPositions(float x, float y){
        defaultX = x;
        defaultY = y;
        positionX = defaultX;
        positionY = defaultY;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean visible) {
        isVisible = visible;
        if (isVisible){
            this.setVisibility(View.VISIBLE);
        } else{
            this.setVisibility(View.INVISIBLE);
        }
    }

    public void moveToDefaultPosition(){
        this.animate().x(defaultX).y(defaultY).setDuration(0).start();
    }

    public void moveToConfiguredPosition(){
        this.animate().x(positionX).y(positionY).setDuration(0).start();
    }
}
