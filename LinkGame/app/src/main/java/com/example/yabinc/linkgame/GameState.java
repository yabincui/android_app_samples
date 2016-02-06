package com.example.yabinc.linkgame;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yabinc on 1/31/16.
 */
class GameState {
    private static final int STANDARD_BLOCK_COUNT = 12 * 12;
    private static final int INIT_TIME_IN_SEC = 3 * 60;
    private static final int MIN_INIT_TIME_IN_SEC = 10;
    private static final int ERASE_INC_TIME_IN_SEC = 3;

    public static final int GAME_BEFORE_START = 0;
    public static final int GAME_RUN = 1;
    public static final int GAME_SUCCESS = 2;
    public static final int GAME_PAUSE = 3;
    public static final int GAME_LOSE = 4;

    public interface OnStateChangeListener {
        public void onStateChange(GameState state);
        public void onWin(GameState state, double leftTimePercent);
        public void onLose(GameState state);
        public void onBlockClick();
        public void onBlockPairErase();
    }

    private int state = GAME_BEFORE_START;
    private int curLevel;
    private Block[][] blocks;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private ArrayList<Point> linkPath;
    private ArrayList<Point> hintPoints;
    private Timer mTimer;
    private int totalTimeInSec;
    private int leftTimeInSec;

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
        hintPoints = null;
        int blockCount = blocks.length * blocks[0].length;
        totalTimeInSec = (blockCount >= STANDARD_BLOCK_COUNT) ? INIT_TIME_IN_SEC :
                Math.max(MIN_INIT_TIME_IN_SEC, (int)((float)blockCount / STANDARD_BLOCK_COUNT * INIT_TIME_IN_SEC));
        leftTimeInSec = totalTimeInSec;
        checkAndShuffleBlocks();
        startTimer();
        onStateChange();
    }

    private void checkAndShuffleBlocks() {
        GameLogic.updateBlocksByLevel(blocks, curLevel);
        ArrayList<Point> path = new ArrayList<>();
        while (!GameLogic.haveErasablePair(blocks, path)) {
            GameLogic.shuffleBlocks(blocks);
            GameLogic.updateBlocksByLevel(blocks, curLevel);
        }
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

    Block[][] getBlocks() {
        return blocks;
    }

    ArrayList<Point> getLinkPath() {
        return linkPath;
    }

    ArrayList<Point> getHintPoints() {
        return hintPoints;
    }

    void selectBlock(int row, int col) {
        if (blocks[row][col].isUnselected()) {
            blocks[row][col].state = Block.STATE_IMAGE_SELECTED;
            if (selectedRow == -1) {
                selectedRow = row;
                selectedCol = col;
            } else {
                ArrayList<Point> path = new ArrayList<>();
                if (GameLogic.canErase(row, col, selectedRow, selectedCol, blocks, path)) {
                    linkPath = path;
                    selectedRow = -1;
                    selectedCol = -1;
                    hintPoints = null;
                    leftTimeInSec = Math.min(leftTimeInSec + ERASE_INC_TIME_IN_SEC, totalTimeInSec);
                    mHandler.sendEmptyMessageDelayed(MyHandler.MSG_ERASE_LINK_PATH, 200);
                } else {
                    blocks[selectedRow][selectedCol].state = Block.STATE_IMAGE_UNSELECTED;
                    selectedRow = row;
                    selectedCol = col;
                }
            }
            onBlockClick();
            onStateChange();
        } else if (blocks[row][col].state == Block.STATE_IMAGE_SELECTED) {
            blocks[row][col].state = Block.STATE_IMAGE_UNSELECTED;
            selectedRow = -1;
            selectedCol = -1;
            onBlockClick();
            onStateChange();
        }
    }

    private void onBlockClick() {
        if (mListener != null) {
            mListener.onBlockClick();
        }
    }

    private void onBlockPairErase() {
        if (mListener != null) {
            mListener.onBlockPairErase();
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
            mListener.onWin(this, (double)leftTimeInSec / totalTimeInSec);
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
        return (float)leftTimeInSec / totalTimeInSec;
    }

    void giveHint() {
        if (hintPoints == null) {
            ArrayList<Point> path = new ArrayList<>();
            if (GameLogic.haveErasablePair(blocks, path)) {
                hintPoints = new ArrayList<>();
                hintPoints.add(path.get(0));
                hintPoints.add(path.get(path.size() - 1));
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
                    blocks[linkPath.get(0).row][linkPath.get(0).col] = Block.EMPTY;
                    blocks[linkPath.get(linkPath.size()-1).row][linkPath.get(linkPath.size()-1).col] = Block.EMPTY;
                    onBlockPairErase();
                    linkPath = null;
                    if (GameLogic.isSuccess(blocks)) {
                        setSuccess();
                    } else {
                        checkAndShuffleBlocks();
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
