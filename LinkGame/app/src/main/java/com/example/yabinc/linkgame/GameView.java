package com.example.yabinc.linkgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;

/**
 * Created by yabinc on 1/21/16.
 */
public class GameView extends View {
    private static final String LOG_TAG = "GameView";
    private static final int ANIMAL_BITMAP_WIDTH = 100;
    private static final int ANIMAL_BITMAP_HEIGHT = 100;

    private int GAME_IMAGE_LENGTH = 14;
    private int GAME_IMAGE_WIDTH = 9;

    private Rect viewRect = new Rect();
    private int viewWidth;
    private int viewHeight;
    private Bitmap mAnimalBitmap;
    private ArrayList<Bitmap> mAnimalBitmaps;
    private Paint mLinePaint;
    private Paint mSelectedImgPaint;

    private State[][] states;

    int selectedRow = -1;
    int selectedCol = -1;

    private GestureDetector mDetector;

    private Handler myHandler;
    private boolean isDrawLine = false;

    class State {
        final static int IMAGE_UNSELECTED = 0;
        final static int IMAGE_SELECTED = 1;
        final static int EMPTY = 2;
        int state;
        int imgIndex;

        State(int state, int imgIndex) {
            this.state = state;
            this.imgIndex = imgIndex;
        }
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void init(Bitmap bitmap) {
        mAnimalBitmap = bitmap;
        mAnimalBitmaps = new ArrayList<Bitmap>();
        for (int y = 0; y + ANIMAL_BITMAP_HEIGHT <= bitmap.getHeight(); y += ANIMAL_BITMAP_HEIGHT) {
            for (int x = 0; x + ANIMAL_BITMAP_WIDTH <= bitmap.getWidth(); x += ANIMAL_BITMAP_WIDTH) {
                if (!isBlank(bitmap, x, y, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT)) {
                    mAnimalBitmaps.add(Bitmap.createBitmap(bitmap, x, y,
                            ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT));
                }
            }
        }
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(20);
        mSelectedImgPaint = new Paint();
        mSelectedImgPaint.setAlpha(60);
        myHandler = new Handler();
        mDetector = new GestureDetector(getContext(), new MyGestureListener());
    }

    private boolean isBlank(Bitmap bitmap, int x, int y, int width, int height) {
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int color = bitmap.getPixel(x + i, y + j);
                if ((color & 0xffffff) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewRect.top = 0;
        viewRect.bottom = h;
        viewRect.left = 0;
        viewRect.right = w;
        initState();
    }

    private void initState() {
        int rows = -1;
        int cols = -1;
        if (viewRect.width() > viewRect.height()) {
            rows = GAME_IMAGE_WIDTH;
            cols = GAME_IMAGE_LENGTH;
        } else {
            rows = GAME_IMAGE_LENGTH;
            cols = GAME_IMAGE_WIDTH;
        }
        if (states != null) {
            if (states.length == rows && states[0].length == cols) {
                return;
            }
            if (states.length == cols && states[0].length == rows) {
                // Swap row and col
                State[][] newStates = new State[rows][cols];
                for (int i = 0; i < rows; ++i) {
                    for (int j = 0; j < cols; ++j) {
                        newStates[i][j] = states[j][i];
                    }
                }
                states = newStates;
                return;
            }
        }
        // Construct new states.
        Random random = new Random(0);
        states = new State[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                int imgIndex = random.nextInt(mAnimalBitmaps.size());
                states[i][j] = new State(State.IMAGE_UNSELECTED, imgIndex);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);

        int rows = states.length;
        int cols = states[0].length;
        viewWidth = Math.min(viewRect.width() / cols, viewRect.height() / rows);
        viewHeight = viewWidth;

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (states[r][c].state == State.IMAGE_UNSELECTED) {
                    canvas.drawBitmap(mAnimalBitmaps.get(states[r][c].imgIndex),
                            new Rect(0, 0, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT),
                            new Rect(c * viewWidth, r * viewHeight,
                                    (c + 1) * viewWidth, (r + 1) * viewHeight), null);
                } else if (states[r][c].state == State.IMAGE_SELECTED){
                    canvas.drawBitmap(mAnimalBitmaps.get(states[r][c].imgIndex),
                            new Rect(0, 0, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT),
                            new Rect(c * viewWidth, r * viewHeight,
                                    (c + 1) * viewWidth, (r + 1) * viewHeight), mSelectedImgPaint);
                }
            }
        }

        if (isDrawLine) {
            canvas.drawLine(viewWidth / 2, viewHeight / 2, viewWidth * 3 / 2, viewHeight / 2, mLinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(LOG_TAG, "onTouchEvent, e.action = " + event.getAction() + ", rawX = " + event.getRawX()
                + ", rawY = " + event.getRawY());
        return mDetector.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(LOG_TAG, "onSingleTagUp, e.action = " + e.getAction() + ", X = " + e.getX()
                            + ", Y = " + e.getY());
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(LOG_TAG, "onSingleTagConfirmed, e.action = " + e.getAction() + ", X = " + e.getX()
                    + ", Y = " + e.getY());
            tapPos(e.getX(), e.getY());
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(LOG_TAG, "onDown, e.action = " + e.getAction() + ", X = " + e.getX()
                    + ", Y = " + e.getY());
            return true;
        }
    }

    private void tapPos(float x, float y) {
        int c = (int)(x / viewWidth);
        int r = (int)(y / viewHeight);
        if (r < 0 || r >= states.length || c < 0 || c >= states[0].length) {
            return;
        }
        Log.d(LOG_TAG, "tapPos, x " + x + ", y " + y + ", w " + viewWidth + ", h " + viewHeight + ", c " + c + ", r " + r);
        if (states[r][c].state == State.IMAGE_UNSELECTED) {
            states[r][c].state = State.IMAGE_SELECTED;
            if (selectedRow == -1) {
                selectedRow = r;
                selectedCol = c;
            } else {
                if (canErase(r, c, selectedRow, selectedCol)) {
                    states[r][c].state = State.EMPTY;
                    states[selectedRow][selectedCol].state = State.EMPTY;
                    selectedRow = -1;
                    selectedCol = -1;
                } else {
                    states[selectedRow][selectedCol].state = State.IMAGE_UNSELECTED;
                    selectedRow = r;
                    selectedCol = c;
                }
            }
        } else if (states[r][c].state == State.IMAGE_SELECTED) {
            states[r][c].state = State.IMAGE_UNSELECTED;
            selectedRow = -1;
            selectedCol = -1;
        }
        invalidate();
    }

    private boolean canErase(int r1, int c1, int r2, int c2) {
        boolean result =  (states[r1][c1].imgIndex == states[r2][c2].imgIndex);
        Log.d(LOG_TAG, "canErase, " + r1 + ", " + c1 + ", " + r2 + ", " + c2 + ", img " +
                states[r1][c1].imgIndex + ", " + states[r2][c2].imgIndex + ", res " + result);
        return result;
    }

    /*
    @Override
    public void onClick(View view) {
        myHandler.post(myRunnable);
    }

    final Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (isDrawLine == true) {
                return;
            }
            isDrawLine = true;
            invalidate();
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    myHandler.post(myRunnable2);
                }
            }, 1000);
        }
    };

    final Runnable myRunnable2 = new Runnable() {
        @Override
        public void run() {
            isDrawLine = false;
            invalidate();
        }
    };
    */
}
