package ru.sibsutis.piratetigo.plttm.chain;

/**
 * Выбрасывается при обнаружении зацикливания при генерации цепочек.
 */
public class LoopDetectionException extends RuntimeException {
    private final Character recursiveNonTerminal;
    public LoopDetectionException(String msg, Character recursiveNonTerminal) {
        super(msg);
        this.recursiveNonTerminal = recursiveNonTerminal;
    }

    public Character getRecursiveNonTerminal() {
        return recursiveNonTerminal;
    }

}
