package org.foodscanner.foodscanner.data;

import android.database.Cursor;
import android.support.v4.util.Pair;
import android.util.Log;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.helpers.DatabaseManager;
import org.foodscanner.foodscanner.helpers.LevenshteinDistance;

import java.util.ArrayList;
import java.util.List;

public class AdditiveCache {
    static final String TAG = "FoodScannerV2";
    public static final int MAX_ENUMBER_LENGTH = 7;   //TODO: Move this
    public static final int MIN_ENUMBER_LENGTH = 4;   //TODO: ^

    private static List<Additive> sAdditives;

    // Non-instantiable class
    private AdditiveCache() {}

    public static synchronized void initializeCache(DatabaseManager databaseManager) {
        Log.d(TAG, "Initializing cache");
        if (databaseManager == null) {
            Log.e(TAG, "Database manager is null");
            throw new IllegalArgumentException();
        }

        if (isCacheInitialized()) {
            Log.d(TAG, "Cache already initialized");
            return;
        }

        Cursor c = databaseManager.getAllAdditives();

        int maxENumberLength = 0;
        int resultSize = c.getCount();
        sAdditives = new ArrayList<>(resultSize);

        while (!c.isAfterLast()) {
            String eNumber = c.getString(0);
            maxENumberLength = eNumber.length() > maxENumberLength? eNumber.length() : maxENumberLength;
            int dangerLevel = c.getInt(1);
            String name = c.getString(3);
            String description = c.getString(5);

            Additive additive = new Additive(name, eNumber, description, dangerLevel);
            sAdditives.add(additive);
            c.moveToNext();
        }
        Log.d(TAG, "Cache is now initialized!");
    }

    public static List<Pair<Integer, Additive>> getPossibleAdditives(String word) {
        List<Pair<Integer, Additive>> possibleAdditives = new ArrayList<>();
        int maxLengthDifferent = LevenshteinDistance.getMaxLevenshteinDistanceForWord(word);

        for (Additive additive : sAdditives) {
            //Log.d(TAG, "Current additive: " + additive.getName());
            String[] additiveName = additive.getName().split(" ");
            for (int index = 0; index < additiveName.length; index++) {
                if (LevenshteinDistance.calculate(additiveName[index], word) <= maxLengthDifferent) {
                    possibleAdditives.add(new Pair<>(index, additive));
                    break;
                }
            }
        }

        return possibleAdditives;
    }

    public static boolean isSpecifiedAdditive(String[] probableWords, Additive additive) {
        String[] additiveWords = additive.getName().split(" ");
        int maxDistance = LevenshteinDistance.getMaxLevenshteinDistanceForWord(additive.getName());
        int currentDistance = 0;
        for (int i = 0; i < probableWords.length; i++) {
            currentDistance += LevenshteinDistance.calculate(probableWords[i], additiveWords[i]);
            if (currentDistance > maxDistance) {
                return false;
            }
        }

        return true;
    }

    public static Additive findAdditiveByEnumber(final String eNumber) {
        for (Additive a : sAdditives) {
            if (a.getENumber().toUpperCase().equals(eNumber.toUpperCase())) {
                return a;
            }
        }

        return null;
    }

    public static AdditiveResponse getAdditive(String input) {
        if (!isCacheInitialized()) {
            return null;
        }

        ENumberResponse eNumberResponse;
        if ((eNumberResponse = isENumber(input)).isENumber()) {
            return getAdditiveByENumber(eNumberResponse.getWord());
        }

        return getAdditiveByName(input, 0);
    }

    public static AdditiveResponse getAdditive(AdditiveResponse previousResponse, String nextWord) {
        String[] input = previousResponse.getInput();
        if (!input[input.length-1].isEmpty()) {
            // Can't do anything else
            return new AdditiveResponse();
        }

        int lastIndex = input.length-1;
        for (; lastIndex >= 0; lastIndex--) {
            if (!input[lastIndex].isEmpty()) {
                // First non-empty field from end
                break;
            }
        }

        String currentWord = previousResponse.getAdditive().getName().split(" ")[lastIndex+1];
        if (LevenshteinDistance.calculate(currentWord, nextWord) <= determineMaxWordDistanceFromWordLegnth(nextWord.length())) {
            input[lastIndex+1] = nextWord;
            return new AdditiveResponse(previousResponse.getAdditive(), input, previousResponse.getAdditiveIndex()+1);
        }

        String firstWord = "";
        for (String s : input) {
            if (!s.isEmpty()) {
                firstWord = s;
                break;
            }
        }

        return getAdditiveByName(firstWord, previousResponse.getAdditiveIndex()+1);
    }

