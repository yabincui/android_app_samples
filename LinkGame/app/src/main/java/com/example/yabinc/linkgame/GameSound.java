package com.example.yabinc.linkgame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.jar.Attributes;

/**
 * Created by yabinc on 2/5/16.
 */
public class GameSound {
    private Context mContext;
    private SoundPool mSoundPool;
    private int mClickSoundId;
    private MediaPlayer mBackgroundMusicPlayer;

    GameSound(Context context) {
        mContext = context;
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mClickSoundId = mSoundPool.load(mContext, R.raw.click, 1);
        mBackgroundMusicPlayer = MediaPlayer.create(mContext, R.raw.background);
    }

    public void playClickSound() {
        mSoundPool.play(mClickSoundId, 0.5f, 0.5f, 1, 0, 1f);
    }

    public void startBackgroundMusic() {
        mBackgroundMusicPlayer.setLooping(true);
        mBackgroundMusicPlayer.start();
    }

    public void stopBackgroundMusic() {
        mBackgroundMusicPlayer.pause();
    }
}
