package com.example.yabinc.linkgame;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Created by yabinc on 1/31/16.
 */
class GameState {
    private static final String LOG_TAG = "GameState";
    private static final int STANDARD_BLOCK_COUNT = 12 * 12;
    private static final int INIT_TIME_IN_SEC = 3 * 60;
    private static final int MIN_INIT_TIME_IN_SEC = 10;
    private static final int ERASE_INC_TIME_IN_SEC = 3;

    public static final int GAME_BEFORE_START = 0;
    public static final int GAME_RUN = 1;
    public static final int GAME_SUCCESS = 2;
    public static final int GAME_PAUSE = 3;
    public static final int GAME_LOSE = 4;


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

    private final MyHandler mHandler = new MyHandler();
    private final EventBus eventBus = EventBus.getDefault();

    public void start(Block[][] blocks, int curLevel) {
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
    }

    private void checkAndShuffleBlocks() {
        GameLogic.updateBlocksByLevel(blocks, curLevel);
        ArrayList<Point> path = new ArrayList<>();
        while (!GameLogic.haveErasablePair(blocks, path)) {
            GameLogic.shuffleBlocks(blocks);
            GameLogic.updateBlocksByLevel(blocks, curLevel);
        }
    }

    public boolean isBeforeStart() {
        return state == GAME_BEFORE_START;
    }
    public boolean isRun() {
        return state == GAME_RUN;
    }
    public boolean isSuccess() {
        return state == GAME_SUCCESS;
    }
    public boolean isLose() {
        return state == GAME_LOSE;
    }
    public boolean isPause() {
        return state == GAME_PAUSE;
    }

    public Block[][] getBlocks() {
        return blocks;
    }

    public ArrayList<Point> getLinkPath() {
        return linkPath;
    }

    public ArrayList<Point> getHintPoints() {
        return hintPoints;
    }

    public void selectBlock(int row, int col) {
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
            Log.d(LOG_TAG, "before send redraw event");
            eventBus.post(GameEvent.createEvent(GameEvent.GAME_BLOCK_CLICK));
            eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
            Log.d(LOG_TAG, "after send redraw event");
        } else if (blocks[row][col].state == Block.STATE_IMAGE_SELECTED) {
            blocks[row][col].state = Block.STATE_IMAGE_UNSELECTED;
            selectedRow = -1;
            selectedCol = -1;
            eventBus.post(GameEvent.createEvent(GameEvent.GAME_BLOCK_CLICK));
            eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
        }
    }

    private void setSuccess() {
        state = GAME_SUCCESS;
        stopTimer();
        eventBus.post(GameEvent.createWinEvent((double) leftTimeInSec / totalTimeInSec));
        eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
    }

    private void setLose() {
        state = GAME_LOSE;
        stopTimer();
        eventBus.post(GameEvent.createEvent(GameEvent.GAME_LOSE));
        eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
    }

    public void pause() {
        state = GAME_PAUSE;
        stopTimer();
        eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
    }

    public void resumeFromPause() {
        state = GAME_RUN;
        startTimer();
        eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
    }

    public float getLeftTimePercent() {
        return (float)leftTimeInSec / totalTimeInSec;
    }

    public void giveHint() {
        if (hintPoints == null) {
            ArrayList<Point> path = new ArrayList<>();
            if (GameLogic.haveErasablePair(blocks, path)) {
                hintPoints = new ArrayList<>();
                hintPoints.add(path.get(0));
                hintPoints.add(path.get(path.size() - 1));
                eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
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
                    eventBus.post(GameEvent.createEvent(GameEvent.GAME_BLOCK_PAIR_ERASE));
                    linkPath = null;
                    if (GameLogic.isSuccess(blocks)) {
                        setSuccess();
                    } else {
                        checkAndShuffleBlocks();
                    }
                    eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
                }
            } else if (msg.what == MSG_UPDATE_TIME) {
                if (leftTimeInSec == 0) {
                    setLose();
                } else {
                    leftTimeInSec--;
                }
                eventBus.post(GameEvent.createEvent(GameEvent.GAME_REDARW));
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