    private static boolean isCacheInitialized() {
        return sAdditives != null && sAdditives.size() != 0;
    }

    private static AdditiveResponse getAdditiveByName(String name, int startIndex) {
        if (startIndex >= sAdditives.size()) {
            // No more additives
            return null;
        }

        int bestDistance = Integer.MAX_VALUE;
        int bestMatchIndex = -1;
        int bestWordIndex = -1;

        for (int i = startIndex; i < sAdditives.size(); i++) {
            Additive additive = sAdditives.get(i);
            String additiveName = additive.getName();
            int index = 0;
            for (String additiveNamePart : additiveName.split(" ")) {
                int currentDistance = LevenshteinDistance.calculate(additiveNamePart, name);
                if (currentDistance < bestDistance) {
                    if (currentDistance == 0) {
                        return new AdditiveResponse(additive, name, index, i);
                    }
                    bestDistance = currentDistance;
                    bestMatchIndex = i;
                    bestWordIndex = index;
                }
                index++;
            }
        }

        if (bestDistance <= determineMaxWordDistanceFromWordLegnth(name.length())) {
            return new AdditiveResponse(sAdditives.get(bestMatchIndex), name, bestWordIndex, bestMatchIndex);
        }

        return new AdditiveResponse();
    }

    //TODO: Improve algorithm :^)
    private static int determineMaxWordDistanceFromWordLegnth(int wordLength) {
        //return wordLength / 5;
        String tmp = String.format("%1$" + wordLength + "s", "0");
        return LevenshteinDistance.getMaxLevenshteinDistanceForWord(tmp);
    }

    // Returns first Additive matching the specified E number.
    private static AdditiveResponse getAdditiveByENumber(String eNumber) {
        for (Additive additive: sAdditives) {
            if (additive.getENumber().toUpperCase().equals(eNumber.toUpperCase())) {
                return new AdditiveResponse(additive);
            }
        }

        return new AdditiveResponse();
    }

    //TODO: Rename
    private static ENumberResponse isENumber(String input) {

        if (input.matches(Constants.E_NUMBER_REGEX)) {
            // Input matches E number format
            new ENumberResponse(input, true);
        }

        String modifiedInput = input.toLowerCase();
        modifiedInput = tryToMakeENumber(modifiedInput);

        if (modifiedInput.matches(Constants.E_NUMBER_REGEX)) {
            // Input matches E number format
            new ENumberResponse(modifiedInput, true);
        }

        return new ENumberResponse(input, false);
    }

