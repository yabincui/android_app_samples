package com.example.yabinc.linkgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.Attributes;

/**
 * Created by yabinc on 1/21/16.
 */
public class GameView extends View {

    private static final int ANIMAL_BITMAP_WIDTH = 100;
    private static final int ANIMAL_BITMAP_HEIGHT = 100;
    private static final String LOG_TAG = "GameView";
    private static final int INIT_TIME_IN_SEC = 5 * 60;
    private static final int ERASE_INC_TIME_IN_SEC = 5;

    private int mLevel;
    private Timer mTimer;
    private int leftTimeInSec;
    private Rect timeRect;
    private Rect viewRect;
    private int viewWidth;
    private int viewHeight;
    private Bitmap mWinBitmap;
    private Bitmap mLoseBitmap;
    private ArrayList<Bitmap> mAnimalBitmaps;
    private Paint mLinePaint;
    private Paint mSelectedImgPaint;
    private Paint mHintPaint;
    private Paint mBlackPaint;
    private Paint mGreenPaint;

    private GestureDetector mDetector;

    private Handler myHandler;

    private GameLogic.State[][] states;

    int selectedRow = -1;
    int selectedCol = -1;

    private GameLogic.LinkPath linkPath;
    private GameLogic.LinkPath hintPath;


    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void init(Bitmap animalBitmap, Bitmap winBitmap, Bitmap loseBitmap) {
        mWinBitmap = winBitmap;
        mLoseBitmap = loseBitmap;
        mAnimalBitmaps = new ArrayList<Bitmap>();
        for (int y = 0; y + ANIMAL_BITMAP_HEIGHT <= animalBitmap.getHeight(); y += ANIMAL_BITMAP_HEIGHT) {
            for (int x = 0; x + ANIMAL_BITMAP_WIDTH <= animalBitmap.getWidth(); x += ANIMAL_BITMAP_WIDTH) {
                if (!isBlank(animalBitmap, x, y, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT)) {
                    mAnimalBitmaps.add(Bitmap.createBitmap(animalBitmap, x, y,
                            ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT));
                }
            }
        }
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(20);
        mSelectedImgPaint = new Paint();
        mSelectedImgPaint.setAlpha(0xa0);
        mHintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHintPaint.setColor(Color.RED);
        mHintPaint.setStrokeWidth(10);
        mHintPaint.setStyle(Paint.Style.STROKE);
        mBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBlackPaint.setColor(Color.BLACK);
        mBlackPaint.setStrokeWidth(10);
        mBlackPaint.setStyle(Paint.Style.STROKE);
        mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGreenPaint.setColor(Color.GREEN);
        mGreenPaint.setStyle(Paint.Style.FILL);
        myHandler = new MyHandler();
        mDetector = new GestureDetector(getContext(), new MyGestureListener());
        mLevel = 1;
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

    private void initState() {
        states = GameLogic.initState(viewRect.width(), viewRect.height(), mAnimalBitmaps.size(),
                getResources().getDisplayMetrics().densityDpi, states);
        selectedRow = -1;
        selectedCol = -1;
        linkPath = null;
        hintPath = null;
        leftTimeInSec = INIT_TIME_IN_SEC;
        startTimer();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        timeRect = new Rect(0, 0, w, h / 10);
        viewRect = new Rect(0, timeRect.bottom, w, h);
        initState();
    }

    private void showImage(Canvas canvas, Bitmap bitmap) {
        float factor = Math.min((float)viewRect.width() / bitmap.getWidth(),
                (float)viewRect.height() / bitmap.getHeight());
        int left = (viewRect.width() - (int)(bitmap.getWidth() * factor)) / 2;
        int top = (viewRect.height() - (int)(bitmap.getHeight() * factor)) / 2;
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(left, top, left + (int)(bitmap.getWidth() * factor),
                        top + (int)(bitmap.getHeight() * factor)), null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);

        if (GameLogic.isSuccess(states)) {
            showImage(canvas, mWinBitmap);
            stopTimer();
            return;
        }
        if (leftTimeInSec == 0) {
            showImage(canvas, mLoseBitmap);
            stopTimer();
            return;
        }

        int timeWidth = timeRect.width() * 2 / 3;
        int timeHeight = Math.min(timeRect.height() * 2 / 3, 50);
        Rect timeR = new Rect();
        timeR.left = timeRect.left + (timeRect.width() - timeWidth) / 2;
        timeR.right = timeR.left + timeWidth;
        timeR.top = timeRect.top + (timeRect.height() - timeHeight) / 2;
        timeR.bottom = timeR.top + timeHeight;
        canvas.drawRect(timeR, mBlackPaint);
        timeR.right = timeR.left + timeR.width() * leftTimeInSec / INIT_TIME_IN_SEC;
        canvas.drawRect(timeR, mGreenPaint);

        int rows = states.length;
        int cols = states[0].length;
        viewWidth = Math.min(viewRect.width() / cols, viewRect.height() / rows);
        viewHeight = viewWidth;
        Log.d(LOG_TAG, "r " + rows + ", c " + cols + ", w " + viewRect.width() + ", h " + viewRect.height() + "vw " + viewWidth);

        Rect src = new Rect(0, 0, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT);
        Rect dst = new Rect();
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (states[r][c].state == GameLogic.State.IMAGE_UNSELECTED) {
                    dst.set(0, 0, viewWidth, viewHeight);
                    dst.offset(viewRect.left + c * viewWidth, viewRect.top + r * viewHeight);
                    canvas.drawBitmap(mAnimalBitmaps.get(states[r][c].imgIndex),
                            src, dst, null);
                } else if (states[r][c].state == GameLogic.State.IMAGE_SELECTED){
                    dst.set(0, 0, viewWidth, viewHeight);
                    dst.offset(viewRect.left + c * viewWidth, viewRect.top + r * viewHeight);
                    canvas.drawBitmap(mAnimalBitmaps.get(states[r][c].imgIndex),
                            src, dst, mSelectedImgPaint);
                }
            }
        }

        if (linkPath != null) {
            int lineCount = linkPath.pointsR.size() - 1;
            float[] points = new float[lineCount * 4];
            for (int i = 0, j = 0; i < linkPath.pointsR.size(); ++i, j += 2) {
                int r = linkPath.pointsR.get(i);
                int c = linkPath.pointsC.get(i);
                if (c >= 0 && c < cols) {
                    points[j] = c * viewWidth + viewWidth / 2;
                } else if (c == -1) {
                    points[j] = 0;
                } else if (c == cols) {
                    points[j] = cols * viewWidth;
                }
                points[j] += viewRect.left;

                if (r >= 0 && r < rows) {
                    points[j + 1] = r * viewHeight + viewHeight / 2;
                } else if (r == -1) {
                    points[j + 1] = 0;
                } else if (r == rows) {
                    points[j + 1] = rows * viewHeight;
                }
                points[j + 1] += viewRect.top;
                if (i > 0 && i < linkPath.pointsR.size() - 1) {
                    j += 2;
                    points[j] = points[j - 2];
                    points[j + 1] = points[j - 1];
                }
            }
            canvas.drawLines(points, mLinePaint);
        }
        if (hintPath != null) {
            Log.d(LOG_TAG, "draw hintPath");
            for (int i = 0; i < hintPath.pointsR.size(); i += hintPath.pointsR.size() - 1) {
                int r = hintPath.pointsR.get(i);
                int c = hintPath.pointsC.get(i);
                dst.set(0, 0, viewWidth, viewHeight);
                dst.offset(viewRect.left + c * viewWidth, viewRect.top + r * viewHeight);
                canvas.drawRect(dst, mHintPaint);
            }
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
            tapPos(e.getX(), e.getY());
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(LOG_TAG, "onSingleTagConfirmed, e.action = " + e.getAction() + ", X = " + e.getX()
                    + ", Y = " + e.getY());

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
        if (GameLogic.isSuccess(states) || leftTimeInSec == 0) {
            restart();
            return;
        }
        if (linkPath != null || !viewRect.contains((int)x, (int)y)) {
            return;
        }
        x -= viewRect.left;
        y -= viewRect.top;
        int c = (int)(x / viewWidth);
        int r = (int)(y / viewHeight);
        if (r < 0 || r >= states.length || c < 0 || c >= states[0].length) {
            return;
        }
        Log.d(LOG_TAG, "tapPos, x " + x + ", y " + y + ", w " + viewWidth + ", h " + viewHeight + ", c " + c + ", r " + r);
        if (states[r][c].state == GameLogic.State.IMAGE_UNSELECTED) {
            states[r][c].state = GameLogic.State.IMAGE_SELECTED;
            if (selectedRow == -1) {
                selectedRow = r;
                selectedCol = c;
            } else {
                GameLogic.LinkPath path = new GameLogic.LinkPath();
                if (GameLogic.canErase(r, c, selectedRow, selectedCol, states, path)) {
                    linkPath = path;
                    selectedRow = -1;
                    selectedCol = -1;
                    hintPath = null;
                    leftTimeInSec = Math.min(leftTimeInSec + ERASE_INC_TIME_IN_SEC, INIT_TIME_IN_SEC);
                    myHandler.sendEmptyMessageDelayed(MyHandler.MSG_ERASE_LINK_PATH, 200);
                } else {
                    states[selectedRow][selectedCol].state = GameLogic.State.IMAGE_UNSELECTED;
                    selectedRow = r;
                    selectedCol = c;
                }
            }
        } else if (states[r][c].state == GameLogic.State.IMAGE_SELECTED) {
            states[r][c].state = GameLogic.State.IMAGE_UNSELECTED;
            selectedRow = -1;
            selectedCol = -1;
        }
        invalidate();
    }

    private class MyHandler extends Handler {
        static final int MSG_ERASE_LINK_PATH = 0;
        static final int MSG_UPDATE_TIME = 1;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ERASE_LINK_PATH) {
                if (linkPath != null) {
                    int len = linkPath.pointsR.size();
                    states[linkPath.pointsR.get(0)][linkPath.pointsC.get(0)].state = GameLogic.State.EMPTY;
                    states[linkPath.pointsR.get(len-1)][linkPath.pointsC.get(len-1)].state = GameLogic.State.EMPTY;
                    linkPath = null;
                    if (!GameLogic.isSuccess(states)) {
                        GameLogic.LinkPath path = new GameLogic.LinkPath();
                        while (!GameLogic.haveErasablePair(states, path)) {
                            GameLogic.shuffleStates(states);
                        }
                    }
                    invalidate();
                }
            } else if (msg.what == MSG_UPDATE_TIME) {
                if (leftTimeInSec != 0) {
                    leftTimeInSec--;
                    invalidate();
                }
            }
        }
    }

    public void hint() {
        GameLogic.LinkPath path = new GameLogic.LinkPath();
        Log.d(LOG_TAG, "hint");
        if (hintPath == null && GameLogic.haveErasablePair(states, path)) {
            Log.d(LOG_TAG, "haveErasablePair");
            hintPath = path;
            invalidate();
        }
    }

    public void restart() {
        states = null;
        initState();
        invalidate();
    }

    public void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    myHandler.sendEmptyMessage(MyHandler.MSG_UPDATE_TIME);
                }
            }, 1000, 1000);
        }
    }

    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
    }

    public void setLevel(int level) {
        mLevel = level;
        invalidate();
    }
}
