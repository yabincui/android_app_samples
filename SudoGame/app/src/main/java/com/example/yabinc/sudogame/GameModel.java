package com.example.yabinc.sudogame;

/**
 * Created by yabinc on 6/27/16.
 */
public class GameModel {
    static final int BOARD_ROWS = 9;
    static final int BOARD_COLS = 9;

    private static final int GAME_RUN = 1;
    private static final int GAME_SUCCESS = 2;
    private static final int GAME_LOSE = 3;

    private int state;

    class BlockState {
        int digit;
        boolean isFilled;
        boolean isFixed;

        BlockState() {
            digit = 0;
            isFilled = false;
            isFixed = false;
        }
    }
    private BlockState[][] board;

    GameModel() {
        init();
    }

    private void init() {
        board = new BlockState[BOARD_ROWS][BOARD_COLS];
        int k = 0;
        for (int i = 0; i < BOARD_ROWS; ++i) {
            for (int j = 0; j < BOARD_COLS; ++j) {
                board[i][j] = new BlockState();
                board[i][j].digit = k % 10;
                k++;
                board[i][j].isFilled = true;
                board[i][j].isFixed = true;
            }
        }
        state = GAME_RUN;
    }

    // row is in [0, BOARD_ROWS), col is in [0, BOARD_COLS).
    BlockState getBlockState(int row, int col) {
        if (row >= 0 && row < BOARD_ROWS && col >= 0 && col < BOARD_COLS) {
            return board[row][col];
        }
        return null;
    }

    boolean isRun() { return state == GAME_RUN; }
    boolean isSuccess() { return state == GAME_SUCCESS; }
    boolean isLose() { return state == GAME_LOSE; }
}
