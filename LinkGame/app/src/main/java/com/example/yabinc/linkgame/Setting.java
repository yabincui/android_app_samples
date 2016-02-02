package com.example.yabinc.linkgame;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yabinc on 2/2/16.
 */
public class Setting {
    private static final String FILE_KEY = "PREFERENCE_FILE_KEY";
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    Setting(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(FILE_KEY, Context.MODE_PRIVATE);
    }

    public SizeInfo getSizeInfo() {
        SizeInfo info = new SizeInfo();
        info.useDefaultSize = mSharedPreferences.getBoolean("SizeInfo_useDefaultSize", true);
        info.rows = mSharedPreferences.getInt("SizeInfo_rows", -1);
        info.cols = mSharedPreferences.getInt("SizeInfo_cols", -1);
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
        info.curLevel = mSharedPreferences.getInt("LevelInfo_curLevel", -1);
        info.maxAchievedLevel = mSharedPreferences.getInt("LevelInfo_maxAchievedLevel", -1);
        info.maxLevel = mSharedPreferences.getInt("LevelInfo_maxLevel", -1);
        return info;
    }

    public void setLevelInfo(LevelInfo info) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("LevelInfo_curLevel", info.curLevel);
        editor.putInt("LevelInfo_maxAchievedLevel", info.maxAchievedLevel);
        editor.putInt("LevelInfo_maxLevel", info.maxLevel);
        editor.commit();
    }
}
