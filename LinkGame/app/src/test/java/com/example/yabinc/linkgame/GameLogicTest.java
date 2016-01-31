package com.example.yabinc.linkgame;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by yabinc on 1/23/16.
 */
public class GameLogicTest {
    private Block[][] fillBlocks(int rows, int cols, String map) {
        Block[][] blocks = new Block[rows][cols];
        int t = 0;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                char c = map.charAt(t++);
                if (c == 'x') {
                    blocks[i][j] = new Block(Block.STATE_IMAGE_UNSELECTED, 1);
                } else if (c == 't') {
                    blocks[i][j] = new Block(Block.STATE_IMAGE_SELECTED, 0);
                } else if (c == 'b') {
                    blocks[i][j] = Block.EMPTY;
                }
            }
        }
        return blocks;
    }

    @Test
    public void testCanErase1() {
        String map = "xt" +
                     "tb";
        Block[][] blocks = fillBlocks(2, 2, map);
        GameLogic.LinkPath path = new GameLogic.LinkPath();
        assertFalse(GameLogic.canErase(0, 0, 0, 1, blocks, path));
        assertTrue(GameLogic.canErase(0, 1, 1, 0, blocks, path));
    }

    @Test
    public void testCanErase2() {
        String map = "xt" +
                     "xx" +
                     "tb";
        Block[][] blocks = fillBlocks(3, 2, map);
        GameLogic.LinkPath path = new GameLogic.LinkPath();
        assertTrue(GameLogic.canErase(0, 1, 2, 0, blocks, path));
    }

    @Test
    public void testCanErase3() {
        String map = "xxxt" +
                     "xxxb" +
                     "xxxx" +
                     "xxbb" +
                     "xxxx" +
                     "xxxt";
        Block[][] blocks = fillBlocks(6, 4, map);
        GameLogic.LinkPath path = new GameLogic.LinkPath();
        assertTrue(GameLogic.canErase(0, 3, 5, 3, blocks, path));
        assertEquals(path.pointsR.size(), 4);
        assertArrayEquals(path.pointsR.toArray(new Integer[0]), new Integer[]{5, 5, 0, 0});
        assertArrayEquals(path.pointsC.toArray(new Integer[0]), new Integer[]{3, 4, 4, 3});
        assertTrue(GameLogic.canErase(5, 3, 0, 3, blocks, path));
    }
}
