package ru.sibsutis.piratetigo.plttm.chain;

/**
 * Выбрасывается при превышении длины цепочки заданного значения.
 */
public class ChainLengthExceededException extends RuntimeException {
    public ChainLengthExceededException(String msg) {
        super(msg);
    }
}
