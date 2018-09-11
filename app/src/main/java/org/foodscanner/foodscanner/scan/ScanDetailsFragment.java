package org.foodscanner.foodscanner.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.foodscanner.foodscanner.Constants;
import org.foodscanner.foodscanner.R;
import org.foodscanner.foodscanner.adapters.FragmentAdapter;
import org.foodscanner.foodscanner.data.Additive;
import org.foodscanner.foodscanner.data.DangerLevel;
import org.foodscanner.foodscanner.data.MultilineWord;
import org.foodscanner.foodscanner.data.Result;
import org.foodscanner.foodscanner.data.Word;
import org.foodscanner.foodscanner.data.WordBoundingBox;
import org.foodscanner.foodscanner.helpers.AdditiveMarker;
import org.foodscanner.foodscanner.helpers.FileOperations;
import org.foodscanner.foodscanner.helpers.UIHelper;
import org.foodscanner.foodscanner.interfaces.OcrCallbacksManager;
import org.foodscanner.foodscanner.tasks.AdditiveRecognitionTask;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScanDetailsFragment extends Fragment implements OcrCallbacksManager {

    private int mPosition;
    private EditText mWordText;
    private TextView mAdditiveText, mAdditiveDescription, mAdditiveDangerLevel;
    private ImageView mWordImage;
    private LinearLayout mBackgroundLayout;
    private Button mConfirmButton;
    private FragmentAdapter mFragmentAdapter;
    private boolean mHasInputChanged;

    public static ScanDetailsFragment newInstance(FragmentAdapter adapter, int position) {
        ScanDetailsFragment scanDetailsFragment = new ScanDetailsFragment();
        scanDetailsFragment.mPosition = position;
        scanDetailsFragment.mFragmentAdapter = adapter;
        return scanDetailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_details, container, false);
        initializeWordTextField(view);

        mAdditiveText = view.findViewById(R.id.details_description_text_view);
        mAdditiveDescription = view.findViewById(R.id.details_additive_description);
        mAdditiveDangerLevel = view.findViewById(R.id.details_additive_danger_level);
        mWordImage = view.findViewById(R.id.details_word_image);
        mBackgroundLayout = view.findViewById(R.id.details_background_layout);

        initializeConfirmButton(view);
        initializeMergeButtons(view);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        AppBarLayout appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
        appBarLayout.setElevation(0);
        updateDetailsView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mWordText.isFocused()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            checkNotNull(imm).hideSoftInputFromWindow(checkNotNull(getView()).getWindowToken(), 0);
        }
    }

    public void onPageChanged() {
        updateDetailsView();
    }

    private void initializeWordTextField(View view) {
        mWordText = view.findViewById(R.id.details_word_input_text_field);
        mWordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                setHasInputChanged(hasInputChanged());
            }
        });

        mWordText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mWordText.setSelection(mWordText.getText().length());
                }
            }
        });
    }

    private void initializeMergeButtons(View view) {
        ImageButton leftMerge =  view.findViewById(R.id.details_menu_merge_left);
        ImageButton rightMerge = view.findViewById(R.id.details_menu_merge_right);
        final Result result = Result.getInstance();

        if (mPosition == 0) {
            leftMerge.setEnabled(false);
            leftMerge.setImageDrawable(UIHelper.disableDrawable(leftMerge.getDrawable()));
        } else {
            leftMerge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    result.mergeTwoWords(mPosition, mPosition - 1);
                    recognizeAdditives();
                    getPager().mergeTwoDetailsFragments(mPosition, mPosition - 1);
                }
            });
        }

        if (mPosition == result.getResultSize() -1) {
            rightMerge.setEnabled(false);
            Log.d(Constants.TAG, "Right merge disabled");
            rightMerge.setImageDrawable(UIHelper.disableDrawable(rightMerge.getDrawable()));
        } else {
            rightMerge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    result.mergeTwoWords(mPosition, mPosition + 1);
                    recognizeAdditives();
                    getPager().mergeTwoDetailsFragments(mPosition, mPosition + 1);
                }
            });
        }
    }

    private void recognizeAdditives() {
        new AdditiveRecognitionTask(
                getContext(),
                ScanDetailsFragment.this,
                null
        ).execute();
    }

    /**
     * Sets click listener to the confirm button
     *
     * @param view containing the confirm button
     */
    private void initializeConfirmButton(View view) {
        mConfirmButton = view.findViewById(R.id.details_confirm_input_button);
        setHasInputChanged(hasInputChanged());
        mConfirmButton.setOnClickListener((View v) -> {
            Log.i(Constants.TAG, "Confirm button clicked!");

            // If input HAS NOT changed - exit method and don't bother with following code
            if ( !mHasInputChanged ) {
                // Can this happen anymore? (disabled button)
                Log.d(Constants.TAG, "Input has not changed - onClick finished.");
                return;
            }

            String input = mWordText.getText().toString();
            Word selectedWord = Result.getInstance().getWordAt(mPosition);
            selectedWord.setName(input);

            // If input must be split
            if (mustInputBeSplit(input)) {

                Log.d(Constants.TAG, "Input (Word) must be split - " + input);

                // Split Word into multiple Word(s)
                Result.getInstance().splitWord(mPosition);
                recognizeAdditives();
                getPager().increaseAmountOfDetailsFragments(mPosition);

                // Exit method
                setHasInputChanged(false);
                return;
            }

            // If corrected word WAS an additive
            if (isWordPartOfAdditive()) {
                Log.d(Constants.TAG, "Word is additive!");
                removeCurrentAdditive();
                updateDetailsView();
                //setHasInputChanged(false);
                //return;
            }

            // Find relevant surrounding words and analyze whether a new additive is found
            int[] additiveInterval = getRelevantSurroundingWords();
            int start = additiveInterval[0];
            int end = additiveInterval[1];

            new AdditiveRecognitionTask(
                    getContext(),
                    ScanDetailsFragment.this,
                    Result.getInstance().getWordSubList(start, ++end)
            ).execute();
            setHasInputChanged(false);
        });
    }

    /**
     * Returns whether the user provided correction needs to be split into multiple {@code Word}s
     * or not.
     *
     * @param input User corrected word
     *
     * @return {@code true} if input must be split, {@code false} otherwise
     */
    private boolean mustInputBeSplit(@NonNull String input) {
        return input.trim().contains(Result.SPLIT_WORD_SEPARATOR);
    }

    /**
     * Checks and returns whether the currently selected {@code Word} has an {@code Additive}
     * assigned.
     *
     * @return {@code true} if current {@code Word} has an {@code Additive} assigned, {@code false}
     * otherwise
     */
    private boolean isWordPartOfAdditive() {
        Word w = Result.getInstance().getWordAt(mPosition);

        return w.hasAdditive();
    }

    /**
     * Finds and returns the beginning and ending index of current non-additive surrounding words,
     * as well as the length of said sequence of words.
     *
     * @return {@code int} array of size 3, containing the beginning index, ending index and length
     */
    private int[] getRelevantSurroundingWords() {
        List<Word> wordList = Result.getInstance().getWordList();
        int beginIndex = mPosition;
        Word word = wordList.get(beginIndex);

        while (!word.hasAdditive() && beginIndex > 0) {

            beginIndex--;
            word = wordList.get(beginIndex);

        }

        if (word.hasAdditive()) {
            beginIndex++;
        }

        int endIndex = beginIndex;
        for (int i = beginIndex; i < wordList.size(); i++) {
            word = wordList.get(i);
            if (word.hasAdditive()) break;
            endIndex = i;
        }

        Log.d(Constants.TAG, beginIndex + " " + endIndex);

        return new int[] {beginIndex, endIndex, (endIndex-beginIndex)};
    }

    private void removeCurrentAdditive() {
        List<Word> wordList = Result.getInstance().getWordList();
        Word word = wordList.get(mPosition);

        // The Word "is" an additive
        if (word.isENumber()) {
            word.setAdditive(null);
            return;
        }

        // The Word might be part of a multi-word additive
        Additive additive = word.getAdditive();

        int index = mPosition;
        while (index > 0) {
            Word w = wordList.get(index);
            if (w.getAdditive() != null && w.getAdditive().getId() == additive.getId()) {
                w.setAdditive(null);
            } else {
                break;
            }
            index--;
        }

        index = mPosition;
        while (index < wordList.size()) {
            Word w = wordList.get(index);
            if (w.getAdditive() != null && w.getAdditive().getId() == additive.getId()) {
                w.setAdditive(null);
            } else {
                break;
            }
            index++;
        }
    }

    /**
     * Returns whether the input has been changed (edited) by the user or not.
     *
     * @return {@code true} if the input has been changed, {@code false} otherwise
     */
    private boolean hasInputChanged() {
        String inputText = mWordText.getText().toString();
        Word currentWord = Result.getInstance().getWordList().get(mPosition);

        // If recognized word equals input text return false
        // Otherwise return true
        return !(currentWord.getName().equals(inputText));
    }

    /**
     * Sets the {@code mHasInputChanged} variable to the specified value. Also enables or disables
     * the confirm button depending on the change.
     *
     * @param hasInputChanged true if input has changed false otherwise
     */
    private void setHasInputChanged(boolean hasInputChanged) {
        mConfirmButton.setEnabled(hasInputChanged);
        mHasInputChanged = hasInputChanged;
    }

    /**
     * Updates the displayed word / additive details information with the available data. The
     * specified {@code boolean} parameter tells whether the method has been run for the first time
     * or not.
     * <p>
     * If the method is run for the first time it will also update the {@code EditText}
     * field with the word recognized with OCR.
     */
    private void updateDetailsView() {
        if (!isAdded()){
            Log.e(Constants.TAG, "Fragment not yet added!");
            return;
        }

        if (mWordText == null) {
            Log.e(Constants.TAG, "Fragment not finished creating!");
            return;
        }

        List<Word> wordList = Result.getInstance().getWordList();
        if (wordList == null || mPosition >= wordList.size()) {
            Log.e(Constants.TAG, "Position > word list or mRecognizedWords is NULL");
            return;
        }

        Word word = wordList.get(mPosition);
        if (word instanceof MultilineWord) {
            Log.w(Constants.TAG, "Word is multiline");
        }

        mWordText.setText(word.getName());
        Bitmap wordImage = getWordImage(word.getBoundingBox());
        if (wordImage != null) {
            mWordImage.setImageBitmap(wordImage);
        }

        if (word.hasAdditive()) {
            Additive additive = word.getAdditive();
            mAdditiveText.setText(additive.getName());
            String description = additive.getDescription();
            description = description != null && description.length() > 0 ? description : isAdded() ? getString(R.string.details_description_not_available) : "ERROR";
            mAdditiveDescription.setText(description);
            String dangerLevel = word.getAdditive().getDangerLevel().toString(); //TODO: Get from resource
            mAdditiveDangerLevel.setText(String.format("(%s)", dangerLevel.replace('_', ' ')));
            mBackgroundLayout.setBackgroundResource(AdditiveMarker.getColorForDangerLevel(additive.getDangerLevel()));
        } else {
            mAdditiveText.setText(R.string.details_description_no_info);
            mAdditiveDescription.setText("");
            mAdditiveDangerLevel.setText("");
            mBackgroundLayout.setBackgroundResource(AdditiveMarker.getColorForDangerLevel(DangerLevel.UNDEFINED));
        }
    }

    private Bitmap getWordImage(WordBoundingBox box) {
        if (!isAdded()) {
            Log.e(Constants.TAG, "Fragment not added!");
            return null;
        }

        if (getContext() == null) {
            Log.e(Constants.TAG, "Context is null!");
            return null;
        }

        Bitmap resultImage = FileOperations.openExternalImageFile(
                getContext(),
                FileOperations.getCroppedImageName()
        );

        if (resultImage == null) {
            return null;
        }

        return Bitmap.createBitmap(resultImage, box.getTopLeftCoordinate().getXCoordinate(), box.getTopLeftCoordinate().getYCoordinate(), box.getWidth(),box.getHeight());
    }

    private ScanWordPager getPager() {
        return mFragmentAdapter.getPager();
    }

    @Override
    public void setOcrProgressPercentage(int percentage) { }

    @Override
    public void onOcrCompleted(String hocrText) { }

    @Override
    public void onOcrCanceled() { }

    @Override
    public void onHocrParsingComplete() { }

    @Override
    public void onAdditiveRecognitionCompleted(boolean wereAdditivesRecognized) {
        Log.d(Constants.TAG, "Additive recognition completed; new additives were recognizes: " + wereAdditivesRecognized);
        if (wereAdditivesRecognized) {
            updateDetailsView();
        }
    }
}
