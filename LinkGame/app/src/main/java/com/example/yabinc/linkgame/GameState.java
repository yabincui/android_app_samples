package com.example.yabinc.linkgame;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yabinc on 1/31/16.
 */

class Block {
    final static int STATE_IMAGE_UNSELECTED = 0;
    final static int STATE_IMAGE_SELECTED = 1;
    final static int STATE_EMPTY = 2;
    final static Block EMPTY = new Block(STATE_EMPTY, -1);

    int state;
    int imgIndex;

    Block(int state, int imgIndex) {
        this.state = state;
        this.imgIndex = imgIndex;
    }

    boolean isEmpty() {
        return state == STATE_EMPTY;
    }
    boolean isUnselected() {
        return state == STATE_IMAGE_UNSELECTED;
    }
    boolean isSelected() {
        return state == STATE_IMAGE_SELECTED;
    }
    boolean isSameImage(Block other) {
        return (state != STATE_EMPTY && other.state != STATE_EMPTY && imgIndex == other.imgIndex);
    }
    boolean isSameImage(int imgIndex) {
        return (state != STATE_EMPTY && imgIndex == this.imgIndex);
    }
}

class GameState {
    private static final int INIT_TIME_IN_SEC = 5 * 60;
    private static final int ERASE_INC_TIME_IN_SEC = 5;

    public static final int GAME_BEFORE_START = 0;
    public static final int GAME_RUN = 1;
    public static final int GAME_SUCCESS = 2;
    public static final int GAME_PAUSE = 3;
    public static final int GAME_LOSE = 4;

    public interface OnStateChangeListener {
        public void onStateChange(GameState state);
        public void onWin(GameState state);
        public void onLose(GameState state);
    }

    int state = GAME_BEFORE_START;
    private int curLevel;
    Block[][] blocks;
    int selectedRow = -1;
    int selectedCol = -1;
    GameLogic.LinkPath linkPath;
    GameLogic.LinkPath hintPath;
    Timer mTimer;
    int leftTimeInSec;

    private final MyHandler mHandler;
    private final OnStateChangeListener mListener;


    GameState(OnStateChangeListener listener) {
        mHandler = new MyHandler();
        mListener = listener;
    }

    void start(Block[][] blocks, int curLevel) {
        this.curLevel = curLevel;
        state = GAME_RUN;
        this.blocks = blocks;
        selectedRow = -1;
        selectedCol = -1;
        linkPath = null;
        hintPath = null;
        leftTimeInSec = INIT_TIME_IN_SEC;
        startTimer();
        onStateChange();
    }

    boolean isBeforeStart() {
        return state == GAME_BEFORE_START;
    }
    boolean isRun() {
        return state == GAME_RUN;
    }
    boolean isSuccess() {
        return state == GAME_SUCCESS;
    }
    boolean isLose() {
        return state == GAME_LOSE;
    }
    boolean isPause() {
        return state == GAME_PAUSE;
    }

    void selectBlock(int row, int col) {
        if (blocks[row][col].isUnselected()) {
            blocks[row][col].state = Block.STATE_IMAGE_SELECTED;
            if (selectedRow == -1) {
                selectedRow = row;
                selectedCol = col;
            } else {
                GameLogic.LinkPath path = new GameLogic.LinkPath();
                if (GameLogic.canErase(row, col, selectedRow, selectedCol, blocks, path)) {
                    linkPath = path;
                    selectedRow = -1;
                    selectedCol = -1;
                    hintPath = null;
                    leftTimeInSec = Math.min(leftTimeInSec + ERASE_INC_TIME_IN_SEC, INIT_TIME_IN_SEC);
                    mHandler.sendEmptyMessageDelayed(MyHandler.MSG_ERASE_LINK_PATH, 200);
                } else {
                    blocks[selectedRow][selectedCol].state = Block.STATE_IMAGE_UNSELECTED;
                    selectedRow = row;
                    selectedCol = col;
                }
            }
            onStateChange();
        } else if (blocks[row][col].state == Block.STATE_IMAGE_SELECTED) {
            blocks[row][col].state = Block.STATE_IMAGE_UNSELECTED;
            selectedRow = -1;
            selectedCol = -1;
            onStateChange();
        }
    }

    private void onStateChange() {
        if (mListener != null) {
            mListener.onStateChange(this);
        }
    }

    private void setSuccess() {
        state = GAME_SUCCESS;
        stopTimer();
        if (mListener != null) {
            mListener.onWin(this);
        }
    }

    private void setLose() {
        state = GAME_LOSE;
        stopTimer();
        if (mListener != null) {
            mListener.onLose(this);
        }
    }

    void pause() {
        state = GAME_PAUSE;
        stopTimer();
        onStateChange();
    }

    void resumeFromPause() {
        state = GAME_RUN;
        startTimer();
        onStateChange();
    }

    float getLeftTimePercent() {
        return (float)leftTimeInSec / INIT_TIME_IN_SEC;
    }

    void giveHint() {
        if (hintPath == null) {
            GameLogic.LinkPath path = new GameLogic.LinkPath();
            if (GameLogic.haveErasablePair(blocks, path)) {
                hintPath = path;
                onStateChange();
            }
        }
    }


    private class MyHandler extends Handler {
        static final int MSG_ERASE_LINK_PATH = 0;
        static final int MSG_UPDATE_TIME = 1;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_ERASE_LINK_PATH) {
                if (linkPath != null) {
                    int len = linkPath.pointsR.size();
                    blocks[linkPath.pointsR.get(0)][linkPath.pointsC.get(0)] = Block.EMPTY;
                    blocks[linkPath.pointsR.get(len-1)][linkPath.pointsC.get(len-1)] = Block.EMPTY;
                    linkPath = null;
                    if (GameLogic.isSuccess(blocks)) {
                        setSuccess();
                    } else {
                        GameLogic.updateBlocksByLevel(blocks, curLevel);
                        GameLogic.LinkPath path = new GameLogic.LinkPath();
                        while (!GameLogic.haveErasablePair(blocks, path)) {
                            GameLogic.shuffleBlocks(blocks);
                            GameLogic.updateBlocksByLevel(blocks, curLevel);
                        }
                    }
                    onStateChange();
                }
            } else if (msg.what == MSG_UPDATE_TIME) {
                if (leftTimeInSec == 0) {
                    setLose();
                } else {
                    leftTimeInSec--;
                }
                onStateChange();
            }
        }
    }

    public void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(MyHandler.MSG_UPDATE_TIME);
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
}
