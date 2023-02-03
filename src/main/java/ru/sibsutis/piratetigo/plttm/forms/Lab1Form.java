package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.chain.Generator;
import ru.sibsutis.piratetigo.plttm.common.InferenceType;

import static ru.sibsutis.piratetigo.plttm.chain.Generator.LAMBDA;

/**
 * Контроллер формы вкладки "Лабораторная работа №1".
 */
@Log
public class Lab1Form {

    private final static Character ARROW = '→';
    private final static String COUNTER_DEFAULT = "Всего цепочек: 0";

    /** Введенные пользователем терминальные символы. */
    @FXML
    TextArea userTerminals;

    /** Введенные пользователем нетерминальные символы. */
    @FXML
    TextArea userNonTerminals;

    /** Целевой символ. */
    @FXML
    ComboBox<Character> goalCharacter;

    /** Порождающий нетерминальный символ правила. */
    @FXML
    ComboBox<Character> generatingSymbol;

    /** Вводимое пользователем правило вывода КС-грамматики. */
    @FXML
    TextField rule;

    /** Кнопка добавления правила в список правил вывода. */
    @FXML
    Button addRule;

    /** Список правил вывода. */
    @FXML
    ListView<String> rules;

    /** Кнопка ввода символа пустой цепочки. */
    @FXML
    Button lambda;

    /** Кнопка удаления правила из списка правил вывода. */
    @FXML
    Button deleteRule;

    /** Управляющий элемент левой границы диапазона длин генерируемых цепочек. */
    @FXML
    Spinner<Integer> chainSizeFrom;

    /** Управляющий элемент правой границы диапазона длин генерируемых цепочек. */
    @FXML
    Spinner<Integer> chainSizeTo;

    /** Переключатель типа вывода. */
    @FXML
    Button inferenceType;

    /** Кнопка запуска вывода. */
    @FXML
    Button start;

    /** Вывод цепочек. */
    @FXML
    ListView<String> inference;

    /** Счетчик числа сгенерированных цепочек. */
    @FXML
    Label chainCount;

    /** Кнопка сброса всех полей. */
    @FXML
    Button reset;

    /** Вычисленное множество терминальных символов. */
    private HashSet<Character> terminals = new HashSet<>();

    /** Вычисленное множество нетерминальных символов. */
    private HashSet<Character> nonTerminals = new HashSet<>();

