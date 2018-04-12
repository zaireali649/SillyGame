package edu.temple.sillygame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

public class GameView extends View {

    int canvasWidth, canvasHeight, buttonX = 100, buttonY = 100;
    Paint myColor;

    public GameObject enemy, otherEnemy;

    int myX, myY, mySize;

    Context context;

    Queue<GameObject> toBeDrawn = new LinkedList<>();

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        myColor = new Paint();
        myColor.setColor(Color.BLUE);
        enemy = new GameObject(0,0,80, Color.RED, 20, null) {

            int xDirection; // 0 - left, 1 - right
            int yDirection; // 0 - up, 1 - down

            @Override
            public void performAction(Object data) {

                final int movePixels = 5;

                if (xDirection == 0) {
                    if ((x - movePixels) > 0) x -= movePixels ;
                    else xDirection = 1;
                } else {
                    if ((x + movePixels) + size < canvasWidth) x += movePixels;
                    else xDirection = 0;
                }

                if (yDirection == 0) {
                    if ((y - movePixels) > 0) y -= movePixels;
                    else yDirection = 1;
                } else {
                    if ((y + movePixels) + size < canvasHeight) y += movePixels;
                    else yDirection = 0;
                }

                addToBeDrawn(this);

            }

        };

        enemy.start();

        otherEnemy = new GameObject(300,670,150, Color.YELLOW, 10, null) {

            int xDirection; // 0 - left, 1 - right
            int yDirection; // 0 - up, 1 - down

            @Override
            public void performAction(Object data) {

                final int movePixels = 5;

                if (xDirection == 0) {
                    if ((x - movePixels) > 0) x -= movePixels ;
                    else xDirection = 1;
                } else {
                    if ((x + movePixels) + size < canvasWidth) x += movePixels;
                    else xDirection = 0;
                }

                if (yDirection == 0) {
                    if ((y - movePixels) > 0) y -= movePixels;
                    else yDirection = 1;
                } else {
                    if ((y + movePixels) + size < canvasHeight) y += movePixels;
                    else yDirection = 0;
                }

                addToBeDrawn(this);

            }

        };

        otherEnemy.start();


        myX = 0;
        myY = 0;
        mySize = 100;

    }

    float[] values = new float[3];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        GameObject object;

        while ((object = getObjectToBeDrawn()) != null)
            drawObject(canvas, object);

        values = ((Rotatable) context).getRotationInfo();


        drawSelf(canvas, values);

    }

    private synchronized void addToBeDrawn(GameObject gameObject) {
        toBeDrawn.add(gameObject);
    }

    private synchronized GameObject getObjectToBeDrawn() {
        return toBeDrawn.poll();
    }

    private void drawSelf (Canvas canvas, float[] values) {

        int multiplier = 50;

        if (values[1] < 0) {
            if (myY + (int) (Math.abs(values[1]) * multiplier) > 0)
                myY = myY + (int) (Math.abs(values[1]) * multiplier);
        } else {
            if (myY - (int) (Math.abs(values[1]) * multiplier) < canvasHeight)
            myY = myY - (int) (Math.abs(values[1]) * multiplier);
        }

        if (values[2] < 0) {
            if (myX - (int) (Math.abs(values[2]) * multiplier) > 0)
            myX = myX - (int) (Math.abs(values[2]) * multiplier);
        } else {
            if (myX + (int) (Math.abs(values[2]) * multiplier) < canvasWidth)
            myX = myX + (int) (Math.abs(values[2]) * multiplier);
        }

        canvas.drawCircle(myX, myY, mySize, myColor);
    }

    private void drawObject(Canvas canvas, GameObject object) {
        canvas.drawRect(object.x
                , object.y
                , object.x + object.size
                , object.y + object.size
                , object.color);
    }


    abstract class GameObject {
        int x, y, size;
        Paint color;
        private ObjectThread thread;

        GameObject(int x, int y, int size, int color, int delay, Object data) {
            this.x = x;
            this.y = y;
            this.size = size;
            Paint p = new Paint();
            p.setColor(color);
            this.color = p;
            this.thread = new ObjectThread(this, delay, data);
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void start(){
            thread.start();
        }

        public abstract void performAction (Object data);

        class ObjectThread extends Thread {

            int delay;
            GameObject object;
            Object data;
            boolean running = true;

            ObjectThread(GameObject object, int delay, Object data) {
                this.object = object;
                this.delay = delay;
                this.data = data;

            }

            public void run () {
                while (running) {
                    try {
                        Thread.sleep(delay);
                        object.performAction(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    interface Rotatable {
        float[] getRotationInfo();
    }
}
