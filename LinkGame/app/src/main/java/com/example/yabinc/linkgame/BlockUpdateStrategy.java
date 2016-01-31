package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 1/30/16.
 */
public interface BlockUpdateStrategy {
    public void updateBlocks(Block[][] blocks);
}

    /*
    <item>Level 1 Start Up</item>
    <item>Level 2 Go Up</item>
    <item>Level 3 Go Down</item>
    <item>Level 4 Go Left</item>
    <item>Level 5 Go Right</item>
    <item>Level 6 Up Down Split</item>
    <item>Level 7 Left Right Split</item>
    <item>Level 8 Go Inner</item>
    <item>Level 9 Go Outer</item>
    */

class BlockUpdateStrategyKeep implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
    }
}

class BlockUpdateStrategyGoUp implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        for (int c = 0; c < blocks[0].length; ++c) {
            for (int r = 0; r < blocks.length; ++r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevR = r;
                while (prevR - 1 >= 0 && blocks[prevR - 1][c].isEmpty()) {
                    prevR--;
                }
                if (prevR != r) {
                    blocks[prevR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}

class BlockUpdateStrategyGoDown implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        for (int c = 0; c < blocks[0].length; ++c) {
            for (int r = blocks.length - 1; r >= 0; --r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < blocks.length && blocks[nextR + 1][c].isEmpty()) {
                    nextR++;
                }
                if (nextR != r) {
                    blocks[nextR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}

class BlockUpdateStrategyGoLeft implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= 0 && blocks[r][prevC - 1].isEmpty()) {
                    prevC--;
                }
                if (prevC != c) {
                    blocks[r][prevC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}

class BlockUpdateStrategyGoRight implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        for (int r = 0; r < rows; ++r) {
            for (int c = cols - 1; c >= 0; --c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < cols && blocks[r][nextC + 1].isEmpty()) {
                    nextC++;
                }
                if (nextC != c) {
                    blocks[r][nextC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}

class BlockUpdateStrategyUpDownSplit implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        int halfRows = rows / 2;
        for (int r = 0; r < halfRows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevR = r;
                while (prevR - 1 >= 0 && blocks[prevR - 1][c].isEmpty()) {
                    prevR--;
                }
                if (prevR != r) {
                    blocks[prevR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
        for (int r = rows - 1; r >= halfRows; --r) {
            for (int c = 0; c < cols; ++c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < rows && blocks[nextR + 1][c].isEmpty()) {
                    nextR++;
                }
                if (nextR != r) {
                    blocks[nextR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}

class BlockUpdateStrategyLeftRightSplit implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        int halfCols = cols / 2;
        for (int c = 0; c < halfCols; ++c) {
            for (int r = 0; r < rows; ++r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= 0 && blocks[r][prevC - 1].isEmpty()) {
                    prevC--;
                }
                if (prevC != c) {
                    blocks[r][prevC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
        for (int c = cols - 1; c >= halfCols; --c) {
            for (int r = 0; r < rows; ++r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < cols && blocks[r][nextC + 1].isEmpty()) {
                    nextC++;
                }
                if (nextC != c) {
                    blocks[r][nextC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}

class BlockUpdateStrategyGoInner implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        int halfRows = rows / 2;
        int halfCols = cols / 2;
        for (int k = 0; k < Math.max(halfRows, halfCols) + 2; ++k) {
            int r;
            int c;
            // Top line to down.
            r = Math.max(0, halfRows - k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < halfRows && blocks[nextR + 1][c].isEmpty()) {
                    nextR++;
                }
                if (nextR != r) {
                    blocks[nextR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
            // Bottom line to up.
            r = Math.min(rows - 1, halfRows + k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevR = r;
                while (prevR - 1 >= halfRows && blocks[prevR - 1][c].isEmpty()) {
                    prevR--;
                }
                if (prevR != r) {
                    blocks[prevR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
            // Left line to right.
            c = Math.max(0, halfCols - k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < halfCols && blocks[r][nextC + 1].isEmpty()) {
                    nextC++;
                }
                if (nextC != c) {
                    blocks[r][nextC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
            // Right line to left.
            c = Math.min(cols - 1, halfCols + k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= halfCols && blocks[r][prevC - 1].isEmpty()) {
                    prevC--;
                }
                if (prevC != c) {
                    blocks[r][prevC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}


class BlockUpdateStrategyGoOuter implements BlockUpdateStrategy {
    @Override
    public void updateBlocks(Block[][] blocks) {
        int rows = blocks.length;
        int cols = blocks[0].length;
        int halfRows = rows / 2;
        int halfCols = cols / 2;
        for (int k = Math.max(halfRows, halfCols) + 2; k >= 0; --k) {
            int r;
            int c;
            // Top line to up.
            r = Math.max(0, halfRows - k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevR = r;
                while (prevR -1 >= 0 && blocks[prevR - 1][c].isEmpty()) {
                    prevR--;
                }
                if (prevR != r) {
                    blocks[prevR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
            // Bottom line to down.
            r = Math.min(rows - 1, halfRows + k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < rows && blocks[nextR + 1][c].isEmpty()) {
                    nextR++;
                }
                if (nextR != r) {
                    blocks[nextR][c] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
            // Left line to left.
            c = Math.max(0, halfCols - k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= 0 && blocks[r][prevC - 1].isEmpty()) {
                    prevC--;
                }
                if (prevC != c) {
                    blocks[r][prevC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
            // Right line to right.
            c = Math.min(cols - 1, halfCols + k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (blocks[r][c].isEmpty()) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < cols && blocks[r][nextC + 1].isEmpty()) {
                    nextC++;
                }
                if (nextC != c) {
                    blocks[r][nextC] = blocks[r][c];
                    blocks[r][c] = Block.EMPTY;
                }
            }
        }
    }
}