    @FXML
    private void initialize() {
        // Обработчик пользовательского ввода терминальных символов
        userTerminals.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        userTerminals.setText(oldValue);
                    } else {
                        terminals = calculateAlphabet(
                                newValue.trim(),
                                terminals,
                                userTerminals,
                                nonTerminals
                        );
                        rule.textProperty().set("");
                        rules.getItems().clear();
                    }
                }
        );

        // Обработчик пользовательского ввода нетерминальных символов
        userNonTerminals.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        userNonTerminals.setText(oldValue);
                    } else {
                        nonTerminals = calculateAlphabet(
                                newValue.trim(),
                                nonTerminals,
                                userNonTerminals,
                                terminals
                        );
                        goalCharacter.getItems().setAll(nonTerminals);
                        generatingSymbol.getItems().setAll(nonTerminals);
                        rule.textProperty().set("");
                        rules.getItems().clear();
                    }
                }
        );

        // Обработчик выбора целевого символа
        goalCharacter.setOnAction(event ->toggleStartButton());

        // Обработчик пользовательского ввода порождающего символа правила вывода
        generatingSymbol.setOnAction(event -> toggleAddRuleButton());

        // Обработчик пользовательского ввода символов правила вывода
        rule.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > oldValue.length()) {
                if (oldValue.equals(LAMBDA.toString())) {
                    rule.textProperty().set(LAMBDA.toString());
                    return;
                }
            }
            HashSet<Character> allSymbols = new HashSet<>();
            allSymbols.addAll(terminals);
            allSymbols.addAll(nonTerminals);
            allSymbols.add(LAMBDA);
            boolean isCorrectInput = true;
            for (Character character: newValue.toCharArray()) {
                if (!allSymbols.contains(character)) {
                    isCorrectInput = false;
                    break;
                }
            }
            if (!isCorrectInput) {
                rule.textProperty().set(oldValue);
            }
            toggleAddRuleButton();
        });

        // Обработчик кнопки символа пустой цепочки
        lambda.setOnAction(event -> rule.textProperty().set(LAMBDA.toString()));

        // Обработчик кнопки добавления нового правила в список правил вывода
        addRule.setOnAction(event -> {
            String item = generatingSymbol.getValue() +
                    ARROW.toString() +
                    rule.getText();
            if (!rules.getItems().contains(item)) {
                rules.getItems().add(item);
                rule.setText("");
            }
            rule.requestFocus();
        });

        // Обработчик выделения элемента списка правил вывода
        rules.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        deleteRule.setDisable(false));

        // Обработчик кнопки удаления выбранного правила из списка правил вывода
        deleteRule.setOnAction(event -> rules.getItems().removeAll(
                rules.getSelectionModel().getSelectedItems()
        ));

        // Обработчик изменения количества элементов в списке правил вывода
        rules.getItems().addListener((ListChangeListener<String>) c -> {
            toggleStartButton();
            if (rules.getItems().size() == 0) {
                deleteRule.setDisable(true);
            }
        });

        // Обработчик управляющего элемента левой границы диапазона длин цепочек
        chainSizeFrom.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue > chainSizeTo.getValue()) {
                chainSizeFrom.getValueFactory().setValue(oldValue);
            }
        });

        // Обработчик управляющего элемента правой границы диапазона длин цепочек
        chainSizeTo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue < chainSizeFrom.getValue()) {
                chainSizeTo.getValueFactory().setValue(oldValue);
            }
        });

        // Обработчик переключателя типа вывода
        inferenceType.setOnAction(event -> {
            if (inferenceType.getText().equals(InferenceType.LEFT.getValue())) {
                inferenceType.setText(InferenceType.RIGHT.getValue());
            } else {
                inferenceType.setText(InferenceType.LEFT.getValue());
            }
        });

        // Обработчик кнопки запуска генерации цепочек
        start.setOnAction(event -> {
            setAllDisable(true);

            HashMap<Character, List<String>> srcRules = new HashMap<>();
            rules.getItems().forEach(item ->
                    srcRules.computeIfAbsent(item.charAt(0), k -> new ArrayList<>())
                            .add(item.substring(2)));
            logGrammar(srcRules, goalCharacter.getValue());
            InferenceType type = InferenceType.from(inferenceType.getText());
            inference.getItems().clear();
            chainCount.setText(COUNTER_DEFAULT);

            Thread chainGenerationTask = new Thread(new Task<List<String>>() {
                private List<String> chains = new ArrayList<>();

                @Override
                protected List<String> call() {
                    Generator generator = new Generator(
                            srcRules,
                            goalCharacter.getValue(),
                            type,
                            chainSizeFrom.getValue(),
                            chainSizeTo.getValue(),
                            terminals,
                            nonTerminals
                    );
                    generator.generate();
                    chains = generator.getChains();
                    return chains;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        inference.getItems().setAll(chains);
                        chainCount.setText("Всего цепочек: " + chains.size());
                        setAllDisable(false);
                    });
                }
            });

            chainGenerationTask.start();
        });

        // Обработчик кнопки сброса всех полей
        reset.setOnAction(event -> {
            userTerminals.textProperty().set("");
            userNonTerminals.textProperty().set("");
            inference.getItems().clear();
            chainCount.setText(COUNTER_DEFAULT);
        });

        rules.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private boolean isUnique(String value, HashSet<Character> alphabet) {
        char[] characters = value.toCharArray();
        for (Character character: characters) {
            if (!alphabet.add(character)) {
                return false;
            }
        }
        return true;
    }

    private HashSet<Character> calculateAlphabet(
            String newValue,
            HashSet<Character> oldAlphabet,
            TextArea textArea,
            HashSet<Character> excluded) {
        HashSet<Character> alphabet = new HashSet<>();
        if (isUnique(newValue, alphabet) && !areIntersected(alphabet, excluded)) {
            return alphabet;
        }
        textArea.textProperty().set(
                oldAlphabet.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining())
        );
        return oldAlphabet;
    }

    private boolean areIntersected(Set<Character> left, Set<Character> right) {
        HashSet<Character> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection.size() != 0;
    }

    private void toggleAddRuleButton() {
        addRule.setDisable(
                generatingSymbol.getValue() == null || rule.getText().equals("")
        );
    }

    private void toggleStartButton() {
        start.setDisable(
                rules.getItems().size() == 0 || goalCharacter.getValue() == null
        );
    }

    private void logGrammar(HashMap<Character, List<String>> srcRules, Character goal) {
        StringJoiner stringedRules = new StringJoiner(
                ", ",
                "Целевой символ: " + goal + "\nИсходные правила: [",
                "]"
        );
        srcRules.forEach((left, right) -> {
            StringJoiner oneRule = new StringJoiner(
                    "|",
                    left.toString() + "=>",
                    ""
            );
            for (String rule: right) {
                oneRule.add(rule.replace(LAMBDA.toString(), "<lambda>"));
            }
            stringedRules.add(oneRule.toString());
        });
        LOGGER.info(stringedRules.toString());
    }

    private void setAllDisable(boolean disable) {
        userTerminals.setDisable(disable);
        userNonTerminals.setDisable(disable);
        goalCharacter.setDisable(disable);
        generatingSymbol.setDisable(disable);
        rule.setDisable(disable);
        rules.setDisable(disable);
        lambda.setDisable(disable);
        chainSizeFrom.setDisable(disable);
        chainSizeTo.setDisable(disable);
        inferenceType.setDisable(disable);
        start.setDisable(disable);
        reset.setDisable(disable);
    }

}
