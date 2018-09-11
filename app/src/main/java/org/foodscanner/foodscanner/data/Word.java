package org.foodscanner.foodscanner.data;

import android.util.Log;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.helpers.FontWeightHelper;

import java.util.ArrayList;
import java.util.List;

import static org.foodscanner.foodscanner.data.Result.SPLIT_WORD_SEPARATOR;
import static org.foodscanner.foodscanner.data.Result.WHITESPACE_PIXEL_SIZE_BETWEEN_WORDS;

/**
 * Data structure for storing ingredient words.
 */
public class Word {

    private WordBoundingBox mBoundingBox;
    private String mName;
    private Additive mAdditive;
    private int mConfidence;
    private int mLineNumber;
    private boolean mIsWordValid;

    public Word(Word word) {
        mBoundingBox = word.mBoundingBox;
        mName = word.mName;
        mAdditive = word.mAdditive;
        mConfidence = word.mConfidence;
        mLineNumber = word.mLineNumber;
        mIsWordValid = word.mIsWordValid;
    }

    public Word(String name) {
        mName = sanitizeWordName(name);
        mIsWordValid = !mName.isEmpty();
        mAdditive = null;
        mBoundingBox = new WordBoundingBox();
        mConfidence = -1;
        mLineNumber = -1;
    }

    public boolean hasAdditive() {
        return ( mAdditive != null );
    }

    /**
     * Checks and returns whether this {@code Word} represents an E-number or not.
     *
     * @return {@code true} if this {@code Word} represents an E-number, {@code false} otherwise
     */
    public boolean isENumber() {
        return mName.matches(Constants.E_NUMBER_REGEX);
    }

    protected static String sanitizeWordName(String name) {
        boolean sanitizedStart = false;
        String tmpName = name;
        for (int i = 0; i < tmpName.length(); i++) {
            if (!Character.isLetterOrDigit(tmpName.charAt(i))) {
                continue;
            }

            sanitizedStart = true;
            tmpName = tmpName.substring(i);
            break;
        }

        if (!sanitizedStart) {
            return "";
        }

        for (int i = tmpName.length() - 1; i > 0; i--) {
            if (!Character.isLetterOrDigit((tmpName.charAt(i)))) {
                continue;
            }

            tmpName = tmpName.substring(0, i+1);
            break;
        }

        return tmpName;
    }

    /* Getters and setters */

    public void setBoundingBox(Point topLeft, Point bottomRight) {
        mBoundingBox.setBoundingBox(topLeft, bottomRight);
    }

    public void setBoundingBox(int leftX, int topY, int rightX, int bottomY) {
        Point topLeft = new Point(leftX, topY);
        Point botomRight = new Point(rightX, bottomY);
        setBoundingBox(topLeft, botomRight);
    }

    public void setLineNumber(int line) {
        mLineNumber = line;
    }

    public int getLineNumber() {
        return mLineNumber;
    }

    public WordBoundingBox getBoundingBox() {
        return mBoundingBox;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setAdditive(Additive additive) {
        mAdditive = additive;
    }

    public Additive getAdditive() {
        return mAdditive;
    }

    public void setConfidence(int confidence) {
        mConfidence = confidence;
    }

    public int getConfidence() {
        return mConfidence;
    }

    public boolean isValid() {
        return mIsWordValid;
    }

    public Word merge(Word wordToMergeWith) {
        Word mergedWord;
        if (mLineNumber != wordToMergeWith.mLineNumber) {
            mergedWord = new MultilineWord(new Word[] {this, wordToMergeWith});
        } else {
            mergedWord = new Word(mName + wordToMergeWith.mName);
        }

        mergedWord.mBoundingBox.setTopLeftCoordinate((this.mBoundingBox.getTopLeftCoordinate()));
        mergedWord.mBoundingBox.setBottomRightCoordinate(wordToMergeWith.mBoundingBox.getBottomRightCoordinate());
        mergedWord.mConfidence = (mConfidence + wordToMergeWith.mConfidence) / 2;

        return  mergedWord;
    }

    public List<Word> split() {
        Log.d(Constants.TAG, "Single line splitter");
        int wordPixelLength = mBoundingBox.getWidth();
        int wordNameWeight = FontWeightHelper.getWordWeight(mName.replace(" ", ""));
        String[] newWordNames = mName.split(SPLIT_WORD_SEPARATOR);

        if (newWordNames.length < 2) {
            Log.w(Constants.TAG, String.format("Splitting %1$s with separator '%2$s' produced no results. Should not happen - as splitting is triggered automatically when separator is detected.", mName, SPLIT_WORD_SEPARATOR));
            return null;
        }

        // Word coordinates and length
        int leftX = mBoundingBox.getTopLeftCoordinate().getXCoordinate();
        final int rightX = mBoundingBox.getBottomRightCoordinate().getXCoordinate();
        final int topY = mBoundingBox.getTopLeftCoordinate().getYCoordinate();
        final int bottomY = mBoundingBox.getBottomRightCoordinate().getYCoordinate();

        List<Word> newWords = new ArrayList<>();
        for (String newWordName : newWordNames) {
            Word newWord = new Word(newWordName);
            newWord.setLineNumber(mLineNumber);
            newWord.setConfidence(mConfidence);
            int newWordPixelLength = FontWeightHelper.getWordWeight(newWord.getName()) * wordPixelLength / wordNameWeight;
            newWord.setBoundingBox(leftX, topY, Math.min(leftX + newWordPixelLength, rightX), bottomY);
            leftX += newWordPixelLength + WHITESPACE_PIXEL_SIZE_BETWEEN_WORDS;
            newWords.add(newWord);
        }

        return newWords;
    }
}