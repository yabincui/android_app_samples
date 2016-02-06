package com.example.yabinc.linkgame;

import org.junit.Test;

import java.util.ArrayList;

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
        ArrayList<Point> path = new ArrayList<>();
        assertFalse(GameLogic.canErase(0, 0, 0, 1, blocks, path));
        assertTrue(GameLogic.canErase(0, 1, 1, 0, blocks, path));
    }

    @Test
    public void testCanErase2() {
        String map = "xt" +
                     "xx" +
                     "tb";
        Block[][] blocks = fillBlocks(3, 2, map);
        ArrayList<Point> path = new ArrayList<>();
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
        ArrayList<Point> path = new ArrayList<>();
        assertTrue(GameLogic.canErase(0, 3, 5, 3, blocks, path));
        assertEquals(path.size(), 4);
        int[][] expected = new int[][] {
            {0, 3}, {0, 4}, {5, 4}, {5, 3}
        };
        for (int i = 0; i < 4; ++i) {
            System.out.printf("%d, %d\n", path.get(i).row, path.get(i).col);
        }
        for (int i = 0; i < 4; ++i) {
            assertEquals(path.get(i).row, expected[i][0]);
            assertEquals(path.get(i).col, expected[i][1]);
        }
        path.clear();
        assertTrue(GameLogic.canErase(5, 3, 0, 3, blocks, path));
    }
}
