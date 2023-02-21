package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.chain.generator.Generator;
import ru.sibsutis.piratetigo.plttm.common.InferenceType;
import ru.sibsutis.piratetigo.plttm.grammar.ContextFreeGrammar;

import static ru.sibsutis.piratetigo.plttm.common.Tools.*;

/**
 * Контроллер формы вкладки "Лабораторная работа №1".
 */
@Log
public class Lab1Form {

    /** Введенные пользователем терминальные символы. */
    @FXML
    TextArea userTerminals;

    /** Введенные пользователем нетерминальные символы. */
    @FXML
    TextArea userNonTerminals;

    /** Целевой символ. */
    @FXML
    ComboBox<String> goalCharacter;

    /** Порождающий нетерминальный символ правила. */
    @FXML
    ComboBox<String> generatingSymbol;

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

    /** Кнопка отмены процесса вывода. */
    @FXML
    Button cancel;

    /** Вывод цепочек. */
    @FXML
    ListView<String> inference;

    /** Счетчик числа сгенерированных цепочек. */
    @FXML
    Label chainCount;

    /** Кнопка сброса всех полей. */
    @FXML
    Button reset;

    /** Контекстно-свободная грамматика. */
    private ContextFreeGrammar contextFreeGrammar;

    /** Признак возможности добавления текущего введенного правила вывода в список. */
    private boolean isCorrectRule = false;

    /** Генератор цепочек. */
    private Generator generator;

    @FXML
    private void initialize() {
        contextFreeGrammar = new ContextFreeGrammar(
                userTerminals,
                userNonTerminals,
                goalCharacter,
                rules
        );

        // Обработчик пользовательского ввода терминальных символов
        userTerminals.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        userTerminals.setText(oldValue);
                    } else {
                        contextFreeGrammar.setTerminals(newValue.trim());
                        rule.textProperty().set("");
                        rules.getItems().clear();
                        contextFreeGrammar.getRules().clear();
                    }
                }
        );

        // Обработчик пользовательского ввода нетерминальных символов
        userNonTerminals.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.contains(LESS_THAN.toString())
                        || newValue.contains(GREATER_THAN.toString())
                        || newValue.trim().equals(oldValue)) {
                        userNonTerminals.setText(oldValue);
                    } else {
                        contextFreeGrammar.setNonTerminals(newValue.trim());
                        goalCharacter.getItems().setAll(
                                contextFreeGrammar.getNonTerminals()
                        );
                        generatingSymbol.getItems().setAll(
                                contextFreeGrammar.getNonTerminals()
                        );
                        rule.textProperty().set("");
                        rules.getItems().clear();
                        contextFreeGrammar.getRules().clear();
                    }
                }
        );

        // Обработчик выбора целевого символа
        goalCharacter.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    toggleStartButton();
                    if (goalCharacter.getValue() != null) {
                        contextFreeGrammar.setGoal(goalCharacter.getValue());
                    }
                });

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
            HashSet<String> allLexemes = new HashSet<>();
            allLexemes.addAll(symbolsToStrings(
                    contextFreeGrammar.getTerminals()
            ));
            allLexemes.addAll(
                    contextFreeGrammar.getNonTerminals()
            );
            allLexemes.add(LAMBDA.toString());
            boolean isCorrectInput = true;
            isCorrectRule = true;
            try {
                for (String lexeme : stringToLexemes(newValue)) {
                    if (lexeme.charAt(0) == LESS_THAN) {
                        if (!setContainsLexeme(
                                lexeme,
                                contextFreeGrammar.getNonTerminals(),
                                true)) {
                            isCorrectInput = false;
                            break;
                        }
                        continue;
                    }
                    if (!setContainsLexeme(lexeme, allLexemes, false)) {
                        isCorrectInput = false;
                        break;
                    }
                }
            } catch (IllegalArgumentException ex) {
                isCorrectRule = false;
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
            String item =
                    generatingSymbol.getValue() + ARROW + rule.getText();
            if (!rules.getItems().contains(item)) {
                rules.getItems().add(item);
                rule.setText("");
                contextFreeGrammar.addRule(item);
            }
            rule.requestFocus();
        });

        // Обработчик выделения элемента списка правил вывода
        rules.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        deleteRule.setDisable(false));

        // Обработчик кнопки удаления выбранного правила из списка правил вывода
        deleteRule.setOnAction(event -> {
            rules.getItems().removeAll(
                    rules.getSelectionModel().getSelectedItems()
            );
            contextFreeGrammar.getRules().clear();
            rules.getItems().forEach(rule -> contextFreeGrammar.addRule(rule));
        });

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

            contextFreeGrammar.log();
            InferenceType type = InferenceType.from(inferenceType.getText());
            inference.getItems().clear();
            chainCount.setText(COUNTER_DEFAULT);

            generator = new Generator(
                    contextFreeGrammar.getRules(),
                    goalCharacter.getValue(),
                    type,
                    chainSizeFrom.getValue(),
                    chainSizeTo.getValue(),
                    symbolsToStrings(
                            contextFreeGrammar.getTerminals()
                    )
            );

            Thread chainGenerationTask = new Thread(new Task<List<String>>() {
                private List<String> chains = new ArrayList<>();

                @Override
                protected List<String> call() {
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

        // Обработчик кнопки отмены генерации цепочек
        cancel.setOnAction(event -> {
            if (generator != null && generator.isRunning()) {
                generator.cancel();
            }
        });

        // Обработчик кнопки сброса всех полей
        reset.setOnAction(event -> {
            userTerminals.textProperty().set("");
            userNonTerminals.textProperty().set("");
            inference.getItems().clear();
            chainCount.setText(COUNTER_DEFAULT);
            userTerminals.requestFocus();
        });

        rules.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void toggleAddRuleButton() {
        addRule.setDisable(
                generatingSymbol.getValue() == null
                        || rule.getText().equals("")
                        || !isCorrectRule
        );
    }

    private void toggleStartButton() {
        start.setDisable(
                rules.getItems().size() == 0
                        || goalCharacter.getValue() == null
        );
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
