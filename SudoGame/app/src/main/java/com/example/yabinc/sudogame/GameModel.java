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
        boolean isFixed;
        boolean isUserInput;
        boolean isConflictWithOthers;

        BlockState() {
            digit = 0;
            isFixed = false;
            isUserInput = false;
            isConflictWithOthers = false;
        }

        void guess(int digit) {
            if (!isFixed) {
                if (digit == 0) {
                    isUserInput = false;
                } else {
                    isUserInput = true;
                }
                this.digit = digit;
            }
        }

        void clearGuess() {
            if (!isFixed) {
                isUserInput = false;
                digit = 0;
                isConflictWithOthers = false;
            }
        }

        void setIntState(int state) {
            if (state == 0) {
                isFixed = false;
                isUserInput = false;
                digit = 0;
            } else if (state > 0) {
                isFixed = false;
                isUserInput = true;
                digit = state;
            } else {
                isFixed = true;
                isUserInput = false;
                digit = -state;
            }
            isConflictWithOthers = false;
        }

        int getIntState() {
            return isFixed ? -digit : digit;
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
                mBoard[r][c].clearGuess();
            }
        }
        updateConflictMarks();
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
        mBoard[row][col].guess(digit);
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
                mBoard[r][c].setIntState(intBoard[r][c]);
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
                mIntBoard[i][j] = mBoard[i][j].getIntState();
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
                if (mBoard[r][c].digit == 0 || mBoard[r][c].isConflictWithOthers) {
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
                if (mBoard[r][c].isUserInput) {
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
