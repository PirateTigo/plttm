package ru.sibsutis.piratetigo.plttm.chain;

/**
 * Выбрасывается при необходимости пропуска правила генерации цепочек.
 */
public class MissRuleException extends RuntimeException {
    public MissRuleException(String msg) {
        super(msg);
    }
}
