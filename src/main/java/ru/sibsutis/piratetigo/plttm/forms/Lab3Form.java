package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.chain.recognizer.DPDARecognizer;
import ru.sibsutis.piratetigo.plttm.chain.recognizer.DPDARule;
import ru.sibsutis.piratetigo.plttm.chain.recognizer.RecognizeException;

import java.util.HashSet;
import java.util.LinkedList;

import static ru.sibsutis.piratetigo.plttm.common.Tools.*;

/**
 * Контроллер формы вкладки "Лабораторная работа №3".
 */
@Log
public class Lab3Form {

    /** Введенное пользователем множество состояний ДМПА. */
    @FXML
    TextArea userStates;

    /** Введенный пользователем алфавит языка. */
    @FXML
    TextArea userAlphabet;

    /** Начальное состояние ДМПА. */
    @FXML
    ComboBox<String> startState;

    /** Множество заключительных состояний ДМПА. */
    @FXML
    ListView<String> userEndStates;

    /** Введенный пользователем алфавит магазина ДМПА. */
    @FXML
    TextArea userStackAlphabet;

    /** Начальный символ магазина ДМПА. */
    @FXML
    ComboBox<Character> startStackSymbol;

    /** Исходное состояние правила функции переходов. */
    @FXML
    ComboBox<String> fromState;

    /** Новое состояние правила функции переходов. */
    @FXML
    ComboBox<String> toState;

    /** Текущий символ на ленте для правила функции переходов. */
    @FXML
    TextField chainSymbol;

    /** Текущий символ на вершине стека для правила функции переходов. */
    @FXML
    TextField stackTop;

    /** Признак необходимости совершения λ-такта для правила функции переходов. */
    @FXML
    CheckBox ignoreChain;

    /** Удалить из стека верхний символ - вариант для правила функции переходов. */
    @FXML
    RadioButton stackBehaviour1;

    /** Оставить содержимое стека без изменений - вариант для правила функции переходов. */
    @FXML
    RadioButton stackBehaviour2;

    /** Дописать символы в стек - вариант для правила функции переходов. */
    @FXML
    RadioButton stackBehaviour3;

    /** Заменить вершину стека символами - вариант для правила функции переходов. */
    @FXML
    RadioButton stackBehaviour4;

    /** Символы, помещаемые в стек для правила функции переходов. */
    @FXML
    TextField stackSymbols;

    /** Кнопка добавления правила функции переходов. */
    @FXML
    Button add;

    /** Список отображаемых пользователю правил функции переходов. */
    @FXML
    ListView<String> userRules;

    /** Кнопка удаления правила функции переходов. */
    @FXML
    Button delete;

    /** Введенная пользователем цепочка символов языка. */
    @FXML
    TextArea userChain;

    /** Кнопка запуска процесса распознавания цепочки символов. */
    @FXML
    Button test;

    /** Окно вывода информации во время процесса распознавания цепочки. */
    @FXML
    ListView<String> process;

    /** Результирующее сообщение процесса распознавания. */
    @FXML
    Label result;

    /** Описание ошибки. */
    @FXML
    Label description;

    /** Кнопка сброса. */
    @FXML
    Button reset;

    /** Вычисленное множество состояний ДМПА. */
    private HashSet<String> states = new HashSet<>();

    /** Вычисленный алфавит языка. */
    private HashSet<Character> alphabet = new HashSet<>();

    /** Вычисленное множество заключительных состояний. */
    private final HashSet<String> endStates = new HashSet<>();

    /** Вычисленный алфавит магазина ДМПА. */
    private HashSet<Character> stackAlphabet = new HashSet<>();

    /** Группа радиокнопок - вариантов для правила функции переходов. */
    private ToggleGroup stackBehaviours;

    /** Список правил функции переходов. */
    private final HashSet<DPDARule> rules = new HashSet<>();

