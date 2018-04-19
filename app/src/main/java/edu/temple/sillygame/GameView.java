package edu.temple.sillygame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;



public class GameView extends View {

    int canvasWidth, canvasHeight, buttonX = 100, buttonY = 100;
    Paint myColor, scoreColor;

    public GameObject enemy, otherEnemy;

    public int myX, myY, mySize;

    int score = 0;

    Context context;

    Queue<GameObject> toBeDrawn = new LinkedList<>();

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        myX = 0;
        myY = 0;
        mySize = 100;

        scoreColor = new Paint();
        scoreColor.setColor(Color.BLACK);

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
                }
                else {
                    if ((y + movePixels) + size < canvasHeight) y += movePixels;
                    else yDirection = 0;
                }

                addToBeDrawn(this);

            }

        };

        otherEnemy.addCollisionListener(new GameObject.CollisionListener(){
            @Override
            public void collisionOccurred(){
                score = score + 1;
            }
        });

        otherEnemy.start();


    }

    float[] values = new float[3];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        GameObject object;

        while ((object = getObjectToBeDrawn()) != null) {
            updatePlayer(object);
            drawObject(canvas, object);
        }

        values = ((Rotatable) context).getRotationInfo();

        drawSelf(canvas, values);

        drawScore(canvas, score);
    }

    private void updatePlayer(GameObject object) {
        object.setpX(myX);
        object.setpY(myY);
        object.setpSize(mySize);
    }

    private void drawScore(Canvas canvas, int score) {

        final float testTextSize = 32f;

        // Get the bounds of the text, using our testTextSize.
        scoreColor.setTextSize(testTextSize);
        Rect bounds = new Rect();
        scoreColor.getTextBounds(String.valueOf(score), 0, String.valueOf(score).length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * 200 / bounds.width();

        // Set the paint for that size.
        scoreColor.setTextSize(desiredTextSize);

        canvas.drawText(String.valueOf(score), 10, canvas.getHeight() - 10, scoreColor);
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
        }
        else {
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

        if (myY < 0){
            myY = 0;
        }
        else if (myY > canvasHeight)
        {
            myY = canvasHeight;
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


    abstract static class GameObject {
        int x, y, size;
        int pX, pY, pSize;
        Paint color;
        private ObjectThread thread;
        private List<CollisionListener> listeners = new ArrayList<CollisionListener>();

        GameObject(int x, int y, int size, int color, int delay, Object data) {
            this.x = x;
            this.y = y;
            this.size = size;

            this.pX = 0;
            this.pY = 0;
            this.pSize = 10;

            Paint p = new Paint();
            p.setColor(color);
            this.color = p;
            this.thread = new ObjectThread(this, delay, data);

            Thread thread = new Thread(){
                public void run(){
                    collisionChecker();
                }
            };

            thread.start();
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


        public void addCollisionListener(CollisionListener listener) {
            listeners.add(listener);
        }

        public void collisionChecker() {
            while(true) {
                // Notify everybody that may be interested.
                for (CollisionListener cl : listeners) {
                    //Circle c = new Circle(pX, pY, pSize);
                    //Circle c = new Circle(0f, 0f, 0f);

                    //Rectangle r = new Rectangle(x, y, size, size);
                    if (collides()){
                        cl.collisionOccurred();
                    }
                }
            }
        }

        private boolean collides() {
            float closestX = clamp((float) pX, x, x + size );
            float closestY = clamp((float) pY, y - size, y);

            float distanceX = (float) pX - closestX;
            float distanceY = (float) (pY - closestY);

            return Math.pow(distanceX, 2) + Math.pow(distanceY, 2) < Math.pow(pSize, 2);
        }

        public static float clamp(float value, double min, double max) {
            double x = value;
            if (x < min) {
                x = min;
            } else if (x > max) {
                x = max;
            }
            return (float) x;
        }

        public int getpX() {
            return pX;
        }

        public void setpX(int pX) {
            this.pX = pX;
        }

        public int getpY() {
            return pY;
        }

        public void setpY(int pY) {
            this.pY = pY;
        }

        public int getpSize() {
            return pSize;
        }

        public void setpSize(int pSize) {
            this.pSize = pSize;
        }


        public interface CollisionListener {
            void collisionOccurred();
        }

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