    // Try replacing some characters to get E number. If successful, the transformed E number is
    // returned. Otherwise null is returned.
    private static String tryToMakeENumber(String input) {

        if (input.length() < MIN_ENUMBER_LENGTH) {
            // Input too short to be E number
            return null;
        }

        if (input.length() > MAX_ENUMBER_LENGTH) {
            // Input too long to be E number
            return null;
        }

        char[] inputChars = input.toCharArray();

        if (inputChars[0] != 'e') {
            // First character doesn't match
            char replaced = tryReplace(inputChars[0], false, false);
            inputChars[0] =  replaced != ' '? replaced : inputChars[0];
        }

        if (!Character.isDigit(inputChars[1])) {
            // Second character doesn't match
            char replaced = tryReplace(inputChars[1], true, false);
            inputChars[1] =  replaced != ' '? replaced : inputChars[1];
        }

        if (!Character.isDigit(inputChars[2])) {
            // Second character doesn't match
            char replaced = tryReplace(inputChars[2], true, false);
            inputChars[2] =  replaced != ' '? replaced : inputChars[2];
        }

        if (!Character.isDigit(inputChars[3])) {
            // Second character doesn't match
            char replaced = tryReplace(inputChars[3], true, false);
            inputChars[3] =  replaced != ' '? replaced : inputChars[3];
        }

        boolean step4 = false;
        if (inputChars.length > 5) {
            step4 = true;
            if (!Character.isDigit((inputChars[4]))) {
                char replaced = tryReplace(inputChars[4], true, false);
                inputChars[4] =  replaced != ' '? replaced : inputChars[4];
            }
        }

        if (!step4 && inputChars.length > 4 && (!Character.isDigit(inputChars[4]) || (inputChars[4] < 'a' || inputChars[4] > 'e'))) {
            char replaced = tryReplace(inputChars[4], true, false);
            if (replaced == ' ') {
                replaced = tryReplace(inputChars[4], false, true);
            }
            inputChars[4] =  replaced != ' '? replaced : inputChars[4];
        }

        if (inputChars.length > 5 && (inputChars[5] < 'a' || inputChars[5] > 'e')) {
            char replaced = tryReplace(inputChars[5], false, true);
            inputChars[5] =  replaced != ' '? replaced : inputChars[5];
        }

        //TODO: Handle failed replacements (whitespace)

        return new String(inputChars);
    }

    // Return first possible replacement character or whitespace if none are found
    private static char tryReplace(char original, boolean isDigit, boolean isLetter) {
        //TODO: Move this
        List<Pair<Character, Character>> similarCharacters = new ArrayList<>();
        similarCharacters.add(new Pair<>('0', 'o'));
        similarCharacters.add(new Pair<>('5', 's'));
        similarCharacters.add(new Pair<>('e', '5'));

        char replaced = ' ';

        for (Pair<Character, Character> pair : similarCharacters) {

            // Not what we're looking for - skip
            if (!isDigit && !isLetter && pair.first != 'e') continue;
            if (isDigit && !isLetter && !Character.isDigit(pair.first)) continue;
            if (!isDigit && isLetter && (pair.first < 'a' || pair.first > 'e')) continue;

            if (original == pair.second) {
                // Could be similar character, replace it
                replaced = pair.second;
                break;
            }
        }

        return replaced;
    }

    private static class ENumberResponse {
        private final boolean mIsENumber;
        private final String mWord;

        public ENumberResponse(String word, boolean isENumber ) {
            mWord = word;
            mIsENumber = isENumber;
        }

        public boolean isENumber() {
            return mIsENumber;
        }

        public String getWord() {
            return mWord;
        }
    }

    public static class AdditiveResponse {
        private Additive mAdditive;
        private String[] mInput;
        private int mAdditiveIndex;
        private boolean mIsENumber;
        private boolean mIsMatch;

        public AdditiveResponse(Additive additive, String input, int wordIndex, int additiveIndex) {
            mAdditive = additive;
            mInput = new String[additive.getName().length()];
            mInput[wordIndex] = input;
            mIsENumber = false;
            mAdditiveIndex = additiveIndex;
            mIsMatch = mInput.length == 1;
        }

        public AdditiveResponse(Additive additive, String[] input, int additiveIndex) {
            mAdditive = additive;
            mInput = input;
            mIsENumber = false;
            mAdditiveIndex = additiveIndex;
            mIsMatch = false;
            // If all fields in input are set, it is a match
            for (int i = 0; i < input.length; i++) {
                if (input[i].isEmpty()) {
                    break;
                }
                if (i == input.length - 1) {
                    mIsMatch = true;
                }
            }
        }

        public AdditiveResponse(Additive additive) {
            mAdditive = additive;
            mIsENumber = true;
            mIsMatch = true;
        }

        public AdditiveResponse() {
            mIsMatch = false;
        }

        public Additive getAdditive() {
            return mAdditive;
        }

        public String[] getInput() {
            return mInput;
        }

        public int getAdditiveIndex() {
            return mAdditiveIndex;
        }

        public boolean isENumber() {
            return mIsENumber;
        }

        public boolean isMatch() {
            return mIsMatch;
        }
    }

}
