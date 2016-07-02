package com.example.yabinc.sudogame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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
        Rect digitRegionRect;
        int digitWidth;
        int digitHeight;
    }

    private ViewInfo mViewInfo;

    private Paint mLinePaint;
    private Paint mGrayLinePaint;
    private Paint mSelectedLinePaint;
    private Paint mHintLinePaint;
    private Paint mFixedDigitPaint;
    private Paint mUserFixedDigitPaint;
    private Paint mGuessDigitPaint;
    private Paint mWrongGuessDigitPaint;
    private Paint mDigitSelectPaint;

    private GestureDetector mDetector;

    private Point mSelectedBlock;
    private Point mHintBlock;

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

        mGrayLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGrayLinePaint.setStyle(Paint.Style.STROKE);
        mGrayLinePaint.setColor(Color.GRAY);
        mGrayLinePaint.setStrokeWidth(10);

        mSelectedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectedLinePaint.setStyle(Paint.Style.STROKE);
        mSelectedLinePaint.setColor(Color.BLUE);
        mSelectedLinePaint.setStrokeWidth(10);

        mHintLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHintLinePaint.setStyle(Paint.Style.STROKE);
        mHintLinePaint.setColor(Color.YELLOW);
        mHintLinePaint.setStrokeWidth(10);

        mFixedDigitPaint = new Paint();
        mFixedDigitPaint.setStyle(Paint.Style.FILL);
        mFixedDigitPaint.setColor(Color.GRAY);
        mFixedDigitPaint.setTextAlign(Paint.Align.CENTER);

        mUserFixedDigitPaint = new Paint();
        mUserFixedDigitPaint.setStyle(Paint.Style.FILL);
        mUserFixedDigitPaint.setColor(Color.BLACK);
        mUserFixedDigitPaint.setTextAlign(Paint.Align.CENTER);

        mGuessDigitPaint = new Paint();
        mGuessDigitPaint.setStyle(Paint.Style.FILL);
        mGuessDigitPaint.setColor(Color.GREEN);
        mGuessDigitPaint.setTextAlign(Paint.Align.CENTER);

        mWrongGuessDigitPaint = new Paint();
        mWrongGuessDigitPaint.setStyle(Paint.Style.FILL);
        mWrongGuessDigitPaint.setColor(Color.RED);
        mWrongGuessDigitPaint.setTextAlign(Paint.Align.CENTER);

        mDigitSelectPaint = new Paint();
        mDigitSelectPaint.setStyle(Paint.Style.FILL);
        mDigitSelectPaint.setColor(Color.BLUE);
        mDigitSelectPaint.setTextAlign(Paint.Align.CENTER);

        mDetector = new GestureDetector(getContext(), new MyGestureListener());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewInfo.globalRect = new Rect((int)(0.05 * w), (int)(0.05 * h), (int)(0.95 * w), (int)(0.95 * h));
        if (w > h) {
            mViewInfo.blockRegionRect = new Rect(mViewInfo.globalRect.left, mViewInfo.globalRect.top,
                    (int)(mViewInfo.globalRect.left + mViewInfo.globalRect.width() * 0.75),
                    mViewInfo.globalRect.bottom);
            mViewInfo.digitRegionRect = new Rect(mViewInfo.blockRegionRect.right, mViewInfo.globalRect.top,
                    mViewInfo.globalRect.right, mViewInfo.globalRect.bottom);
        } else {
            mViewInfo.blockRegionRect = new Rect(mViewInfo.globalRect.left, mViewInfo.globalRect.top,
                    mViewInfo.globalRect.right, (int)(mViewInfo.globalRect.top + mViewInfo.globalRect.height() * 0.75));
            mViewInfo.digitRegionRect = new Rect(mViewInfo.globalRect.left, mViewInfo.blockRegionRect.bottom,
                    mViewInfo.globalRect.right, mViewInfo.globalRect.bottom);
        }
        int blockBorder = Math.min(mViewInfo.blockRegionRect.width(), mViewInfo.blockRegionRect.height());
        int blockRegionLeft = mViewInfo.blockRegionRect.left +
                (mViewInfo.blockRegionRect.width() - blockBorder) / 2;
        int blockRegionTop = mViewInfo.blockRegionRect.top +
                (mViewInfo.blockRegionRect.height() - blockBorder) / 2;
        mViewInfo.blockRegionRect = new Rect(blockRegionLeft, blockRegionTop,
                blockRegionLeft + blockBorder, blockRegionTop + blockBorder);
        mViewInfo.blockWidth = blockBorder / mGameModel.BOARD_COLS;
        mViewInfo.blockHeight = blockBorder / mGameModel.BOARD_ROWS;
        Log.d(TAG, "globalRect = " + mViewInfo.globalRect);
        Log.d(TAG, "blockRegionRect = " + mViewInfo.blockRegionRect);
        Log.d(TAG, "blockWidth = " + mViewInfo.blockWidth + ", blockHeight = " + mViewInfo.blockHeight);
        mFixedDigitPaint.setTextSize(mViewInfo.blockWidth);
        mUserFixedDigitPaint.setTextSize(mViewInfo.blockWidth);
        mGuessDigitPaint.setTextSize(mViewInfo.blockWidth);
        mWrongGuessDigitPaint.setTextSize(mViewInfo.blockWidth);

        int digitBorder = Math.min(mViewInfo.digitRegionRect.width(), mViewInfo.digitRegionRect.height());
        int digitRegionLeft = mViewInfo.digitRegionRect.left +
                (mViewInfo.digitRegionRect.width() - digitBorder) / 2;
        int digitRegionTop = mViewInfo.digitRegionRect.top +
                (mViewInfo.digitRegionRect.height() - digitBorder) / 2;
        mViewInfo.digitRegionRect = new Rect(digitRegionLeft, digitRegionTop,
                digitRegionLeft + digitBorder, digitRegionTop + digitBorder);
        mViewInfo.digitWidth = mViewInfo.digitRegionRect.width() / 3;
        mViewInfo.digitHeight = mViewInfo.digitRegionRect.height() / 3;
        mDigitSelectPaint.setTextSize(mViewInfo.digitWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mGameModel.isRun()) {
            showBlocks(canvas);
        } else if (mGameModel.isSuccess()) {
            canvas.drawText("You Win!", mViewInfo.globalRect.centerX(), mViewInfo.globalRect.centerY(), mFixedDigitPaint);
        }
    }

    private void showBlocks(Canvas canvas) {
        int blocks = mGameModel.BOARD_ROWS * mGameModel.BOARD_COLS;
        for (int r = 0; r <= mGameModel.BOARD_ROWS; ++r) {
            int startX = mViewInfo.blockRegionRect.left;
            int stopX = mViewInfo.blockRegionRect.right;
            int y = mViewInfo.blockRegionRect.top + r * mViewInfo.blockHeight;
            if (r % 3 == 0) {
                canvas.drawLine(startX, y, stopX, y, mLinePaint);
            } else {
                canvas.drawLine(startX, y, stopX, y, mGrayLinePaint);
            }
        }
        for (int c = 0; c <= mGameModel.BOARD_COLS; ++c) {
            int x = mViewInfo.blockRegionRect.left + c * mViewInfo.blockWidth;
            int startY = mViewInfo.blockRegionRect.top;
            int stopY = mViewInfo.blockRegionRect.bottom;
            if (c % 3 == 0) {
                canvas.drawLine(x, startY, x, stopY, mLinePaint);
            } else {
                canvas.drawLine(x, startY, x, stopY, mGrayLinePaint);
            }
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
                text[0] = (char) ('0' + block.digit);
                if (block.digit != 0) {
                    if (block.isFixed) {
                        if (block.isUserInput) {
                            canvas.drawText(text, 0, 1, x, y, mUserFixedDigitPaint);
                        } else {
                            canvas.drawText(text, 0, 1, x, y, mFixedDigitPaint);
                        }
                    } else if (block.isConflictWithOthers) {
                        canvas.drawText(text, 0, 1, x, y, mWrongGuessDigitPaint);
                    } else {
                        canvas.drawText(text, 0, 1, x, y, mGuessDigitPaint);
                    }
                }
            }
        }

        if (mSelectedBlock != null) {
            float x = mViewInfo.blockRegionRect.left + mSelectedBlock.x * mViewInfo.blockWidth;
            float y = mViewInfo.blockRegionRect.top + mSelectedBlock.y * mViewInfo.blockHeight;
            canvas.drawRect(x, y, x + mViewInfo.blockWidth, y + mViewInfo.blockHeight, mSelectedLinePaint);
        }
        if (mHintBlock != null) {
            float x = mViewInfo.blockRegionRect.left + mHintBlock.x * mViewInfo.blockWidth;
            float y = mViewInfo.blockRegionRect.top + mHintBlock.y * mViewInfo.blockHeight;
            canvas.drawRect(x, y, x + mViewInfo.blockWidth, y + mViewInfo.blockHeight, mHintLinePaint);
        }

        for (int r = 0; r <= 3; ++r) {
            int startX = mViewInfo.digitRegionRect.left;
            int stopX = mViewInfo.digitRegionRect.right;
            int y = mViewInfo.digitRegionRect.top + r * mViewInfo.digitHeight;
            canvas.drawLine(startX, y, stopX, y, mLinePaint);
        }
        for (int c = 0; c <= 3; ++c) {
            int x = mViewInfo.digitRegionRect.left + c * mViewInfo.digitWidth;
            int startY = mViewInfo.digitRegionRect.top;
            int stopY = mViewInfo.digitRegionRect.bottom;
            canvas.drawLine(x, startY, x, stopY, mLinePaint);
        }
        mDigitSelectPaint.getTextBounds("0", 0, 1, bounds);
        hBorder = (float)(mViewInfo.digitHeight - bounds.height()) / 2;
        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 3; ++c) {
                float x = mViewInfo.digitRegionRect.left + c * mViewInfo.digitWidth + mViewInfo.digitWidth / 2;
                float y = mViewInfo.digitRegionRect.top + r * mViewInfo.digitHeight + mViewInfo.digitHeight - hBorder;
                text[0] = (char)(r * 3 + c + 1 + '0');
                canvas.drawText(text, 0, 1, x, y, mDigitSelectPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            tapPos(e.getX(), e.getY());
            return true;
        }
    }

    private void tapPos(float x, float y) {
        mHintBlock = null;
        if (mGameModel.isRun()) {
            if (x >= mViewInfo.blockRegionRect.left && x <= mViewInfo.blockRegionRect.right &&
                    y >= mViewInfo.blockRegionRect.top && y <= mViewInfo.blockRegionRect.bottom) {
                int selRow = (int) ((y - mViewInfo.blockRegionRect.top) / mViewInfo.blockHeight);
                int selCol = (int) ((x - mViewInfo.blockRegionRect.left) / mViewInfo.blockWidth);
                Log.d(TAG, "selRow = " + selRow + ", selCol = " + selCol);
                if (selRow < 0 || selRow >= mGameModel.BOARD_ROWS || selCol < 0 ||
                        selCol >= mGameModel.BOARD_COLS) {
                    return;
                }
                if (mGameModel.getBlockState(selRow, selCol).isFixed == false) {
                    if (mSelectedBlock != null && mSelectedBlock.x == selCol && mSelectedBlock.y == selRow) {
                        mGameModel.guessBlockDigit(selRow, selCol, 0);
                    }
                    mSelectedBlock = new Point(selCol, selRow);
                }
                invalidate();
            } else if (x >= mViewInfo.digitRegionRect.left && x <= mViewInfo.digitRegionRect.right &&
                    y >= mViewInfo.digitRegionRect.top && y <= mViewInfo.digitRegionRect.bottom) {
                int selRow = (int) ((y - mViewInfo.digitRegionRect.top) / mViewInfo.digitHeight);
                int selCol = (int) ((x - mViewInfo.digitRegionRect.left) / mViewInfo.digitWidth);
                if (selRow < 0 || selRow >= 3 || selCol < 0 || selCol >= 3) {
                    return;
                }
                int digit = selRow * 3 + selCol + 1;
                if (mSelectedBlock != null) {
                    mGameModel.guessBlockDigit(mSelectedBlock.y, mSelectedBlock.x, digit);
                    invalidate();
                }
            }
        } else if (mGameModel.isSuccess()) {
            mGameModel.reInit();
            invalidate();
        }
    }

    public void startNewGame() {
        mGameModel.reInit();
        mSelectedBlock = null;
        mHintBlock = null;
        invalidate();
    }

    public void restartCurrentGame() {
        mGameModel.clearGuess();
        mSelectedBlock = null;
        mHintBlock = null;
        invalidate();
    }

    public void hint() {
        int[] pos = mGameModel.getOneReasonablePosition();
        if (pos == null) {
            Toast.makeText(getContext(), "Sorry, no hint available, you need to guess!",
                    Toast.LENGTH_LONG).show();
        } else {
            mHintBlock = new Point(pos[1], pos[0]);
            invalidate();
        }
    }

    public void markFix() {
        mGameModel.markFix();
        invalidate();
    }
}
