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



    private Rect viewRect = new Rect();
    private int viewWidth;
    private int viewHeight;
    private Bitmap mAnimalBitmap;
    private ArrayList<Bitmap> mAnimalBitmaps;
    private Paint mLinePaint;
    private Paint mSelectedImgPaint;
    private Paint mHintPaint;

    private GameLogic.State[][] states;

    int selectedRow = -1;
    int selectedCol = -1;

    private GameLogic.LinkPath hintPath;

    private GestureDetector mDetector;

    private Handler myHandler;
    private GameLogic.LinkPath linkPath;



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
        mHintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHintPaint.setColor(Color.RED);
        mHintPaint.setStrokeWidth(15);
        mHintPaint.setStyle(Paint.Style.STROKE);
        myHandler = new MyHandler();
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
        states = GameLogic.initState(w, h, mAnimalBitmaps.size(), states);
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
                if (states[r][c].state == GameLogic.State.IMAGE_UNSELECTED) {
                    canvas.drawBitmap(mAnimalBitmaps.get(states[r][c].imgIndex),
                            new Rect(0, 0, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT),
                            new Rect(c * viewWidth, r * viewHeight,
                                    (c + 1) * viewWidth, (r + 1) * viewHeight), null);
                } else if (states[r][c].state == GameLogic.State.IMAGE_SELECTED){
                    canvas.drawBitmap(mAnimalBitmaps.get(states[r][c].imgIndex),
                            new Rect(0, 0, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT),
                            new Rect(c * viewWidth, r * viewHeight,
                                    (c + 1) * viewWidth, (r + 1) * viewHeight), mSelectedImgPaint);
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

                if (r >= 0 && r < rows) {
                    points[j + 1] = r * viewHeight + viewHeight / 2;
                } else if (r == -1) {
                    points[j + 1] = 0;
                } else if (r == rows) {
                    points[j + 1] = rows * viewHeight;
                }
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
                canvas.drawRect(c * viewWidth, r * viewHeight, (c + 1) * viewWidth,
                        (r + 1) * viewHeight, mHintPaint);
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
        if (linkPath != null) {
            return;
        }
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
}
