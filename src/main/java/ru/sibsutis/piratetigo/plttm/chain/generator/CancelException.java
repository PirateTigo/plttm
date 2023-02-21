package ru.sibsutis.piratetigo.plttm.chain.generator;

/**
 * Используется для остановки процесса вычислений.
 */
public class CancelException extends RuntimeException {
    public CancelException(String message) {
        super(message);
    }
}
