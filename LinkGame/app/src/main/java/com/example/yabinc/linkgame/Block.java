package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 2/6/16.
 */

class Block {
    final static int STATE_IMAGE_UNSELECTED = 0;
    final static int STATE_IMAGE_SELECTED = 1;
    final static int STATE_EMPTY = 2;
    final static Block EMPTY = new Block(STATE_EMPTY, -1);

    int state;
    int imgIndex;

    Block(int state, int imgIndex) {
        this.state = state;
        this.imgIndex = imgIndex;
    }

    boolean isEmpty() {
        return state == STATE_EMPTY;
    }
    boolean isUnselected() {
        return state == STATE_IMAGE_UNSELECTED;
    }
    boolean isSelected() {
        return state == STATE_IMAGE_SELECTED;
    }
    boolean isSameImage(Block other) {
        return (state != STATE_EMPTY && other.state != STATE_EMPTY && imgIndex == other.imgIndex);
    }
    boolean isSameImage(int imgIndex) {
        return (state != STATE_EMPTY && imgIndex == this.imgIndex);
    }
}
