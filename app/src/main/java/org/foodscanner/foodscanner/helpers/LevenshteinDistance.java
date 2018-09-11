package org.foodscanner.foodscanner.helpers;

/**
 * Class for computing Levenshtein distances
 */
public class LevenshteinDistance {

    public static final int MAX_LEVENSHTEIN_DISTANCE = 2;
    private static final int[] LEVENSHTEIN_DISTANCES_WORD_LENGTH_BOUNDS = { 5, 8, 14, 20 };
    private static final int[] LEVENSHTEIN_DISTANCES = { 1, 2, 3, 4};

    public static int calculate(String first, String second) {
        int[][] distance = new int[first.length() + 1][second.length() + 1];

        for (int i = 0; i <= first.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 1; j <= second.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((first.charAt(i - 1) == second.charAt(j - 1))? 0 : 1)
                );
            }
        }

        return distance[first.length()][second.length()];
    }

    public static int getMaxLevenshteinDistanceForWord(String word) {
        int wordLength = word.length();
        for (int i = LEVENSHTEIN_DISTANCES_WORD_LENGTH_BOUNDS.length - 1; i >= 0; i--) {
            if (wordLength > LEVENSHTEIN_DISTANCES_WORD_LENGTH_BOUNDS[i]) {
                return LEVENSHTEIN_DISTANCES[i];
            }
        }

        return 0;
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
}
