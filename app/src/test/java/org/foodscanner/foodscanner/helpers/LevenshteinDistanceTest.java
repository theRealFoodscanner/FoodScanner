package org.foodscanner.foodscanner.helpers;

import org.junit.Test;

import static org.junit.Assert.*;

public class LevenshteinDistanceTest {

    @Test
    public void calculate_same_word() {
        String input1 = "word";
        String input2 = "word";
        int expectedDistance = 0;

        int actualDistance = LevenshteinDistance.calculate(input1, input2);

        assertEquals(expectedDistance, actualDistance);
    }

    @Test
    public void caluclate_different_word() {
        String input1 = "abcde";
        String input2 = "edcba";
        int expectedDistance = 4;

        int actualDistance = LevenshteinDistance.calculate(input1, input2);

        assertEquals(expectedDistance, actualDistance);
    }
}