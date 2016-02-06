package com.example.yabinc.linkgame;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by yabinc on 2/2/16.
 */
public class Setting {
    private static final String FILE_KEY = "PREFERENCE_FILE_KEY";
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SoundInfo mSoundInfo;
    private ScoreInfo mScoreInfo;

    Setting(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE);
    }

    public SizeInfo getSizeInfo() {
        SizeInfo info = new SizeInfo();
        info.useDefaultSize = mSharedPreferences.getBoolean("SizeInfo_useDefaultSize", info.useDefaultSize);
        info.rows = mSharedPreferences.getInt("SizeInfo_rows", info.rows);
        info.cols = mSharedPreferences.getInt("SizeInfo_cols", info.cols);
        return info;
    }

    public void setSizeInfo(SizeInfo info) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("SizeInfo_useDefaultSize", info.useDefaultSize);
        editor.putInt("SizeInfo_rows", info.rows);
        editor.putInt("SizeInfo_cols", info.cols);
        editor.commit();
    }

    public LevelInfo getLevelInfo() {
        LevelInfo info = new LevelInfo();
        info.curLevel = mSharedPreferences.getInt("LevelInfo_curLevel", info.curLevel);
        info.maxAchievedLevel = mSharedPreferences.getInt("LevelInfo_maxAchievedLevel", info.maxAchievedLevel);
        info.maxLevel = mSharedPreferences.getInt("LevelInfo_maxLevel", info.maxLevel);
        return info;
    }

    public void setLevelInfo(LevelInfo info) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("LevelInfo_curLevel", info.curLevel);
        editor.putInt("LevelInfo_maxAchievedLevel", info.maxAchievedLevel);
        editor.putInt("LevelInfo_maxLevel", info.maxLevel);
        editor.commit();
    }

    public SoundInfo getSoundInfo() {
        if (mSoundInfo == null) {
            SoundInfo info = new SoundInfo();
            info.playClickSound = mSharedPreferences.getBoolean("SoundInfo_playClickSound", info.playClickSound);
            info.playBackgroundMusic = mSharedPreferences.getBoolean("SoundInfo_playBackgroundMusic", info.playBackgroundMusic);
            mSoundInfo = info;
        }
        return mSoundInfo;
    }

    public void setSoundInfo(SoundInfo info) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("SoundInfo_playClickSound", info.playClickSound);
        editor.putBoolean("SoundInfo_playBackgroundMusic", info.playBackgroundMusic);
        editor.commit();
        mSoundInfo = info;
    }

    public ScoreInfo getScoreInfo() {
        if (mScoreInfo == null) {
            ScoreInfo info = new ScoreInfo();
            info.playTimes = mSharedPreferences.getLong("ScoreInfo_playTimes", info.playTimes);
            info.maxWinLevelInOneTime = mSharedPreferences.getLong("ScoreInfo_maxWinLevelInOneTime",
                    info.maxWinLevelInOneTime);
            info.totalWinLevel = mSharedPreferences.getLong("ScoreInfo_totalWinLevel",
                    info.totalWinLevel);
            info.curWinLevel = mSharedPreferences.getLong("ScoreInfo_curWinLevel",
                    info.curWinLevel);
            info.maxScoreInOneTime = mSharedPreferences.getLong("ScoreInfo_maxScoreInOneTime",
                    info.maxScoreInOneTime);
            info.totalScore = mSharedPreferences.getLong("ScoreInfo_totalScore", info.totalScore);
            info.curScore = mSharedPreferences.getLong("ScoreInfo_curScore", info.curScore);
            mScoreInfo = info;
        }
        return mScoreInfo;
    }

    public void setScoreInfo(ScoreInfo info) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong("ScoreInfo_playTimes", info.playTimes);
        editor.putLong("ScoreInfo_maxWinLevelInOneTime", info.maxWinLevelInOneTime);
        editor.putLong("ScoreInfo_totalWinLevel", info.totalWinLevel);
        editor.putLong("ScoreInfo_curWinLevel", info.curWinLevel);
        editor.putLong("ScoreInfo_maxScoreInOneTime", info.maxScoreInOneTime);
        editor.putLong("ScoreInfo_totalScore", info.totalScore);
        editor.putLong("ScoreInfo_curScore", info.curScore);
        editor.commit();
        mScoreInfo = info;
    }
}
