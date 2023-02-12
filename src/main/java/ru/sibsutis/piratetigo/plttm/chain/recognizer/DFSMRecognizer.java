package ru.sibsutis.piratetigo.plttm.chain.recognizer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.common.Tools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Детерминированный конечный автомат, распознающий цепочки языка.
 */
@Log
@RequiredArgsConstructor
public class DFSMRecognizer {

    private final HashSet<String> states;
    private final HashSet<Character> alphabet;
    private final String startState;
    private final HashSet<String> endStates;
    private final Map<String, Map<Character, String>> transitions;
    private final LinkedList<Character> chain;
    private final ListView<String> process;

    private String currentState;

    /**
     * Запускает процесс распознавания цепочки языка.
     */
    public void recognize() throws RecognizeException {
        logInputParams();
        currentState = startState;
        log("Начинаем процесс распознавания");
        Map<Character, String> cases;
        Character nextSymbol;
        while (true) {
            if (chain.isEmpty()) {
                break;
            }
            logState(false);
            nextSymbol = chain.removeFirst();
            if (currentState == null || currentState.isBlank()) {
                createError("Состояние ДКА не определено.");
            }
            cases = transitions.get(currentState);
            if (cases != null && cases.containsKey(nextSymbol)) {
                currentState = cases.get(nextSymbol);
            } else {
                createError("В цепочке присутствуют посторонние символы.");
            }
        }
        logState(true);
        if (!endStates.contains(currentState)) {
            createError("После прочтения цепочки ДКА не пришёл в конечное состояние.");
        }
        log("Процесс распознавания завершен");
    }

    private void logInputParams() {
        StringBuilder msg = new StringBuilder();
        msg.append("Параметры ДКА:\n\n");
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
        msg.append("Функция переходов:\n");
        transitions.forEach((state, ways) -> {
            msg.append(state).append(": [ ");
            ways.forEach((letter, newState) ->
                    msg.append(letter).append(" => ")
                            .append(newState).append(", "));
            msg.append("]\n");
        });
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
        log(String.format(
                "(%s, %s)" + (isFinal ? "" : " " + Tools.RULE),
                currentState,
                restChain.isEmpty() ? Tools.LAMBDA : restChain
        ));
    }

    private void createError(String msg) throws RecognizeException {
        log("Ошибка");
        throw new RecognizeException(msg);
    }

}
