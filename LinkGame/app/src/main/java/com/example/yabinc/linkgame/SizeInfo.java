package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 2/6/16.
 */
class SizeInfo {
    boolean useDefaultSize;
    int rows;
    int cols;
    SizeInfo(boolean useDefaultSize, int rows, int cols) {
        this.useDefaultSize = useDefaultSize;
        this.rows = rows;
        this.cols = cols;
    }
    SizeInfo() {
        useDefaultSize = true;
        rows = -1;
        cols = -1;
    }
}