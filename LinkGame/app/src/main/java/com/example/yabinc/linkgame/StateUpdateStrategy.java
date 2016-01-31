package com.example.yabinc.linkgame;

/**
 * Created by yabinc on 1/30/16.
 */
public interface StateUpdateStrategy {
    public void updateStates(GameLogic.State[][] states);
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

class StateUpdateStrategyKeep implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
    }
}

class StateUpdateStrategyGoUp implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        for (int c = 0; c < states[0].length; ++c) {
            for (int r = 0; r < states.length; ++r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevR = r;
                while (prevR - 1 >= 0 && states[prevR - 1][c].state == GameLogic.State.EMPTY) {
                    prevR--;
                }
                if (prevR != r) {
                    states[prevR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}

class StateUpdateStrategyGoDown implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        for (int c = 0; c < states[0].length; ++c) {
            for (int r = states.length - 1; r >= 0; --r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < states.length && states[nextR + 1][c].state == GameLogic.State.EMPTY) {
                    nextR++;
                }
                if (nextR != r) {
                    states[nextR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}

class StateUpdateStrategyGoLeft implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        int rows = states.length;
        int cols = states[0].length;
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= 0 && states[r][prevC - 1].state == GameLogic.State.EMPTY) {
                    prevC--;
                }
                if (prevC != c) {
                    states[r][prevC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}

class StateUpdateStrategyGoRight implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        int rows = states.length;
        int cols = states[0].length;
        for (int r = 0; r < rows; ++r) {
            for (int c = cols - 1; c >= 0; --c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < cols && states[r][nextC + 1].state == GameLogic.State.EMPTY) {
                    nextC++;
                }
                if (nextC != c) {
                    states[r][nextC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}

class StateUpdateStrategyUpDownSplit implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        int rows = states.length;
        int cols = states[0].length;
        int halfRows = rows / 2;
        for (int r = 0; r < halfRows; ++r) {
            for (int c = 0; c < cols; ++c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevR = r;
                while (prevR - 1 >= 0 && states[prevR - 1][c].state == GameLogic.State.EMPTY) {
                    prevR--;
                }
                if (prevR != r) {
                    states[prevR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
        for (int r = rows - 1; r >= halfRows; --r) {
            for (int c = 0; c < cols; ++c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < rows && states[nextR + 1][c].state == GameLogic.State.EMPTY) {
                    nextR++;
                }
                if (nextR != r) {
                    states[nextR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}

class StateUpdateStrategyLeftRightSplit implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        int rows = states.length;
        int cols = states[0].length;
        int halfCols = cols / 2;
        for (int c = 0; c < halfCols; ++c) {
            for (int r = 0; r < rows; ++r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= 0 && states[r][prevC - 1].state == GameLogic.State.EMPTY) {
                    prevC--;
                }
                if (prevC != c) {
                    states[r][prevC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
        for (int c = cols - 1; c >= halfCols; --c) {
            for (int r = 0; r < rows; ++r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < cols && states[r][nextC + 1].state == GameLogic.State.EMPTY) {
                    nextC++;
                }
                if (nextC != c) {
                    states[r][nextC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}

class StateUpdateStrategyGoInner implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        int rows = states.length;
        int cols = states[0].length;
        int halfRows = rows / 2;
        int halfCols = cols / 2;
        for (int k = 0; k < Math.max(halfRows, halfCols) + 2; ++k) {
            int r;
            int c;
            // Top line to down.
            r = Math.max(0, halfRows - k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < halfRows && states[nextR + 1][c].state == GameLogic.State.EMPTY) {
                    nextR++;
                }
                if (nextR != r) {
                    states[nextR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
            // Bottom line to up.
            r = Math.min(rows - 1, halfRows + k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevR = r;
                while (prevR - 1 >= halfRows && states[prevR - 1][c].state == GameLogic.State.EMPTY) {
                    prevR--;
                }
                if (prevR != r) {
                    states[prevR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
            // Left line to right.
            c = Math.max(0, halfCols - k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < halfCols && states[r][nextC + 1].state == GameLogic.State.EMPTY) {
                    nextC++;
                }
                if (nextC != c) {
                    states[r][nextC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
            // Right line to left.
            c = Math.min(cols - 1, halfCols + k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= halfCols && states[r][prevC - 1].state == GameLogic.State.EMPTY) {
                    prevC--;
                }
                if (prevC != c) {
                    states[r][prevC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}


class StateUpdateStrategyGoOuter implements StateUpdateStrategy {
    @Override
    public void updateStates(GameLogic.State[][] states) {
        int rows = states.length;
        int cols = states[0].length;
        int halfRows = rows / 2;
        int halfCols = cols / 2;
        for (int k = Math.max(halfRows, halfCols) + 2; k >= 0; --k) {
            int r;
            int c;
            // Top line to up.
            r = Math.max(0, halfRows - k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevR = r;
                while (prevR -1 >= 0 && states[prevR - 1][c].state == GameLogic.State.EMPTY) {
                    prevR--;
                }
                if (prevR != r) {
                    states[prevR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
            // Bottom line to down.
            r = Math.min(rows - 1, halfRows + k);
            for (c = Math.max(0, halfCols - k); c <= Math.min(cols - 1, halfCols + k); ++c) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextR = r;
                while (nextR + 1 < rows && states[nextR + 1][c].state == GameLogic.State.EMPTY) {
                    nextR++;
                }
                if (nextR != r) {
                    states[nextR][c] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
            // Left line to left.
            c = Math.max(0, halfCols - k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int prevC = c;
                while (prevC - 1 >= 0 && states[r][prevC - 1].state == GameLogic.State.EMPTY) {
                    prevC--;
                }
                if (prevC != c) {
                    states[r][prevC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
            // Right line to right.
            c = Math.min(cols - 1, halfCols + k);
            for (r = Math.max(0, halfRows - k); r <= Math.min(rows - 1, halfRows + k); ++r) {
                if (states[r][c].state == GameLogic.State.EMPTY) {
                    continue;
                }
                int nextC = c;
                while (nextC + 1 < cols && states[r][nextC + 1].state == GameLogic.State.EMPTY) {
                    nextC++;
                }
                if (nextC != c) {
                    states[r][nextC] = states[r][c];
                    states[r][c] = GameLogic.State.getEmptyState();
                }
            }
        }
    }
}