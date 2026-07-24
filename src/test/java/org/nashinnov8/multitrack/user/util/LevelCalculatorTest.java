package org.nashinnov8.multitrack.user.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LevelCalculatorTest {

    @Test
    void calculateLevel_Level1() {
        assertEquals(1, LevelCalculator.calculateLevel(0));
        assertEquals(1, LevelCalculator.calculateLevel(50));
        assertEquals(1, LevelCalculator.calculateLevel(99));
    }

    @Test
    void calculateLevel_Level2() {
        assertEquals(2, LevelCalculator.calculateLevel(100));
        assertEquals(2, LevelCalculator.calculateLevel(200));
        assertEquals(2, LevelCalculator.calculateLevel(299));
    }

    @Test
    void calculateLevel_Level3() {
        assertEquals(3, LevelCalculator.calculateLevel(300));
        assertEquals(3, LevelCalculator.calculateLevel(450));
        assertEquals(3, LevelCalculator.calculateLevel(599));
    }

    @Test
    void calculateLevel_Level4() {
        assertEquals(4, LevelCalculator.calculateLevel(600));
        assertEquals(4, LevelCalculator.calculateLevel(999));
    }

    @Test
    void calculateLevel_Level5() {
        assertEquals(5, LevelCalculator.calculateLevel(1000));
    }

    @Test
    void calculateLevel_NegativeExp() {
        assertEquals(1, LevelCalculator.calculateLevel(-50));
    }
}
