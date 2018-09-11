package org.foodscanner.foodscanner.data;

import android.util.Log;

import org.foodscanner.foodscanner.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.foodscanner.foodscanner.data.Result.SPLIT_WORD_SEPARATOR;

public class MultilineWord extends Word {
    private List<Word> mLines;

    public MultilineWord(Word[] words) {
        super(getWholeName(words));
        addLines(words);
    }

    public void addLines(Word[] words) {
        tryInitLines();
        mLines.addAll(mergeSameLineWords(Arrays.asList(words)));
    }

    public void addLines(List<Word> words) {
        tryInitLines();
        mLines.addAll(mergeSameLineWords(words));
    }

    public List<Word> getAllLines() {
        return mLines;
    }

    private void tryInitLines() {
        if (mLines == null) {
            mLines = new ArrayList<>();
        }
    }

    private static String getWholeName(Word[] words) {
        StringBuilder wholeName = new StringBuilder();
        for (Word w : words) {
            wholeName.append(w.getName());
        }

        return wholeName.toString().trim();
    }

    private static List<Word> mergeSameLineWords(List<Word> words) {
        List<Word> wordList = new ArrayList<>();

        for (int i = 0; i < words.size(); i++) {
            final Word currentWord = words.get(i);

            boolean isCurrentLineDone = false;
            for (Word w : wordList) {
                if (w.getLineNumber() == currentWord.getLineNumber()) {
                    isCurrentLineDone = true;
                    break;
                }
            }

            if (isCurrentLineDone) {
                continue;
            }

            for (int j = i+1; j < words.size(); j++) {
                if (words.get(i).getLineNumber() == words.get(j).getLineNumber()) {
                    currentWord.merge(words.get(j));
                }
            }
            wordList.add(currentWord);
        }

        return wordList;
    }

    public List<Word> split() {
        Log.d(Constants.TAG, "Multiline splitter");
        String[] newWordNames = getName().split(SPLIT_WORD_SEPARATOR);

        if (newWordNames.length < 2) {
            Log.w(Constants.TAG, String.format("Splitting %1$s with separator '%2$s' produced no results. Should not happen - as splitting is triggered automatically when separator is detected.", getName(), SPLIT_WORD_SEPARATOR));
            return null;
        }

        //TODO: 2 cases: split at end of line (Multiword => multiple Words) or split elsewhere (split Word out of Multiline - either left or right).
        //TODO: Extra case: word name was changed and split at once... (count characters? don't allow both at same time?)

        // Assuming names haven't changed
        List<Word> wordList = new ArrayList<>();
        LinkedList<Word> words = new LinkedList<>();
        words.addAll(mLines);
        Word currentWord = words.removeFirst();
        for (int i = 0; i < newWordNames.length;) {
            String name = newWordNames[i];
            int nameLength = name.length();
            String wordName = currentWord.getName();
            int wordLength = wordName.length();

            if (wordLength == nameLength) {
                // Split happens at line end
                currentWord.setName(name);
                wordList.add(currentWord);
                if (words.isEmpty()) {
                    // End
                    break;
                }
                currentWord = words.removeFirst();
                i++;
            } else if (wordLength > nameLength) {
                // Split happens in middle of word
                String newName = wordName.substring(0, nameLength) + SPLIT_WORD_SEPARATOR + wordName.substring(nameLength);
                currentWord.setName(newName);
                List<Word> splitWords = currentWord.split();   // Size should always be 2
                splitWords.get(0).setName(name);
                wordList.add(splitWords.get(0));
                currentWord = splitWords.get(1);
                i++;
            } else {
                // Split happens in next line(s)
                currentWord = currentWord.merge(words.removeFirst());
            }
        }

        return wordList;
    }
}
