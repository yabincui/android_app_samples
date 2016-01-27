package com.example.yabinc.linkgame;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by yabinc on 1/23/16.
 */
public class GameLogic {
    private static final String LOG_TAG = "GameLogic";
    private static final float ANIMAL_MIN_INCH = 0.2f;
    private static final int MAX_ITEMS = 14 * 14;

    static class State {
        final static int IMAGE_UNSELECTED = 0;
        final static int IMAGE_SELECTED = 1;
        final static int EMPTY = 2;
        int state;
        int imgIndex;

        State(int state, int imgIndex) {
            this.state = state;
            this.imgIndex = imgIndex;
        }
    }

    private static final int DIR_NONE = -1;
    private static final int DIR_UP = 0;
    private static final int DIR_LEFT = 1;
    private static final int DIR_DOWN = 2;
    private static final int DIR_RIGHT = 3;
    private static final int[] dr = new int[]{1, 0, -1, 0};
    private static final int[] dc = new int[]{0, -1, 0, 1};

    static class LinkPath {
        ArrayList<Integer> pointsR = new ArrayList<>();
        ArrayList<Integer> pointsC = new ArrayList<>();

        void add(int r, int c) {
            pointsR.add(r);
            pointsC.add(c);
        }
    }


    public static State[][] initState(int graphWidth, int graphHeight, int imgCount,
                                      int dotsPerInch, State[][] oldStates) {

        int minDot = (int)(dotsPerInch * ANIMAL_MIN_INCH);
        int dot = minDot;
        int cols = -1;
        int rows = -1;
        while (true) {
            cols = graphWidth / dot;
            rows = graphHeight / dot;
            if (rows % 2 != 0 && cols % 2 != 0) {
                rows--;
            }
            if (rows * cols <= MAX_ITEMS) {
                break;
            }
            dot++;
        }
        Log.d(LOG_TAG, "dotsPerInch = " + dotsPerInch + ", minDot = " + minDot + ", dot = " + dot);
        Log.d(LOG_TAG, "w = " + graphWidth + ", h = " + graphHeight + ", r " + rows + ", c " + cols);

        if (oldStates != null) {
            if (oldStates.length == rows && oldStates[0].length == cols) {
                return oldStates;
            }
            if (oldStates.length == cols && oldStates[0].length == rows) {
                // Swap row and col
                State[][] newStates = new State[rows][cols];
                for (int i = 0; i < rows; ++i) {
                    for (int j = 0; j < cols; ++j) {
                        newStates[i][j] = oldStates[j][i];
                    }
                }
                return newStates;
            }
        }
        // Construct new states.
        Random random = new Random();
        int[] indexArray = new int[rows * cols];
        for (int i = 0; i < indexArray.length; i += 2) {
            indexArray[i] = indexArray[i + 1] = random.nextInt(imgCount);
        }
        for (int i = 0; i < indexArray.length; ++i) {
            int j = random.nextInt(indexArray.length);
            int temp = indexArray[i];
            indexArray[i] = indexArray[j];
            indexArray[j] = temp;
        }

        State[][] states = new State[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                states[i][j] = new State(State.IMAGE_UNSELECTED, indexArray[i * cols + j]);
            }
        }
        return states;
    }

    interface Condition {
        public boolean fulfill(int r, int c, State state);
    }

    static class PlaceCondition implements Condition {
        int targetR;
        int targetC;

        PlaceCondition(int r, int c) {
            targetR = r;
            targetC = c;
        }

        public boolean fulfill(int r, int c, State state) {
            return r == targetR && c == targetC;
        }
    }

    // DFS
    public static boolean canErase(int r1, int c1, int r2, int c2, State[][] states, LinkPath path) {
        if (states[r1][c1].imgIndex != states[r2][c2].imgIndex) {
            return false;
        }
        for (int dir = 0; dir < 4; ++dir) {
            if (searchPath(r1, c1, new PlaceCondition(r2, c2), dir, 1, states, path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean searchPath(int srcR, int srcC, Condition condition, int direction,
                                      int depth, State[][] states, LinkPath path) {
        if (depth > 3) {
            return false;
        }
        int rows = states.length;
        int cols = states[0].length;
        int nextDir1 = (direction + 3) % 4;
        int nextDir2 = (direction + 1) % 4;
        int r = srcR;
        int c = srcC;
        boolean found = false;
        while (true) {
            r += dr[direction];
            c += dc[direction];
            if (r < -1 || r > rows || c < -1 || c > cols) {
                break;
            }
            if (r >= 0 && r < rows && c >= 0 && c < cols) {
                if (condition.fulfill(r, c, states[r][c])) {
                    path.add(r, c);
                    found = true;
                    break;
                }
                if (states[r][c].state != State.EMPTY) {
                    break;
                }
            }
            if (searchPath(r, c, condition, nextDir1, depth + 1, states, path)
                    || searchPath(r, c, condition, nextDir2, depth + 1, states, path)) {
                found = true;
                break;
            }
        }
        if (found) {
            path.add(srcR, srcC);
        }
        return found;
    }

    static class ImgIndexCondition implements Condition {
        int srcR;
        int srcC;
        int imgIndex;

        ImgIndexCondition(int srcR, int srcC, int imgIndex) {
            this.srcR = srcR;
            this.srcC = srcC;
            this.imgIndex = imgIndex;
        }

        public boolean fulfill(int r, int c, State state) {
            return (state.state != State.EMPTY && imgIndex == state.imgIndex) && !(r == srcR && c == srcC);
        }
    }

    public static boolean haveErasablePair(State[][] states, LinkPath path) {
        for (int i = 0; i < states.length; ++i) {
            for (int j = 0; j < states[0].length; ++j) {
                if (states[i][j].state != State.EMPTY) {
                    Condition condition = new ImgIndexCondition(i, j, states[i][j].imgIndex);
                    for (int dir = 0; dir < 4; ++dir) {
                        if (searchPath(i, j, condition, dir, 1, states, path)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void shuffleStates(State[][] states) {
        Random random = new Random();
        for (int i = 0; i < states.length; ++i) {
            for (int j = 0; j < states[0].length; ++j) {
                int ti = random.nextInt(states.length);
                int tj = random.nextInt(states[0].length);
                State state = states[i][j];
                states[i][j] = states[ti][tj];
                states[ti][tj] = state;
            }
        }
    }

    public static boolean isSuccess(State[][] states) {
        for (int i = 0; i < states.length; ++i) {
            for (int j = 0; j < states[0].length; ++j) {
                if (states[i][j].state != State.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
}
