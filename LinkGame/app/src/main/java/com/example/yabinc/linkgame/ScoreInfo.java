package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 2/6/16.
 */
class ScoreInfo {
    static final long SCORE_INC_FOR_ERASE_ONE_PAIR = 2;
    static final long SCORE_INC_FOR_WIN_ONE_LEVEL = 100;
    static final long SCORE_MUL_FOR_CUR_LEVEL = 5;
    static final long SCORE_MUL_FOR_CONTINUOUS_WIN = 10;

    long playTimes;
    long maxWinLevelInOneTime;
    long totalWinLevel;
    long curWinLevel;
    long maxScoreInOneTime;
    long totalScore;
    long curScore;

    ScoreInfo() {
        playTimes = 1;
        maxWinLevelInOneTime = 0;
        totalWinLevel = 0;
        curWinLevel = 0;
        maxScoreInOneTime = 0;
        totalScore = 0;
        curScore = 0;
    }

    private void addScore(long score) {
        totalScore += score;
        curScore += score;
        if (curScore > maxScoreInOneTime) {
            maxScoreInOneTime = curScore;
        }
    }

    void addScoreForEraseOnePair() {
        addScore(SCORE_INC_FOR_ERASE_ONE_PAIR);
    }

    void addScoreForWinOneLevel(int curLevel, double lastTimePercent) {
        curWinLevel++;
        totalWinLevel++;
        if (curWinLevel > maxWinLevelInOneTime) {
            maxWinLevelInOneTime = curWinLevel;
        }
        long add = (long)(SCORE_INC_FOR_WIN_ONE_LEVEL * lastTimePercent) +
                SCORE_MUL_FOR_CUR_LEVEL * curLevel + SCORE_MUL_FOR_CONTINUOUS_WIN * curWinLevel;
        addScore(add);
    }

    void lose() {
        curScore = 0;
        curWinLevel = 0;
        playTimes++;
    }
}
