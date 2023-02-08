package ru.sibsutis.piratetigo.plttm.chain.recognizer;

/**
 * Возникает в случае ошибки распознавания цепочки языка.
 */
public class RecognizeException extends Throwable {
    public RecognizeException(String msg) {
        super(msg);
    }
}
