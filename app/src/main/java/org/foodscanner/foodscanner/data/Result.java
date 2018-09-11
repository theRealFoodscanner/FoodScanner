package org.foodscanner.foodscanner.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Static data structure which stores the result
 */
public class Result {

    public static String SPLIT_WORD_SEPARATOR = " ";
    public static int WHITESPACE_PIXEL_SIZE_BETWEEN_WORDS = 0;
    public static String TAG = "FoodScanner-RESULT";

    private static Result sResult;
    private List<Word> mWordList;

    private Result() {
        mWordList = new ArrayList<>();
        WHITESPACE_PIXEL_SIZE_BETWEEN_WORDS = 0;
    }

    public static Result getInstance() {
        if (sResult == null) {
            sResult = new Result();
        }
        return sResult;
    }

    /**
     * Resets the result data - all progress is lost for current scan
     */
    public void reset() {
        sResult = null;
    }

    public void setWordList(List<Word> wordList) {
        mWordList = wordList;
    }

    public void addWord(Word word) {
        calculateAndSetAverageWhiteSpace(word);
        mWordList.add(word);
    }

    public List<Word> getWordList() {
        return mWordList;
    }

    public List<Word> getWordSubList(int from, int to) {
        return mWordList.subList(from, to);
    }

    public Word getWordAt(int index) {
        if (index < mWordList.size()) {
            return mWordList.get(index);
        }
        return null;
    }

    public void setWordName(String name, int index) {
        if (index < mWordList.size()) {
            mWordList.get(index).setName(name);
        }
    }

    public void setWordAdditive(Additive additive, int index) {
        if (index < mWordList.size()) {
            mWordList.get(index).setAdditive(additive);
        }
    }

    public int getResultSize() {
        return mWordList.size();
    }

    /**
     * Takes the two {@code Word}s at the specified indexes and creates a new {@code Word} which
     * combines values of both {@code Word}s. Removes the {@code Word} at the second index and
     * replaces the {@code Word} at the first index with the newly created {@code Word}.
     *
     * @param word1Index index of first {@code Word} in list to merge
     * @param word2Index index of second {@code Word} in list to merge
     */
    public void mergeTwoWords(int word1Index, int word2Index) {

        if (word1Index < 0 || word1Index >= mWordList.size()
                || word2Index < 0 || word2Index >= mWordList.size()) {
            // Invalid word(s)
            return;
        }

        int smallerIndex = Math.min(word1Index, word2Index);
        int largerIndex = Math.max(word1Index, word2Index);

        Word word1 = mWordList.get(smallerIndex);
        Word word2 = mWordList.get(largerIndex);
        Word mergedWord = word1.merge(word2);

        mWordList.remove(largerIndex);
        mWordList.set(smallerIndex, mergedWord);
        Log.d(TAG, mergedWord.getName() + " @ " + mWordList.indexOf(mergedWord));
        //printAll();
    }

    //TODO: Fix split coordinates...
    /**
     * Splits the specified {@code Word} in the list into more {@code Word}s.
     * <p>
     * The {@code Word} will be split around the separator defined in the variable {@code
     * SPLIT_WORD_SEPARATOR} - which is whitespace by default. The original {@code Word} will be
     * removed from the list, and the new {@code Word}s will be put in its place.
     * @param wordIndex index of the {@code Word} in the list to split
     */
    public void splitWord(int wordIndex) {

        //Log.d(TAG, "Splitting started!");

        List<Word> newWords = mWordList.get(wordIndex).split();

        // TODO: remove additive from "connected" Words

            mWordList.remove(wordIndex);
            mWordList.addAll(wordIndex, newWords);
    }

    private void calculateAndSetAverageWhiteSpace(Word w) {
        if (mWordList.isEmpty()) {
            WHITESPACE_PIXEL_SIZE_BETWEEN_WORDS = 0;
            return;
        }
        Word lastWord = mWordList.get(mWordList.size() - 1);

        if (lastWord.getLineNumber() != w.getLineNumber()) {
            // Words aren't in the same line
            return;
        }
        int x1, x2;
        x1 = lastWord.getBoundingBox().getBottomRightCoordinate().getXCoordinate();
        x2 = w.getBoundingBox().getTopLeftCoordinate().getXCoordinate();

        // Assign new average whitespace size
        WHITESPACE_PIXEL_SIZE_BETWEEN_WORDS = (
                ((Math.max(WHITESPACE_PIXEL_SIZE_BETWEEN_WORDS, 1) * mWordList.size()) + x2-x1)
                        / (mWordList.size()+1)
        );
    }

}