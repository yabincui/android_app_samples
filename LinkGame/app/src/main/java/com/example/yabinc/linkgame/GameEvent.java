package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 2/6/16.
 */
public class GameEvent {
    public final static int GAME_REDARW = 1;
    public final static int GAME_WIN = 2;
    public final static int GAME_LOSE = 3;
    public final static int GAME_BLOCK_CLICK = 4;
    public final static int GAME_BLOCK_PAIR_ERASE = 5;
    public final static int GAME_START_ONE_LEVEL = 6;

    private static GameEvent redrawEvent = new GameEvent(GAME_REDARW, 0);
    private static GameEvent loseEvent = new GameEvent(GAME_LOSE, 0);
    private static GameEvent blockClickEvent = new GameEvent(GAME_BLOCK_CLICK, 0);
    private static GameEvent blockPairEraseEvent = new GameEvent(GAME_BLOCK_PAIR_ERASE, 0);
    private static GameEvent startOneLevelEvent = new GameEvent(GAME_START_ONE_LEVEL, 0);

    private int event;
    private double winLeftTimePercent;

    protected GameEvent(int event, double winLeftTimePercent) {
        this.event = event;
        this.winLeftTimePercent = winLeftTimePercent;
    }

    public static GameEvent createEvent(int event) {
        if (event == GAME_REDARW) {
            return redrawEvent;
        }
        if (event == GAME_LOSE) {
            return loseEvent;
        }
        if (event == GAME_BLOCK_CLICK) {
            return blockClickEvent;
        }
        if (event == GAME_BLOCK_PAIR_ERASE) {
            return blockPairEraseEvent;
        }
        if (event == GAME_START_ONE_LEVEL) {
            return startOneLevelEvent;
        }
        assert false;
        return null;
    }

    public static GameEvent createWinEvent(double winLeftTimePercent) {
        return new GameEvent(GAME_WIN, winLeftTimePercent);
    }

    public int getEvent() {
        return event;
    }

    public double getWinLeftTimePercent() {
        return winLeftTimePercent;
    }
}
