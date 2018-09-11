package org.foodscanner.foodscanner.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.foodscanner.foodscanner.data.Point;
import org.foodscanner.foodscanner.data.Result;
import org.foodscanner.foodscanner.data.Word;
import org.foodscanner.foodscanner.interfaces.OcrCallbacksManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Class for parsing the hOCR result returned by the OCR process.
 */
public class ParseHocrTask extends AsyncTask<String, Void, Boolean> {

    private OcrCallbacksManager mOcrCallbacksManager;
    private static final String TAG = "FoodScanner-Hocr";
    private static final String XML_ENCODING = "UTF-8";
    private static final String XML_SPAN_TAG = "span";
    private static final String XML_CLASS_TAG = "class";
    private static final String XML_OCRX_TAG = "ocrx_word";
    private static final String XML_LINE_TAG = "ocr_line";
    private static final String XML_ID_TAG = "id";
    private static final String XML_TITLE_TAG = "title";

    public ParseHocrTask(OcrCallbacksManager callback) {
        mOcrCallbacksManager = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Parses specified hOCR data and stores the parsed {@code Word}s in the {@code Result} class.
     * Returns whether the process finished without errors or not.
     *
     * @param strings raw hOCR data
     * @return {@code true} if process finished without errors, {@code false} otherwise
     */
    @Override
    protected Boolean doInBackground(String... strings) {
        XmlPullParser parser = initializeParser(strings[0]);
        Result result = Result.getInstance();
        String titleValue = "";
        boolean isWordSpanTag = false;
        boolean isProcessFinished = false;

        try {
            int event = parser.getEventType();
            int lineNumber = -2;

            while (event != XmlPullParser.END_DOCUMENT) {

                // Read XML tag
                String tagName = parser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:

                        String classAttribute;
                        if (tagName.equals(XML_SPAN_TAG)) {
                            // Read <span> class
                            classAttribute = parser.getAttributeValue(null, XML_CLASS_TAG);
                        } else {
                            break;
                        }

                        // If class is line
                        if (classAttribute.equals(XML_LINE_TAG)) {
                            // Read current line number
                            String ln = parser.getAttributeValue(null, XML_ID_TAG);
                            lineNumber = parseLineNumber(ln);
                        }

                        // If class is word
                        if (classAttribute.equals(XML_OCRX_TAG)) {
                            // Read <span> title (bbox, word confidence)
                            titleValue = parser.getAttributeValue(null, XML_TITLE_TAG);
                            isWordSpanTag = true;
                        }
                        break;

                    case XmlPullParser.TEXT:

                        // If <span> contains OCR'd word
                        if (isWordSpanTag) {
                            String text = parser.getText();         // read text (word)
                            Word word = new Word(text);
                            if (!word.isValid()) {
                                // Word name is most likely bullshit, don't add it
                                isWordSpanTag = false;
                                break;
                            }
                            word.setLineNumber(lineNumber);
                            int wordConfidence = parseWordConfidence(titleValue);
                            word.setConfidence(wordConfidence);
                            String bbox = parseBbox(titleValue);    // bounding box (coordinates)
                            assignPoints(bbox, word);               // assign points (coordinates)
                            result.addWord(word);                   // add Word to list
                            isWordSpanTag = false;
                        }
                        break;
                }
                event = parser.next();
            }
            isProcessFinished = true;
        } catch (Exception e) {
            // TODO: Handle different exceptions!
            e.printStackTrace();
        }

        return isProcessFinished;
    }

    @Override
    protected void onPostExecute(Boolean isHocrParsingFinished) {
        mOcrCallbacksManager.onHocrParsingComplete();
    }

    private int parseLineNumber(String id) {
        int splitIndex = id.lastIndexOf('_');
        return Integer.parseInt(id.substring(++splitIndex));
    }

    /**
     * Returns the parsed word confidence from the value of the hOCR "title" attribute. The word
     * confidence consists of a range of values between 0 and 100.
     *
     * @param titleValue value of the hOCR "title" attribute of a recognized word.
     * @return word confidence of specified word
     */
    private int parseWordConfidence(String titleValue) {
        int splitIndex = titleValue.lastIndexOf(' ');
        return Integer.parseInt(titleValue.substring(++splitIndex));
    }

    /**
     * Returns the parsed bbox (bounding box) data (coordinates).
     *
     * @param titleValue value of the hOCR "title" attribute of a recognized word.
     * @return space separated bounding box coordinates
     */
    private String parseBbox(String titleValue) {
        int coordinatesStart = titleValue.indexOf(" ");
        coordinatesStart += 1;
        int coordinatesEnd = titleValue.indexOf(";");

        return titleValue.substring(coordinatesStart, coordinatesEnd);
    }

    /**
     * Assigns bounding box coordinates as Points to the specified word.
     *
     * @param bbox space separated bounding box coordinates
     * @param word Word to assign Points to.
     */
    private void assignPoints(String bbox, Word word) {
        int[] coordinates = parseCoordinates(bbox);
        Point point1 = new Point(coordinates[0], coordinates[1]);
        Point point2 = new Point(coordinates[2], coordinates[3]);
        word.setBoundingBox(point1, point2);
    }

    /**
     * Returns an int array of size 4 containing the 4 coordinates parsed from the bbox (bounding
     * box) input.
     *
     * @param bbox space separated bounding box coordinates
     * @return int array of size 4 containing the 4 bbox coordinates
     */
    private int[] parseCoordinates(String bbox) {
        int[] coordinates = new int[4];
        int i = 0;
        for (String coordinate : bbox.split(" ")) {
            coordinates[i] = Integer.parseInt(coordinate);
            i++;
        }
        return coordinates;
    }

    /**
     * Initializes and returns a new instance of XmlPullParser. The hocrToParse argument is set as
     * the parser's input. The expected encoding is UTF-8.
     *
     * @param hocrToParse hocr data to parse from
     * @return XmlPullParser instance with the specified hocr to parse
     */
    private XmlPullParser initializeParser(String hocrToParse) {
        XmlPullParser parser = null;
        InputStream sourceStream = stringToInputStream(hocrToParse);
        try {
            parser = XmlPullParserFactory
                    .newInstance()      // new pull parser factory instance
                    .newPullParser();   // new pull parser instance
            parser.setInput(sourceStream, XML_ENCODING);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.e(TAG, "Problem initializing parser!");
            // TODO: Handle better!
        }
        return parser;
    }

    /**
     * Returns the given String as an (ByteArray) InputStream. UTF-8 decoding is used.
     *
     * @param string String to be "converted" to an InputStream
     * @return InputStream of specified String
     */
    private InputStream stringToInputStream(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }
}