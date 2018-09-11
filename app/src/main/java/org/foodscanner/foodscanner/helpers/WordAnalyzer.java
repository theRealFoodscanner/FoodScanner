package org.foodscanner.foodscanner.helpers;

import org.foodscanner.foodscanner.Constants;

public class WordAnalyzer {

    /**
     * Returns whether the specified is in the E-Number format.
     *
     * @param word word to determine if it is an E-Number
     * @return true if word matches the E-Number format, false otherwise
     */
    public static boolean isENumber(String word) {
        return word.matches(Constants.E_NUMBER_REGEX);
    }

    public static String couldBeENumber(String word) {
        // Is an actual E-Number
        if (isENumber(word)) {
            return word;
        }

        // Size doesn't fit
        if (Constants.MIN_ENUMBER_LENGTH > word.length()
                || word.length() > Constants.MAX_ENUMBER_LENGTH) {
            return null;
        }

        // Count amount of numbers
        int numberOfDigits = 0;
        for (char c : word.toCharArray()) {
            if (Character.isDigit(c)) {
                numberOfDigits++;
            }
        }

        // Not enough numbers
        if (numberOfDigits < word.length()/2) {
            return null;
        }

        // Transform to lower case for easier analysis
        word = word.toLowerCase();

        StringBuilder eNumberBuilder = new StringBuilder();

        //TODO: Add more replacements
        switch (word.charAt(0)) {
            case 'e':
                break;
            case '5':
                break;
            case 's':
                break;
            default:
                return null;
        }
        eNumberBuilder.append('E');

        for (int i = 1; i < word.length(); i++) {
            char currentChar = word.charAt(i);
            if (Character.isDigit(currentChar)) {
                eNumberBuilder.append(currentChar);
            } else {
                if (i == word.length()-1) {
                    // Last non-digit character (could be a-e, ex. E150d)
                    if ('a' <= currentChar && currentChar <= 'e') {
                        eNumberBuilder.append(currentChar);
                        break;
                    }
                }
                switch (currentChar) {
                    case 'i':
                        eNumberBuilder.append(1);
                        break;
                    default:
                        return null;
                }
            }
        }

        // Check & return
        if (isENumber(eNumberBuilder.toString())) {
            return eNumberBuilder.toString();
        }

        return null;
    }
}
