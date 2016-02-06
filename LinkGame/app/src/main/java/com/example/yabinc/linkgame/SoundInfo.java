package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 2/6/16.
 */
class SoundInfo {
    boolean playClickSound;
    boolean playBackgroundMusic;

    SoundInfo() {
        playClickSound = true;
        playBackgroundMusic = false;
    }

    SoundInfo(boolean playClickSound, boolean playBackgroundMusic) {
        this.playClickSound = playClickSound;
        this.playBackgroundMusic = playBackgroundMusic;
    }
}
