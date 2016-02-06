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
public class GameView extends View implements GameState.OnStateChangeListener {

    private static final int ANIMAL_BITMAP_WIDTH = 100;
    private static final int ANIMAL_BITMAP_HEIGHT = 100;
    private static final String LOG_TAG = "GameView";

    private PictureInfo mPictureInfo;
    private Paint mLinePaint;
    private Paint mSelectedImgPaint;
    private Paint mHintPaint;
    private Paint mBlackPaint;
    private Paint mGreenPaint;

    private GestureDetector mDetector;

    private SizeInfo mSizeInfo;
    private ViewInfo mViewInfo;
    private LevelInfo mLevelInfo;
    private GameListener mGameListener;
    private GameState mGameState;

    public interface GameListener {
        public void onLevelChange(LevelInfo info);
        public void onBlockClick();
        public void onBlockPairErase();
        public void onWin(int curLevel, double leftTimePercent);
    }

    static class PictureArg {
        Bitmap animalBitmap;
        Bitmap winBitmap;
        Bitmap loseBitmap;
        Bitmap pauseBitmap;
    }

    class PictureInfo {
        Bitmap winBitmap;
        Bitmap loseBitmap;
        Bitmap pauseBitmap;
        ArrayList<Bitmap> animalBitmaps;
    }

