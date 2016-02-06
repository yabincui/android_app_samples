package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 2/6/16.
 */
class LevelInfo {
    int curLevel;
    int maxAchievedLevel;
    int maxLevel;

    LevelInfo(int curLevel, int maxAchievedLevel, int maxLevel) {
        this.curLevel = curLevel;
        this.maxAchievedLevel = maxAchievedLevel;
        this.maxLevel = maxLevel;
    }

    LevelInfo() {
        curLevel = -1;
        maxAchievedLevel = -1;
        maxLevel = -1;
    }

    void moveToNextLevel() {
        curLevel = (curLevel + 1) % (maxLevel + 1);
        if (curLevel == 0) {
            curLevel = 1;
        }
        maxAchievedLevel = Math.max(maxAchievedLevel, curLevel);
    }
}
