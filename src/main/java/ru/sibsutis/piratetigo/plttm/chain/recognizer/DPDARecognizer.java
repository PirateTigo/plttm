package ru.sibsutis.piratetigo.plttm.chain.recognizer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.common.Tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Детерминированный конечный автомат с магазинной памятью,
 * распознающий цепочки языка.
 */
@Log
@RequiredArgsConstructor
public class DPDARecognizer {

    private final Set<String> states;
    private final Set<Character> alphabet;
    private final String startState;
    private final Set<String> endStates;
    private final Set<Character> stackAlphabet;
    private final Character startStackSymbol;
    private final Set<DPDARule> rules;
    private final LinkedList<Character> chain;
    private final ListView<String> process;

    private String currentState;
    private final LinkedList<Character> stack = new LinkedList<>();

    /**
     * Запускает процесс распознавания цепочки языка.
     */
    public void recognize() throws RecognizeException {
        logInputParams();
        currentState = startState;
        stack.clear();
        stack.push(startStackSymbol);
        log("Начинаем процесс распознавания");
        while (true) {
            if (stack.isEmpty()) {
                break;
            }
            logState(false);
            if (chain.isEmpty()) {
                processRule(getRule(currentState, Tools.LAMBDA, stack.peek()));
            } else {
                processRule(getRule(currentState, chain.peekFirst(), stack.peek()));
            }
        }
        logState(true);
        if (!endStates.contains(currentState) || !chain.isEmpty()) {
            createError("После прочтения цепочки ДМПА не пришёл в конечное состояние.");
        }
        log("Процесс распознавания завершен");
    }

    private void logInputParams() {
        StringBuilder msg = new StringBuilder();
        msg.append("Параметры ДМПА:\n\n");
        msg.append("Множество состояний: ");
        states.forEach(state -> msg.append(state).append(", "));
        msg.append("\n");
        msg.append("Алфавит языка: ");
        alphabet.forEach(letter -> msg.append(letter).append(", "));
        msg.append("\n");
        msg.append("Начальное состояние: ");
        msg.append(startState);
        msg.append("\n");
        msg.append("Множество конечных состояний: ");
        endStates.forEach(state -> msg.append(state).append(", "));
        msg.append("\n");
        msg.append("Алфавит магазина: ");
        stackAlphabet.forEach(letter -> msg.append(letter).append(", "));
        msg.append("\n");
        msg.append("Начальное содержимое стека: ");
        msg.append(startStackSymbol);
        msg.append("\n");
        msg.append("Функция переходов: ");
        msg.append(rules.stream()
                .map(rule ->
                        rule.toString()
                                .replace("δ", "d")
                                .replace(Tools.LAMBDA.toString(), "<lambda>"))
                .collect(Collectors.joining(",\n", "[\n", "\n]\n")));
        msg.append("Проверяемая цепочка: ");
        chain.forEach(msg::append);
        LOGGER.info(msg.toString());
    }

    private void log(String msg) {
        Platform.runLater(() ->
                process.getItems().add(msg));
    }

    private void logState(boolean isFinal) {
        String restChain = chain.stream().collect(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append
        ).toString();
        String currentStack = stack.stream().collect(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append
        ).toString();
        log(String.format(
                "(%s, %s, %s)" + (isFinal ? "" : " " + Tools.RULE),
                currentState,
                restChain.isEmpty() ? Tools.LAMBDA : restChain,
                currentStack.isEmpty() ? Tools.LAMBDA : currentStack
        ));
    }

    private void createError(String msg) throws RecognizeException {
        log("Ошибка");
        throw new RecognizeException(msg);
    }

    private DPDARule getRule(String state, Character chainSymbol, Character stackSymbol)
            throws RecognizeException {
        if (!alphabet.contains(chainSymbol) && !chainSymbol.equals(Tools.LAMBDA)) {
            createError("В цепочке присутствуют посторонние символы");
        }
        List<DPDARule> calculatedRules = rules.stream()
                .filter(rule -> rule.getFromState().equals(state)
                        && (rule.getChainSymbol().equals(chainSymbol)
                            || rule.getChainSymbol().equals(Tools.LAMBDA))
                        && rule.getFromStackTop().equals(stackSymbol))
                .collect(Collectors.toList());
        if (calculatedRules.size() == 0) {
            createError("Поведение ДМПА не определено");
        }
        if (calculatedRules.size() == 2) {
            // Устанавливаем приоритетный выбор обычного правила над λ-правилом
            calculatedRules = calculatedRules.stream()
                    .filter(rule -> rule.getChainSymbol().equals(chainSymbol))
                    .collect(Collectors.toList());
        }
        if (calculatedRules.size() > 1) {
            createError("Поведение ДМПА недетерминированно");
        }
        return calculatedRules.get(0);
    }

    private void processRule(DPDARule rule) throws RecognizeException {
        if (!rule.getChainSymbol().equals(Tools.LAMBDA)) {
            if (chain.isEmpty()) {
                createError("ДМПА может использовать только при пустой цепочке");
            }
            chain.removeFirst();
        }
        if (rule.getToStackTop().equals(Tools.LAMBDA.toString())) {
            // Удаляем верхний символ стека
            stack.pop();
        } else if (rule.getFromStackTop()
                .equals(rule.getToStackTop().charAt(rule.getToStackTop().length() - 1))) {
            // Дописываем в стек цепочку символов
            for (int i = rule.getToStackTop().length() - 2; i >= 0; i--) {
                stack.push(rule.getToStackTop().charAt(i));
            }
        } else {
            // Заменяем вершину стека на цепочку символов
            stack.pop();
            for (int i = rule.getToStackTop().length() - 1; i >= 0; i--) {
                stack.push(rule.getToStackTop().charAt(i));
            }
        }
        currentState = rule.getToState();
    }

}
