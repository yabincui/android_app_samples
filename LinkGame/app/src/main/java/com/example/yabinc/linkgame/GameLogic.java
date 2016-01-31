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

    public static final int DEFAULT_ROWS = 14;
    public static final int DEFAULT_COLS = 9;

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

    public static Block[][] createBlocks(int rows, int cols, int imgCount) {
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

        Block[][] blocks = new Block[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                blocks[i][j] = new Block(Block.STATE_IMAGE_UNSELECTED, indexArray[i * cols + j]);
            }
        }
        return blocks;
    }

    public static Block[][] createBlocksByScreen(int graphWidth, int graphHeight, int imgCount,
                                                        int dotsPerInch) {
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
        return createBlocks(rows, cols, imgCount);
    }

    interface Condition {
        public boolean fulfill(int r, int c, Block block);
    }

    static class PlaceCondition implements Condition {
        int targetR;
        int targetC;

        PlaceCondition(int r, int c) {
            targetR = r;
            targetC = c;
        }

        public boolean fulfill(int r, int c, Block block) {
            return r == targetR && c == targetC;
        }
    }

    // DFS
    public static boolean canErase(int r1, int c1, int r2, int c2, Block[][] blocks, LinkPath path) {
        if (!blocks[r1][c1].isSameImage(blocks[r2][c2])) {
            return false;
        }
        for (int dir = 0; dir < 4; ++dir) {
            if (searchPath(r1, c1, new PlaceCondition(r2, c2), dir, 1, blocks, path)) {
                return true;
            }
        }
        return false;
    }

    private static boolean searchPath(int srcR, int srcC, Condition condition, int direction,
                                      int depth, Block[][] blocks, LinkPath path) {
        if (depth > 3) {
            return false;
        }
        int rows = blocks.length;
        int cols = blocks[0].length;
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
                if (condition.fulfill(r, c, blocks[r][c])) {
                    path.add(r, c);
                    found = true;
                    break;
                }
                if (!blocks[r][c].isEmpty()) {
                    break;
                }
            }
            if (searchPath(r, c, condition, nextDir1, depth + 1, blocks, path)
                    || searchPath(r, c, condition, nextDir2, depth + 1, blocks, path)) {
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

        public boolean fulfill(int r, int c, Block block) {
            return block.isSameImage(imgIndex) && !(r == srcR && c == srcC);
        }
    }

    public static boolean haveErasablePair(Block[][] blocks, LinkPath path) {
        for (int i = 0; i < blocks.length; ++i) {
            for (int j = 0; j < blocks[0].length; ++j) {
                if (!blocks[i][j].isEmpty()) {
                    Condition condition = new ImgIndexCondition(i, j, blocks[i][j].imgIndex);
                    for (int dir = 0; dir < 4; ++dir) {
                        if (searchPath(i, j, condition, dir, 1, blocks, path)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void shuffleBlocks(Block[][] blocks) {
        Random random = new Random();
        for (int i = 0; i < blocks.length; ++i) {
            for (int j = 0; j < blocks[0].length; ++j) {
                int ti = random.nextInt(blocks.length);
                int tj = random.nextInt(blocks[0].length);
                Block tmp = blocks[i][j];
                blocks[i][j] = blocks[ti][tj];
                blocks[ti][tj] = tmp;
            }
        }
    }

    public static boolean isSuccess(Block[][] blocks) {
        for (int i = 0; i < blocks.length; ++i) {
            for (int j = 0; j < blocks[0].length; ++j) {
                if (!blocks[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockUpdateStrategy getStrategyByLevel(int level) {
        switch (level) {
            case 1: return new BlockUpdateStrategyKeep();
            case 2: return new BlockUpdateStrategyGoUp();
            case 3: return new BlockUpdateStrategyGoDown();
            case 4: return new BlockUpdateStrategyGoLeft();
            case 5: return new BlockUpdateStrategyGoRight();
            case 6: return new BlockUpdateStrategyUpDownSplit();
            case 7: return new BlockUpdateStrategyLeftRightSplit();
            case 8: return new BlockUpdateStrategyGoInner();
            case 9: return new BlockUpdateStrategyGoOuter();
        }
        return new BlockUpdateStrategyKeep();
    }

    public static void updateBlocksByLevel(Block[][] blocks, int level) {
        BlockUpdateStrategy strategy = getStrategyByLevel(level);
        strategy.updateBlocks(blocks);
    }
}
