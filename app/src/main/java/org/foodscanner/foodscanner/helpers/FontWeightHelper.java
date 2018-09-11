package org.foodscanner.foodscanner.helpers;

import java.util.Arrays;

public class FontWeightHelper {

    private static char[] WEIGHT_1 = new char[] { 'i', 'j', 'l', ',', '.', '!', ':', ';', '"', '\'' };
    private static char[] WEIGHT_2 = new char[] { 'f', 'r', 't', '(', ')' };
    private static char[] WEIGHT_3 = new char[] { 's', 'š', 'z', 'ž', '/' };
    private static char[] WEIGHT_4 = new char[] { 'c', 'č', 'k', 'v' };
    private static char[] WEIGHT_5 = new char[] { 'a', 'e', 'g' };
    private static char[] WEIGHT_6 = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*' };
    private static char[] WEIGHT_7 = new char[] { 'b', 'd', 'h', 'n', 'o', 'p', 'u'};
    private static char[] WEIGHT_8 = new char[] { 'm'};

    public static int getWordWeight(String word) {
        if (word == null) {
            return 0;
        }

        int weight = 0;
        for (char c : word.toCharArray()){
            weight += getCharWeight(c);
        }

        return weight;
    }

    public static int getCharWeight(char c){
        if (Arrays.asList(WEIGHT_1).contains(c)) {
            return 10;
        }

        if (Arrays.asList(WEIGHT_2).contains(c)) {
            return 13;
        }

        if (Arrays.asList(WEIGHT_3).contains(c)) {
            return 16;
        }

        if (Arrays.asList(WEIGHT_4).contains(c)) {
            return 17;
        }

        if (Arrays.asList(WEIGHT_5).contains(c)) {
            return 20;
        }

        if (Arrays.asList(WEIGHT_6).contains(c)) {
            return 21;
        }

        if (Arrays.asList(WEIGHT_7).contains(c)) {
            return 21;
        }

        if (Arrays.asList(WEIGHT_8).contains(c)) {
            return 32;
        }

        // Somewhat average
        return 15;
    }
}
