package com.example.yabinc.linkgame;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by yabinc on 1/23/16.
 */
public class GameLogicTest {
    private GameLogic.State[][] fillStates(int rows, int cols, String map) {
        GameLogic.State[][] states = new GameLogic.State[rows][cols];
        int t = 0;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                char c = map.charAt(t++);
                if (c == 'x') {
                    states[i][j] = new GameLogic.State(GameLogic.State.IMAGE_UNSELECTED, 1);
                } else if (c == 't') {
                    states[i][j] = new GameLogic.State(GameLogic.State.IMAGE_SELECTED, 0);
                } else if (c == 'b') {
                    states[i][j] = new GameLogic.State(GameLogic.State.EMPTY, -1);
                }
            }
        }
        return states;
    }

    @Test
    public void testCanErase1() {
        String map = "xt" +
                     "tb";
        GameLogic.State[][] states = fillStates(2, 2, map);
        GameLogic.LinkPath path = new GameLogic.LinkPath();
        assertFalse(GameLogic.canErase(0, 0, 0, 1, states, path));
        assertTrue(GameLogic.canErase(0, 1, 1, 0, states, path));
    }

    @Test
    public void testCanErase2() {
        String map = "xt" +
                     "xx" +
                     "tb";
        GameLogic.State[][] states = fillStates(3, 2, map);
        GameLogic.LinkPath path = new GameLogic.LinkPath();
        assertTrue(GameLogic.canErase(0, 1, 2, 0, states, path));
    }

    @Test
    public void testCanErase3() {
        String map = "xxxt" +
                     "xxxb" +
                     "xxxx" +
                     "xxbb" +
                     "xxxx" +
                     "xxxt";
        GameLogic.State[][] states = fillStates(6, 4, map);
        GameLogic.LinkPath path = new GameLogic.LinkPath();
        assertTrue(GameLogic.canErase(0, 3, 5, 3, states, path));
        assertEquals(path.pointsR.size(), 4);
        assertArrayEquals(path.pointsR.toArray(new Integer[0]), new Integer[]{5, 5, 0, 0});
        assertArrayEquals(path.pointsC.toArray(new Integer[0]), new Integer[]{3, 4, 4, 3});
        assertTrue(GameLogic.canErase(5, 3, 0, 3, states, path));
    }
}