    /** Проверяемая на принадлежность алфавиту языка цепочка символов. */
    private LinkedList<Character> chain = new LinkedList<>();

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank();
    }

    @FXML
    private void initialize() {
        // Обработчик пользовательского ввода множества состояний
        userStates.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        userStates.setText(oldValue);
                    } else {
                        HashSet<String> excluded = new HashSet<>();
                        states = calculateUniqueItems(
                                newValue.trim(),
                                states,
                                userStates,
                                excluded
                        );
                        startState.getItems().setAll(states);
                        userEndStates.getItems().setAll(states);
                        fromState.getItems().setAll(states);
                        toState.getItems().setAll(states);

                        userRules.getItems().clear();
                        rules.clear();
                        toggleTestButton();
                        toggleAddButton();
                    }
                });

        // Обработчик пользовательского ввода алфавита языка
        userAlphabet.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        userAlphabet.setText(oldValue);
                    } else {
                        HashSet<Character> excluded = new HashSet<>();
                        alphabet = calculateAlphabet(
                                newValue.trim(),
                                alphabet,
                                userAlphabet,
                                excluded
                        );

                        userRules.getItems().clear();
                        rules.clear();
                        toggleTestButton();
                        chainSymbol.clear();
                        toggleAddButton();
                    }
                });

        // Обработчик автоматического изменения длины элемента управления начальным состоянием
        startState.widthProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.longValue() > userStates.widthProperty().longValue()) {
                        startState.setPrefWidth(userStates.widthProperty().longValue());
                    }
                });

        // Обработчик выбора начального состояния
        startState.valueProperty()
                .addListener((observable, oldValue, newValue) -> toggleTestButton());

        userEndStates.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Обработчик выбора заключительных состояний
        userEndStates.setOnMouseClicked(event -> {
            endStates.clear();
            endStates.addAll(userEndStates.getSelectionModel().getSelectedItems());
            toggleTestButton();
        });

        // Обработчик пользовательского ввода алфавита магазина ДМПА
        userStackAlphabet.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        userStackAlphabet.setText(oldValue);
                    } else {
                        HashSet<Character> excluded = new HashSet<>();
                        stackAlphabet = calculateAlphabet(
                                newValue.trim(),
                                stackAlphabet,
                                userStackAlphabet,
                                excluded
                        );
                        startStackSymbol.getItems().setAll(stackAlphabet);

                        userRules.getItems().clear();
                        rules.clear();
                        toggleTestButton();
                        stackTop.clear();
                        stackSymbols.clear();
                        toggleAddButton();
                    }
                });

        // Обработчик автоматического изменения длины элемента управления начальным символом стека
        startStackSymbol.widthProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.longValue() > userStackAlphabet.widthProperty().longValue()) {
                        startStackSymbol.setPrefWidth(userStackAlphabet.widthProperty().longValue());
                    }
                });

        // Обработчик выбора начального символа стека
        startStackSymbol.setOnMouseClicked(event -> toggleTestButton());

        // Обработчик выбора исходного состояния правила функции переходов
        fromState.valueProperty()
                .addListener((observable, oldValue, newValue) -> toggleAddButton());

        // Обработчик автоматического изменения длины элемента управления исходным состоянием
        // правила функции переходов
        fromState.widthProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.longValue() > userStates.widthProperty().longValue()) {
                        fromState.setPrefWidth(userStates.widthProperty().longValue());
                    }
                });

        // Обработчик выбора нового состояния правила функции переходов
        toState.valueProperty()
                .addListener((observable, oldValue, newValue) -> toggleAddButton());

        // Обработчик автоматического изменения длины элемента управления новым состоянием
        // правила функции переходов
        toState.widthProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.longValue() > userStates.widthProperty().longValue()) {
                        toState.setPrefWidth(userStates.widthProperty().longValue());
                    }
                });

        // Обработчик пользовательского ввода текущего символа на ленте
        // для правила функции переходов
        chainSymbol.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        chainSymbol.setText(oldValue);
                    } else {
                        if (!newValue.isBlank()) {
                            if (newValue.length() > 1
                                    || !alphabet.contains(newValue.charAt(0))) {
                                chainSymbol.setText(oldValue);
                                return;
                            }
                        }
                        toggleAddButton();
                    }
                });

        // Обработчик пользовательского ввода текущей вершины стека
        // для правила функции переходов
        stackTop.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        stackTop.setText(oldValue);
                    } else {
                        if (!newValue.isBlank()) {
                            if (newValue.length() > 1
                                    || !stackAlphabet.contains(newValue.charAt(0))) {
                                stackTop.setText(oldValue);
                                return;
                            }
                        }
                        toggleAddButton();
                    }
                });

        // Обработчик чек-бокса λ-такта
        ignoreChain.setOnAction(event -> chainSymbol.clear());

        // Обработчик пользовательского ввода помещаемых в стек символов
        // для правила функции переходов
        stackSymbols.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.trim().equals(oldValue)) {
                        stackSymbols.setText(oldValue);
                    } else {
                        if (!newValue.isBlank()) {
                            if (!stackAlphabet.contains(newValue.charAt(newValue.length() - 1))) {
                                stackSymbols.setText(oldValue);
                                return;
                            }
                        }
                        toggleAddButton();
                    }
                });

        stackBehaviours = new ToggleGroup();
        stackBehaviour1.setToggleGroup(stackBehaviours);
        stackBehaviour2.setToggleGroup(stackBehaviours);
        stackBehaviour3.setToggleGroup(stackBehaviours);
        stackBehaviour4.setToggleGroup(stackBehaviours);

        // Обработчик выбора варианта для правила функции переходов
        stackBehaviours.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    toggleStackBehaviour((RadioButton) newValue);
                    toggleAddButton();
                }
        );

        stackBehaviours.selectToggle(stackBehaviour1);

        // Обработчик нажатия на кнопку добавления правила функции переходов
        add.setOnAction(event -> {
            addRule();
            showRules();
            toggleTestButton();
        });

        // Обработчик изменения размера списка правил функции переходов
        userRules.getItems().addListener(
                (ListChangeListener<String>) c -> {
                    if (c.getList().size() == 0) {
                        delete.setDisable(true);
                    }
        });

        // Обработчик выделения элемента списка правил функции переходов
        userRules.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> delete.setDisable(false));

        // Обработчик нажатия на кнопку удаления правила функции переходов
        delete.setOnAction(event -> {
            String userRule = userRules.getSelectionModel().getSelectedItem();
            DPDARule rule = rules.stream()
                    .filter(item -> item.toString().equals(userRule))
                    .findFirst()
                    .orElse(null);
            if (rule != null) {
                rules.remove(rule);
                userRules.getItems().remove(userRule);
            }
            toggleTestButton();
        });

        // Обработчик ввода цепочки символов
        userChain.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().equals(oldValue)) {
                userChain.setText(oldValue);
            } else {
                chain = newValue.chars().collect(
                        LinkedList::new,
                        (list, item) -> list.add((char)item),
                        LinkedList::addAll
                );
                LOGGER.info("Пользователь ввел цепочку: "
                        + chain.stream()
                        .map(Object::toString)
                        .reduce("", String::concat));
            }
        });

        // Обработчик кнопки запуска процесса распознавания цепочки языка
        test.setOnAction(event -> {
            setAllDisable(true);
            process.getItems().clear();
            Thread recognizeThread = new Thread(new Task<Void>() {
                private String resultMsg;
                private String descriptionMsg;

                @Override
                protected Void call() {
                    DPDARecognizer dpdaRecognizer = new DPDARecognizer(
                            states,
                            alphabet,
                            startState.getValue(),
                            endStates,
                            stackAlphabet,
                            startStackSymbol.getValue(),
                            rules,
                            new LinkedList<>(chain),
                            process
                    );
                    try {
                        dpdaRecognizer.recognize();
                        resultMsg = "Цепочка принадлежит языку.";
                    } catch (RecognizeException e) {
                        resultMsg = "Цепочка не принадлежит языку.";
                        descriptionMsg = e.getMessage();
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        result.setText(resultMsg);
                        description.setText(descriptionMsg);
                        setAllDisable(false);
                        toggleAddButton();
                        toggleTestButton();
                        toggleStackBehaviour(
                                (RadioButton) stackBehaviours.getSelectedToggle()
                        );
                    });
                }
            });

            recognizeThread.start();
        });

        // Обработчик кнопки сброса значений
        reset.setOnAction(event -> {
            userStates.setText("");
            userAlphabet.setText("");
            userStackAlphabet.setText("");
        });
    }

    private void toggleStackBehaviour(RadioButton radioButton) {
        if (radioButton.equals(stackBehaviour3)
                || radioButton.equals(stackBehaviour4)) {
            stackSymbols.setDisable(false);
        } else {
            stackSymbols.clear();
            stackSymbols.setDisable(true);
        }
    }

    private void toggleAddButton() {
        add.setDisable(
                isNullOrEmpty(fromState.getValue())
                || isNullOrEmpty(toState.getValue())
                || (isNullOrEmpty(chainSymbol.getText()) && !ignoreChain.isSelected())
                || isNullOrEmpty(stackTop.getText())
                || ((stackBehaviour3.isSelected() || stackBehaviour4.isSelected())
                && isNullOrEmpty(stackSymbols.getText()))
        );
    }

    private void addRule() {
        Toggle toggle = stackBehaviours.getSelectedToggle();
        if (toggle.equals(stackBehaviour1)) {
            // Необходимо удалить верхний символ стека
            rules.add(new DPDARule(
                    fromState.getValue(),
                    ignoreChain.isSelected() ? LAMBDA : chainSymbol.getText().charAt(0),
                    stackTop.getText().charAt(0),
                    toState.getValue(),
                    LAMBDA.toString()
            ));
        } else if (toggle.equals(stackBehaviour2)) {
            // Необходимо оставить содержимое стека без изменений
            rules.add(new DPDARule(
                    fromState.getValue(),
                    ignoreChain.isSelected() ? LAMBDA : chainSymbol.getText().charAt(0),
                    stackTop.getText().charAt(0),
                    toState.getValue(),
                    stackTop.getText()
            ));
        } else if (toggle.equals(stackBehaviour3)) {
            // Необходимо дописать в стек цепочку символов
            rules.add(new DPDARule(
                    fromState.getValue(),
                    ignoreChain.isSelected() ? LAMBDA : chainSymbol.getText().charAt(0),
                    stackTop.getText().charAt(0),
                    toState.getValue(),
                    stackSymbols.getText() + stackTop.getText().charAt(0)
            ));
        } else {
            // Необходимо заменить верхний символ стека цепочкой символов
            rules.add(new DPDARule(
                    fromState.getValue(),
                    ignoreChain.isSelected() ? LAMBDA : chainSymbol.getText().charAt(0),
                    stackTop.getText().charAt(0),
                    toState.getValue(),
                    stackSymbols.getText()
            ));
        }
    }

    private void showRules() {
        userRules.getItems().clear();
        rules.stream()
                .map(DPDARule::toString)
                .forEach(rule -> userRules.getItems().add(rule));
    }

    private void toggleTestButton() {
        test.setDisable(
                states.size() == 0
                        || alphabet.size() == 0
                        || stackAlphabet.size() == 0
                        || startState.getValue() == null
                        || endStates.size() == 0
                        || startStackSymbol.getValue() == null
                        || rules.size() == 0
        );
    }

    private void setAllDisable(boolean disable) {
        userStates.setDisable(disable);
        userAlphabet.setDisable(disable);
        startState.setDisable(disable);
        userStackAlphabet.setDisable(disable);
        userEndStates.setDisable(disable);
        startStackSymbol.setDisable(disable);
        fromState.setDisable(disable);
        toState.setDisable(disable);
        stackBehaviour1.setDisable(disable);
        stackBehaviour2.setDisable(disable);
        stackBehaviour3.setDisable(disable);
        stackBehaviour4.setDisable(disable);
        chainSymbol.setDisable(disable);
        stackTop.setDisable(disable);
        ignoreChain.setDisable(disable);
        stackSymbols.setDisable(disable);
        add.setDisable(disable);
        delete.setDisable(disable);
        userRules.setDisable(disable);
        userChain.setDisable(disable);
        process.setDisable(disable);
        reset.setDisable(disable);
        test.setDisable(disable);
    }

}
