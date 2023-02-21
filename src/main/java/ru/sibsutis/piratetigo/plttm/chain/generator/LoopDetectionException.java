package ru.sibsutis.piratetigo.plttm.chain.generator;

/**
 * Выбрасывается при обнаружении зацикливания при генерации цепочек.
 */
public class LoopDetectionException extends RuntimeException {
    private final String recursiveNonTerminal;
    public LoopDetectionException(String msg, String recursiveNonTerminal) {
        super(msg);
        this.recursiveNonTerminal = recursiveNonTerminal;
    }

    public String getRecursiveNonTerminal() {
        return recursiveNonTerminal;
    }

}
