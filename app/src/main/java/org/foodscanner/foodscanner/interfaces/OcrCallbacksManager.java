package org.foodscanner.foodscanner.interfaces;

/**
 * Interface definitions for a OCR-related callbacks.
 */
public interface OcrCallbacksManager {

    void setOcrProgressPercentage(int percentage);

    /**
     * Called when the OCR process has returned a result (onPostExecute). Passes the result to
     * the listening class.
     *
     * @param hocrText OCR result in hocr ("xml") form
     */
    void onOcrCompleted(String hocrText);

    void onOcrCanceled();

    void onHocrParsingComplete();

    void onAdditiveRecognitionCompleted(boolean wereAdditivesRecognized);
}
