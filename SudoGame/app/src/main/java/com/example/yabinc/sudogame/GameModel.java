package com.example.yabinc.sudogame;

import android.util.Log;

/**
 * Created by yabinc on 6/27/16.
 */
public class GameModel {
    private static final String TAG = "GameModel";
    static final int BOARD_ROWS = 9;
    static final int BOARD_COLS = 9;

    private static final int GAME_BEFORE_START = 0;
    private static final int GAME_RUN = 1;
    private static final int GAME_SUCCESS = 2;
    private static final int GAME_LOSE = 3;

    private int mState;
    private int mFixCount;
    private boolean mReasonable;

    class BlockState {
        int digit;
        boolean isFilled;
        boolean isFixed;
        boolean isUserInput;
        boolean isConflictWithOthers;

        BlockState() {
            digit = 0;
            isFilled = false;
            isFixed = false;
            isUserInput = false;
            isConflictWithOthers = false;
        }
    }
    private BlockState[][] mBoard;
    private int[][] mIntBoard;  // mIntBoard is a representation of board used to communicate with jni code.

    GameModel() {
        mState = GAME_BEFORE_START;
        mFixCount = 30;
        mReasonable = false;
        mBoard = new BlockState[BOARD_ROWS][BOARD_COLS];
        for (int r = 0; r < BOARD_ROWS; ++r) {
            for (int c = 0; c < BOARD_COLS; ++c) {
                mBoard[r][c] = new BlockState();
            }
        }
        reInit();
    }

    public void reInit() {
        Log.d(TAG, "randomInitState(" + mFixCount + ", " + mReasonable + ") = " + randomInitState(mFixCount, mReasonable));
        Log.d(TAG, "canFindSolution = " + canFindSolution(getIntBoard()));
        updateConflictMarks();
        mState = GAME_RUN;
    }

    public void clearGuess() {
        for (int r = 0; r < BOARD_ROWS; ++r) {
            for (int c = 0; c < BOARD_COLS; ++c) {
                if (mBoard[r][c].isFilled) {
                    if (!mBoard[r][c].isFixed) {
                        mBoard[r][c].digit = 0;
                        mBoard[r][c].isFilled = false;
                    }
                    mBoard[r][c].isConflictWithOthers = false;
                }
            }
        }
    }

    // row is in [0, BOARD_ROWS), col is in [0, BOARD_COLS).
    BlockState getBlockState(int row, int col) {
        if (row >= 0 && row < BOARD_ROWS && col >= 0 && col < BOARD_COLS) {
            return mBoard[row][col];
        }
        return null;
    }

    void guessBlockDigit(int row, int col, int digit) {
        if (mBoard[row][col].isFixed) {
            return;
        }
        mBoard[row][col].isFilled = (digit == 0 ? false : true);
        mBoard[row][col].isUserInput = (digit == 0 ? false : true);
        mBoard[row][col].digit = digit;
        updateConflictMarks();
        checkSuccess();
    }

    boolean isRun() { return mState == GAME_RUN; }
    boolean isSuccess() { return mState == GAME_SUCCESS; }
    boolean isLose() { return mState == GAME_LOSE; }

    private boolean randomInitState(int fixedCount, boolean isSolutionReasonable) {
        int[][] intBoard = initRandomBoard(fixedCount, isSolutionReasonable);
        if (intBoard == null) {
            return false;
        }
        for (int r = 0; r < BOARD_ROWS; ++r) {
            for (int c = 0; c < BOARD_COLS; ++c) {
                BlockState block = mBoard[r][c];
                block.digit = 0;
                block.isFixed = false;
                block.isFilled = false;
                block.isConflictWithOthers = false;
                Log.d(TAG, "block[" + r + "][" + c + "] = " + intBoard[r][c]);
                if (intBoard[r][c] < 0) {
                    block.isFixed = true;
                    block.isFilled = true;
                    block.digit = - intBoard[r][c];
                }
            }
        }
        return true;
    }

    private int[][] getIntBoard() {
        if (mIntBoard == null) {
            mIntBoard = new int[BOARD_ROWS][BOARD_COLS];
        }
        for (int i = 0; i < BOARD_ROWS; ++i) {
            for (int j = 0; j < BOARD_COLS; ++j) {
                mIntBoard[i][j] = (mBoard[i][j].isFixed ? mBoard[i][j].digit : -mBoard[i][j].digit);
            }
        }
        Log.d(TAG, "mIntBoard[0][1] = " + mIntBoard[0][1]);
        return mIntBoard;
    }

    private void updateConflictMarks() {
        for (int r = 0; r < BOARD_ROWS; ++r) {
            for (int c = 0; c < BOARD_COLS; ++c) {
                mBoard[r][c].isConflictWithOthers = false;
            }
        }
        int[][] intBoard = getIntBoard();
        int[] conflictPairs = findConflictPairs(intBoard);
        for (int i = 0; i < conflictPairs.length;) {
            int r1 = conflictPairs[i++];
            int c1 = conflictPairs[i++];
            int r2 = conflictPairs[i++];
            int c2 = conflictPairs[i++];
            mBoard[r1][c1].isConflictWithOthers = true;
            mBoard[r2][c2].isConflictWithOthers = true;
        }
    }

    private void checkSuccess() {
        for (int r = 0; r < BOARD_ROWS; ++r) {
            for (int c = 0; c < BOARD_COLS; ++c) {
                if (!mBoard[r][c].isFilled || mBoard[r][c].isConflictWithOthers) {
                    return;
                }
             }
        }
        mState = GAME_SUCCESS;
        // If success, change everything to fixed.
        for (int r = 0; r < BOARD_ROWS; ++r) {
            for (int c = 0; c < BOARD_COLS; ++c) {
                mBoard[r][c].isFixed = true;
            }
        }
    }

    public int[] getOneReasonablePosition() {
        return getOneReasonablePosition(getIntBoard());
    }

    public int getFixCount() {
        return mFixCount;
    }

    public void setFixCount(int fixCount) {
        mFixCount = fixCount;
    }

    public boolean getReasonable() {
        return mReasonable;
    }

    public void setReasonable(boolean reasonable) {
        mReasonable = reasonable;
    }

    public void markFix() {
        for (int r = 0; r < BOARD_ROWS; ++r) {
            for (int c = 0; c < BOARD_COLS; ++c) {
                if (mBoard[r][c].isUserInput && mBoard[r][c].isFilled) {
                    if (!mBoard[r][c].isConflictWithOthers) {
                        mBoard[r][c].isFixed = true;
                    }
                }
            }
        }
    }

    private native int[][] initRandomBoard(int fixedCount, boolean isSolutionReasonable);
    private native boolean canFindSolution(int[][] board);
    private native int[] findConflictPairs(int[][] board);
    private native int[] getOneReasonablePosition(int[][] board);

    static {
        System.loadLibrary("sudo-game-jni");
    }
}