    class ViewInfo {
        Rect globalRect;
        Rect timeRect;
        Rect blockRect;
        int blockWidth;
        int blockHeight;
    }


    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void init(PictureArg pictureArg, SizeInfo sizeInfo, LevelInfo levelInfo,
                     GameListener gameListener) {
        mLevelInfo = levelInfo;
        mGameListener = gameListener;
        mPictureInfo = new PictureInfo();
        mPictureInfo.winBitmap = pictureArg.winBitmap;
        mPictureInfo.loseBitmap = pictureArg.loseBitmap;
        mPictureInfo.pauseBitmap = pictureArg.pauseBitmap;
        mPictureInfo.animalBitmaps = new ArrayList<Bitmap>();
        for (int y = 0; y + ANIMAL_BITMAP_HEIGHT <= pictureArg.animalBitmap.getHeight(); y += ANIMAL_BITMAP_HEIGHT) {
            for (int x = 0; x + ANIMAL_BITMAP_WIDTH <= pictureArg.animalBitmap.getWidth(); x += ANIMAL_BITMAP_WIDTH) {
                if (!isBlank(pictureArg.animalBitmap, x, y, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT)) {
                    mPictureInfo.animalBitmaps.add(Bitmap.createBitmap(pictureArg.animalBitmap, x, y,
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
        mDetector = new GestureDetector(getContext(), new MyGestureListener());
        mSizeInfo = sizeInfo;
        mViewInfo = new ViewInfo();
        mGameState = new GameState(this);
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
        Block[][] blocks = null;
        if (mSizeInfo.useDefaultSize) {
            blocks = GameLogic.createBlocksByScreen(mViewInfo.blockRect.width(), mViewInfo.blockRect.height(),
                    mPictureInfo.animalBitmaps.size(), getResources().getDisplayMetrics().densityDpi);
        } else {
            blocks = GameLogic.createBlocks(mSizeInfo.rows, mSizeInfo.cols, mPictureInfo.animalBitmaps.size());
        }
        mGameState.start(blocks, mLevelInfo.curLevel);
        ((MainActivity)getContext()).setTitleByLevel(mLevelInfo.curLevel);
    }

    @Override
    public void onStateChange(GameState state) {
        invalidate();
    }

    @Override
    public void onWin(GameState state, double leftTimePercent) {
        mGameListener.onWin(mLevelInfo.curLevel, leftTimePercent);
        invalidate();
    }

    @Override
    public void onLose(GameState state) {
        invalidate();
    }

    @Override
    public void onBlockClick() {
        mGameListener.onBlockClick();
    }

    @Override
    public void onBlockPairErase() {
        mGameListener.onBlockPairErase();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewInfo.globalRect = new Rect(0, 0, w, h);
        mViewInfo.timeRect = new Rect(0, 0, w, h / 10);
        mViewInfo.blockRect = new Rect(0, mViewInfo.timeRect.bottom, w, h);
        if (mGameState.isBeforeStart()) {
            initState();
        }
    }

    private void showImage(Canvas canvas, Bitmap bitmap) {
        float factor = Math.min((float)mViewInfo.globalRect.width() / bitmap.getWidth(),
                (float)mViewInfo.globalRect.height() / bitmap.getHeight());
        int left = (mViewInfo.globalRect.width() - (int)(bitmap.getWidth() * factor)) / 2;
        int top = (mViewInfo.globalRect.height() - (int)(bitmap.getHeight() * factor)) / 2;
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                new Rect(left, top, left + (int) (bitmap.getWidth() * factor),
                        top + (int) (bitmap.getHeight() * factor)), null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);

        if (mGameState.isRun()) {
            showBlocks(canvas);
        } else if (mGameState.isSuccess()) {
            showImage(canvas, mPictureInfo.winBitmap);
        } else if (mGameState.isLose()) {
            showImage(canvas, mPictureInfo.loseBitmap);
        } else if (mGameState.isPause()) {
            showImage(canvas, mPictureInfo.pauseBitmap);
        }
    }

    private void showBlocks(Canvas canvas) {
        int timeWidth = mViewInfo.timeRect.width() * 2 / 3;
        int timeHeight = Math.min(mViewInfo.timeRect.height() * 2 / 3, 50);
        Rect timeR = new Rect();
        timeR.left = mViewInfo.timeRect.left + (mViewInfo.timeRect.width() - timeWidth) / 2;
        timeR.right = timeR.left + timeWidth;
        timeR.top = mViewInfo.timeRect.top + (mViewInfo.timeRect.height() - timeHeight) / 2;
        timeR.bottom = timeR.top + timeHeight;
        canvas.drawRect(timeR, mBlackPaint);
        timeR.right = timeR.left + (int)(timeR.width() * mGameState.getLeftTimePercent());
        canvas.drawRect(timeR, mGreenPaint);

        Block[][] blocks = mGameState.getBlocks();
        int rows = blocks.length;
        int cols = blocks[0].length;
        int blockSize = Math.min(mViewInfo.blockRect.width() / cols, mViewInfo.blockRect.height() / rows);
        mViewInfo.blockHeight = mViewInfo.blockWidth = blockSize;
        Log.d(LOG_TAG, "r " + rows + ", c " + cols + ", w " + mViewInfo.blockRect.width() + ", h "
                + mViewInfo.blockRect.height() + "block " + blockSize);

        Rect src = new Rect(0, 0, ANIMAL_BITMAP_WIDTH, ANIMAL_BITMAP_HEIGHT);
        Rect dst = new Rect();
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (blocks[r][c].isUnselected()) {
                    dst.set(0, 0, mViewInfo.blockWidth, mViewInfo.blockHeight);
                    dst.offset(mViewInfo.blockRect.left + c * mViewInfo.blockWidth,
                                mViewInfo.blockRect.top + r * mViewInfo.blockHeight);
                    canvas.drawBitmap(mPictureInfo.animalBitmaps.get(blocks[r][c].imgIndex),
                            src, dst, null);
                } else if (blocks[r][c].isSelected()) {
                    dst.set(0, 0, mViewInfo.blockWidth, mViewInfo.blockHeight);
                    dst.offset(mViewInfo.blockRect.left + c * mViewInfo.blockWidth,
                            mViewInfo.blockRect.top + r * mViewInfo.blockHeight);
                    canvas.drawBitmap(mPictureInfo.animalBitmaps.get(blocks[r][c].imgIndex),
                            src, dst, mSelectedImgPaint);
                }
            }
        }

        ArrayList<Point> linkPath = mGameState.getLinkPath();
        if (linkPath != null) {
            int lineCount = linkPath.size() - 1;
            float[] points = new float[lineCount * 4];
            for (int i = 0, j = 0; i < linkPath.size(); ++i, j += 2) {
                int r = linkPath.get(i).row;
                int c = linkPath.get(i).col;
                if (c >= 0 && c < cols) {
                    points[j] = c * mViewInfo.blockWidth + mViewInfo.blockWidth / 2;
                } else if (c == -1) {
                    points[j] = 0;
                } else if (c == cols) {
                    points[j] = cols * mViewInfo.blockWidth;
                }
                points[j] += mViewInfo.blockRect.left;

                if (r >= 0 && r < rows) {
                    points[j + 1] = r * mViewInfo.blockHeight + mViewInfo.blockHeight / 2;
                } else if (r == -1) {
                    points[j + 1] = 0;
                } else if (r == rows) {
                    points[j + 1] = rows * mViewInfo.blockHeight;
                }
                points[j + 1] += mViewInfo.blockRect.top;
                if (i > 0 && i < linkPath.size() - 1) {
                    j += 2;
                    points[j] = points[j - 2];
                    points[j + 1] = points[j - 1];
                }
            }
            canvas.drawLines(points, mLinePaint);
        }
        ArrayList<Point> hintPoints = mGameState.getHintPoints();
        if (hintPoints != null) {
            Log.d(LOG_TAG, "draw hintPath r1 " + hintPoints.get(0).row
                    + ", c1 " + hintPoints.get(0).col
                    + ", r2 " + hintPoints.get(1).row
                    + ", c2 " + hintPoints.get(1).col);
            for (int i = 0; i < 2; i++) {
                int r = hintPoints.get(i).row;
                int c = hintPoints.get(i).col;
                dst.set(0, 0, mViewInfo.blockWidth, mViewInfo.blockHeight);
                dst.offset(mViewInfo.blockRect.left + c * mViewInfo.blockWidth,
                        mViewInfo.blockRect.top + r * mViewInfo.blockHeight);
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
            tapPos(e.getX(), e.getY());
            return true;
        }
    }

    private void tapPos(float x, float y) {
        if (mGameState.isSuccess()) {
            mLevelInfo.moveToNextLevel();
            mGameListener.onLevelChange(mLevelInfo);
            restart();
        } else if (mGameState.isLose()) {
            restart();
        } else if (mGameState.isPause()) {
            resume();
        } else if (mGameState.isRun()) {
            if (mGameState.getLinkPath() != null || !mViewInfo.blockRect.contains((int) x, (int) y)) {
                return;
            }
            x -= mViewInfo.blockRect.left;
            y -= mViewInfo.blockRect.top;
            int c = (int) (x / mViewInfo.blockWidth);
            int r = (int) (y / mViewInfo.blockHeight);
            if (r < 0 || r >= mGameState.getBlocks().length || c < 0 || c >= mGameState.getBlocks()[0].length) {
                return;
            }
            Log.d(LOG_TAG, "tapPos, x " + x + ", y " + y + ", w " + mViewInfo.blockWidth + ", h "
                    + mViewInfo.blockHeight + ", c " + c + ", r " + r);
            mGameState.selectBlock(r, c);
        }
    }

    public void hint() {
        mGameState.giveHint();
    }

    public void restart() {
        initState();
    }

    public void pause() {
        mGameState.pause();
    }

    public boolean isPaused() {
        return mGameState.isPause();
    }

    public void resume() {
        mGameState.resumeFromPause();
    }

    public void startTimer() {
        mGameState.startTimer();
    }

    public void stopTimer() {
        mGameState.stopTimer();
    }

    public SizeInfo getSize() {
        return mSizeInfo;
    }

    public boolean setSize(SizeInfo info) {
        mSizeInfo = info;
        initState();
        invalidate();
        return true;
    }

    public LevelInfo getLevelInfo() {
        return mLevelInfo;
    }

    public void setLevelInfo(LevelInfo levelInfo) {
        int oldLevel = mLevelInfo.curLevel;
        mLevelInfo = levelInfo;
        mGameListener.onLevelChange(mLevelInfo);
        if (oldLevel != levelInfo.curLevel) {
            restart();
        }
    }
}
