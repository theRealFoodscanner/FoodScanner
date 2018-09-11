package org.foodscanner.foodscanner.tasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import org.foodscanner.foodscanner.data.Additive;
import org.foodscanner.foodscanner.data.AdditiveCache;
import org.foodscanner.foodscanner.data.Result;
import org.foodscanner.foodscanner.data.Word;
import org.foodscanner.foodscanner.helpers.DatabaseManager;
import org.foodscanner.foodscanner.helpers.LevenshteinDistance;
import org.foodscanner.foodscanner.helpers.WordAnalyzer;
import org.foodscanner.foodscanner.interfaces.OcrCallbacksManager;

import java.util.List;

/**
 * Class for recognizing OCR'd words.
 */
public class AdditiveRecognitionTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "FoodScanner-Recognition";
    private final OcrCallbacksManager mOcrCallbacksManager;
    private List<Word> mInputWordList;
    private DatabaseManager mDatabaseManager;

    public AdditiveRecognitionTask(
            Context context,
            OcrCallbacksManager callback,
            @Nullable List<Word> inputWordList) {
        mInputWordList = inputWordList;
        mOcrCallbacksManager = callback;
        mDatabaseManager = new DatabaseManager(context);
    }

    @Override
    protected void onPreExecute() {
        if (mInputWordList == null) {
            mInputWordList = Result.getInstance().getWordList();
        }
    }

    /**
     * Detects and marks additives in the specified {@code Word} list (constructor) or in the
     * {@code Result}'s {@code Word} list, if the former is {@code null}. Returns whether any
     * additive has been marked.
     * <p>
     * Iterates over the appropriate {@code Word} list and detects if it contains any additives
     * present in the database. If additives are found they are marked (set) in the appropriate
     * {@code Word}s. Either of the before mentioned lists will be modified by the detection
     * process.
     * <p>
     * Method returns {@code true} if any additives have been marked (set) and {@code false}
     * otherwise.
     *
     * @param voids parameter is ignored
     * @return {@code true} if process exited without problems, {@code false} otherwise
     */
    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean wereAdditivesRecognized = false;
        AdditiveCache.initializeCache(mDatabaseManager);

        for (int currentWordIndex = 0; currentWordIndex < mInputWordList.size();
             currentWordIndex++) {

            Word currentWord = mInputWordList.get(currentWordIndex);

            if (currentWord.hasAdditive()) {
                //Log.d(TAG, "Skipping additive word");
                continue;
            }

            // Check if word is E-Number & assign Additive
            String possibleENumber = WordAnalyzer.couldBeENumber(currentWord.getName());
            if (possibleENumber != null) {
                Additive additive = AdditiveCache.findAdditiveByEnumber(possibleENumber);
                if (additive != null) {
                    currentWord.setAdditive(additive);
                    wereAdditivesRecognized = true;
                    continue;
                }
            }

            // Word probably isn't E-Number, therefore compare by name
            List<Pair<Integer, Additive>> possibleAdditives = AdditiveCache.getPossibleAdditives(currentWord.getName());
            for (Pair<Integer, Additive> pair : possibleAdditives) {
                // Count words in Additive
                int additiveWords = 1;
                for (char c : pair.second.getName().trim().toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        additiveWords++;
                    }
                }

                // Index Validation
                if (currentWordIndex - pair.first < 0 || currentWordIndex + additiveWords - pair.first >= mInputWordList.size()) {
                    // Indexes out of bounds
                    continue;
                }

                //TODO: Ignore Words with already assigned Additives?

                // Concatenate Words
                List<Word> relevantWords = mInputWordList.subList(currentWordIndex - pair.first,currentWordIndex + additiveWords - pair.first);
                String[] relevantWordNames = new String[relevantWords.size()];
                for (int i = 0; i < relevantWords.size(); i++) {
                    relevantWordNames[i] = relevantWords.get(i).getName();
                }

                // Check if Words match Additive
                if (AdditiveCache.isSpecifiedAdditive(relevantWordNames, pair.second)) {
                    // Assign Additive to Words
                    for (Word w: relevantWords) {
                        w.setAdditive(pair.second);
                    }

                    // Skip newly assigned Words
                    currentWordIndex += (additiveWords - pair.first);

                    wereAdditivesRecognized = true;
                    break;
                }
            }
        }
        return wereAdditivesRecognized;
    }

    @Override
    protected void onPostExecute(Boolean wereAdditivesRecognized) {
        mDatabaseManager.close();
        mOcrCallbacksManager.onAdditiveRecognitionCompleted(wereAdditivesRecognized);
    }

    /**
     * Compares the content of the {@code String[] additiveName} against the content of the
     * {@code String namePart}. Returns the array index of where the {@code String}s are equal. If
     * no such index is found, {@code -1} is returned.
     *
     * @param additiveName additive name split into array
     * @param namePart single additive word (name part)
     * @return index of matching {@code String}s, {@code -1} if no match is found
     */
    private int indexOfNamePart(String[] additiveName, String namePart) {
        for (int i = 0; i < additiveName.length; i++) {
            if (namePart.toUpperCase().equals(additiveName[i].toUpperCase())) {
                return i;
            }
        }

        return -1;
    }
}