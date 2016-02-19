package com.example.yabinc.linkgame;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    public static boolean canErase(int r1, int c1, int r2, int c2, Block[][] blocks, ArrayList<Point> path) {
        if (!blocks[r1][c1].isSameImage(blocks[r2][c2])) {
            return false;
        }
        Condition condition = new PlaceCondition(r2, c2);
        return searchPath(r1, c1, condition, blocks, path);
    }

    private static boolean searchPath(int srcR, int srcC, Condition condition,
                                      Block[][] blocks, ArrayList<Point> path) {
        ArrayList<Point> path2 = new ArrayList<Point>();
        ArrayList<Point> path3 = new ArrayList<>();
        boolean result = searchPathByDFS(srcR, srcC, condition, blocks, path2);
        boolean resultDp = searchPathByDP(srcR, srcC, condition, blocks, path3);
        boolean resultDp2 = searchPathByDp2(srcR, srcC, condition, blocks, path);
        if (BuildConfig.DEBUG) {
            if (result != resultDp) {
                throw new AssertionError();
            }
            if (result != resultDp2) {
                throw new AssertionError();
            }
        }
        return result;
    }

    private static boolean searchPathByDFS(int r1, int c1, Condition condition,
                                           Block[][] blocks, ArrayList<Point> path) {
        for (int dir = 0; dir < 4; ++dir) {
            if (searchPathByDFSImpl(r1, c1, condition, dir, 1, blocks, path)) {
                Collections.reverse(path);
                return true;
            }
        }
        return false;
    }

    private static boolean searchPathByDFSImpl(int srcR, int srcC, Condition condition, int direction,
                                      int depth, Block[][] blocks, ArrayList<Point> path) {
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
                    path.add(new Point(r, c));
                    found = true;
                    break;
                }
                if (!blocks[r][c].isEmpty()) {
                    break;
                }
            }
            if (searchPathByDFSImpl(r, c, condition, nextDir1, depth + 1, blocks, path)
                    || searchPathByDFSImpl(r, c, condition, nextDir2, depth + 1, blocks, path)) {
                found = true;
                break;
            }
        }
        if (found) {
            path.add(new Point(srcR, srcC));
        }
        return found;
    }

    private static class Mark {
        int depth;
        int prevR;
        int prevC;

        Mark() {
            depth = Integer.MAX_VALUE;
            prevR = -1;
            prevC = -1;
        }
    }

    private static boolean searchPathByDP(int srcR, int srcC, Condition condition,
                                          Block[][] blocks, ArrayList<Point> path) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        Mark[][] mark = new Mark[rows + 2][cols + 2];
        for (int i = 0; i < rows + 2; ++i) {
            for (int j = 0; j < cols + 2; ++j) {
                mark[i][j] = new Mark();
            }
        }
        mark[srcR + 1][srcC + 1].depth = 0;
        Queue<Point> queue = new ArrayDeque<Point>();
        queue.add(new Point(srcR, srcC));
        boolean found = false;
        for (int depth = 0; depth <= 2 && !queue.isEmpty(); ++depth) {
            Queue<Point> nextQueue = new ArrayDeque<>();
            while (!queue.isEmpty()) {
                Point point = queue.poll();
                int r = point.row;
                int c = point.col;
                if (mark[r + 1][c + 1].depth < depth) {
                    continue;
                }
                for (int dir = 0; dir < 4; ++dir) {
                    int nr = r;
                    int nc = c;
                    while (true) {
                        nr += dr[dir];
                        nc += dc[dir];
                        if (nr < -1 || nr > rows || nc < -1 || nc > cols) {
                            break;
                        }
                        if (mark[nr+1][nc+1].depth < depth + 1) {
                            continue;
                        }

                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                            if (condition.fulfill(nr, nc, blocks[nr][nc])) {
                                found = true;
                                mark[nr+1][nc+1].depth = depth + 1;
                                mark[nr+1][nc+1].prevR = r;
                                mark[nr+1][nc+1].prevC = c;
                                path.add(new Point(nr, nc));
                                break;
                            }
                            if (!blocks[nr][nc].isEmpty()) {
                                break;
                            }
                        }
                        mark[nr+1][nc+1].depth = depth + 1;
                        mark[nr+1][nc+1].prevR = r;
                        mark[nr+1][nc+1].prevC = c;
                        nextQueue.add(new Point(nr, nc));
                    }
                    if (found) {
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (found) {
                break;
            }
            queue = nextQueue;
        }
        if (found) {
            for (int i = 0; i < rows + 2; ++i) {
                for (int j = 0; j < cols + 2; ++j) {
                    //System.out.printf("mark[%d][%d] = (%d,%d), %d\n", i, j, mark[i][j].prevR, mark[i][j].prevC,
                    //        mark[i][j].depth);
                }
            }

            int lastR = path.get(0).row;
            int lastC = path.get(0).col;
            while (mark[lastR + 1][lastC + 1].depth != 0) {
                Mark m = mark[lastR + 1][lastC + 1];
                lastR = m.prevR;
                lastC = m.prevC;
                path.add(new Point(lastR, lastC));
            }
            Collections.reverse(path);
        }
        return found;
    }

    private static class Mark2 {
        int r;
        int c;
        int depth;
        int direction;
        Mark2 prev;

        Mark2(int r, int c, int depth, int direction, Mark2 prev) {
            this.r = r;
            this.c = c;
            this.depth = depth;
            this.direction = direction;
            this.prev = prev;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Mark2)) {
                return false;
            }
            Mark2 other = (Mark2)obj;
            return (r == other.r && c == other.c && depth == other.depth &&
                    direction == other.direction);
        }

        @Override
        public int hashCode() {
            return (r << 24) | (c << 16) | (depth << 8) | direction;
        }
    }

    // O(M*N)
    private static boolean searchPathByDp2(int srcR, int srcC, Condition condition,
                                           Block[][] blocks, ArrayList<Point> path) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        HashSet<Mark2> visited = new HashSet<Mark2>();
        Queue<Mark2> queue = new ArrayDeque<>();
        for (int dir = 0; dir < 4; ++dir) {
            Mark2 mark = new Mark2(srcR, srcC, 0, dir, null);
            queue.add(mark);
            visited.add(mark);
        }
        Mark2 lastMark = null;
        while (!queue.isEmpty() && lastMark == null) {
            Mark2 mark = queue.poll();
            for (int dir = 0; dir < 4; ++dir) {
                int nr = mark.r + dr[dir];
                int nc = mark.c + dc[dir];
                if (nr < -1 || nr > rows || nc < -1 || nc > cols) {
                    continue;
                }
                int ndepth = (dir == mark.direction) ? mark.depth : (mark.depth + 1);
                if (ndepth > 2) {
                    continue;
                }
                Mark2 nmark = new Mark2(nr, nc, ndepth, dir, mark);
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    if (condition.fulfill(nr, nc, blocks[nr][nc])) {
                        lastMark = nmark;
                        break;
                    } else if (!blocks[nr][nc].isEmpty()) {
                        continue;
                    }
                }
                if (visited.contains(nmark)) {
                    continue;
                }
                visited.add(nmark);
                queue.add(nmark);
            }
        }
        if (lastMark == null) {
            return false;
        }
        while (lastMark.prev != null) {
            int lastR = lastMark.r;
            int lastC = lastMark.c;
            int lastDepth = lastMark.depth;
            path.add(new Point(lastR, lastC));
            while (lastMark.depth == lastDepth && lastMark.prev != null) {
                lastMark = lastMark.prev;
            }
        }
        path.add(new Point(srcR, srcC));
        Collections.reverse(path);
        return true;
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

    public static boolean haveErasablePair(Block[][] blocks, ArrayList<Point> path) {
        for (int i = 0; i < blocks.length; ++i) {
            for (int j = 0; j < blocks[0].length; ++j) {
                if (!blocks[i][j].isEmpty()) {
                    Condition condition = new ImgIndexCondition(i, j, blocks[i][j].imgIndex);
                    if (searchPath(i, j, condition, blocks, path)) {
                        return true;
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
