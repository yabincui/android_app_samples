package com.example.yabinc.sudogame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yabinc on 6/27/16.
 */
public class GameView extends View {
    private final static String TAG = "GameView";

    private GameModel mGameModel;

    private class ViewInfo {
        Rect globalRect;
        Rect blockRegionRect;
        int blockWidth;
        int blockHeight;
    }

    private ViewInfo mViewInfo;

    private Paint mLinePaint;
    private Paint mFixedDigitPaint;
    private Paint mGuessDigitPaint;
    private Paint mWrongGuessDigitPaint;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(GameModel gameModel) {
        mGameModel = gameModel;
        mViewInfo = new ViewInfo();
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(10);
        mFixedDigitPaint = new Paint();
        mFixedDigitPaint.setStyle(Paint.Style.FILL);
        mFixedDigitPaint.setColor(Color.BLACK);
        mFixedDigitPaint.setTextSize(100);
        mFixedDigitPaint.setTextAlign(Paint.Align.CENTER);
        mGuessDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGuessDigitPaint.setColor(Color.GREEN);
        mGuessDigitPaint.setStrokeWidth(50);
        mWrongGuessDigitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWrongGuessDigitPaint.setColor(Color.RED);
        mWrongGuessDigitPaint.setStrokeWidth(50);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewInfo.globalRect = new Rect(0, 0, w, h);
        int blockRegionWidth = w - 0;
        int blockRegionHeight = h - 0;
        blockRegionHeight = blockRegionWidth = Math.min(blockRegionHeight, blockRegionWidth);
        int blockRegionWidthStart = (w - blockRegionWidth) / 2;
        int blockRegionHeightStart = (h - blockRegionHeight) / 2;
        mViewInfo.blockRegionRect = new Rect(blockRegionWidthStart, blockRegionHeightStart,
                blockRegionWidthStart + blockRegionWidth,
                blockRegionHeightStart + blockRegionHeight);
        mViewInfo.blockWidth = blockRegionWidth / mGameModel.BOARD_COLS;
        mViewInfo.blockHeight = blockRegionHeight / mGameModel.BOARD_ROWS;
        Log.d(TAG, "globalRect = " + mViewInfo.globalRect);
        Log.d(TAG, "blockRegionRect = " + mViewInfo.blockRegionRect);
        Log.d(TAG, "blockWidth = " + mViewInfo.blockWidth + ", blockHeight = " + mViewInfo.blockHeight);
        mFixedDigitPaint.setTextSize(mViewInfo.blockWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mGameModel.isRun()) {
            showBlocks(canvas);
        }
    }

    private void showBlocks(Canvas canvas) {
        int blocks = mGameModel.BOARD_ROWS * mGameModel.BOARD_COLS;
        for (int r = 0; r <= mGameModel.BOARD_ROWS; ++r) {
            int startX = mViewInfo.blockRegionRect.left;
            int stopX = mViewInfo.blockRegionRect.right;
            int y = mViewInfo.blockRegionRect.top + r * mViewInfo.blockHeight;
            canvas.drawLine(startX, y, stopX, y, mLinePaint);
        }
        for (int c = 0; c <= mGameModel.BOARD_COLS; ++c) {
            int x = mViewInfo.blockRegionRect.left + c * mViewInfo.blockWidth;
            int startY = mViewInfo.blockRegionRect.top;
            int stopY = mViewInfo.blockRegionRect.bottom;
            canvas.drawLine(x, startY, x, stopY, mLinePaint);
        }
        float[] pos = new float[2];
        Rect bounds = new Rect();
        mFixedDigitPaint.getTextBounds("0", 0, 1, bounds);
        float hBorder = (float)(mViewInfo.blockHeight - bounds.height()) / 2;
        char[] text = new char[1];
        for (int r = 0; r < mGameModel.BOARD_ROWS; ++r) {
            for (int c = 0; c < mGameModel.BOARD_COLS; ++c) {
                float x = mViewInfo.blockRegionRect.left + c * mViewInfo.blockWidth + mViewInfo.blockWidth / 2;
                float y = mViewInfo.blockRegionRect.top + r * mViewInfo.blockHeight + mViewInfo.blockHeight - hBorder;
                GameModel.BlockState block = mGameModel.getBlockState(r, c);
                if (block.isFilled && block.isFixed) {
                    text[0] = (char)('0' + block.digit);
                    canvas.drawText(text, 0, 1, x, y, mFixedDigitPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
