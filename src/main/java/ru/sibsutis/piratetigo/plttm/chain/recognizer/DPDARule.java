package ru.sibsutis.piratetigo.plttm.chain.recognizer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Правило функции переходов детерминированного конечного
 * автомата с магазинной памятью, распознающего цепочки
 * языка.
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DPDARule {
    private final String fromState;
    private final Character chainSymbol;
    private final Character fromStackTop;
    private final String toState;
    private final String toStackTop;

    @Override
    public String toString() {
        return "δ(" + fromState + "," + chainSymbol + "," + fromStackTop +
                ")={(" + toState + "," + toStackTop + ")}";
    }
}
