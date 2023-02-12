package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.chain.recognizer.DFSMRecognizer;
import ru.sibsutis.piratetigo.plttm.chain.recognizer.RecognizeException;

import java.util.*;
import java.util.stream.Collectors;

import static ru.sibsutis.piratetigo.plttm.common.Tools.*;

/**
 * Контроллер формы вкладки "Лабораторная работа №2".
 */
@Log
public class Lab2Form {

    private static final String FIRST_COLUMN_ID = "FIRST_COLUMN_ID";

    /** Введенное пользователем множество состояний ДКА. */
    @FXML
    TextArea userStates;

    /** Введенный пользователем алфавит языка. */
    @FXML
    TextArea userAlphabet;

    /** Начальное состояние. */
    @FXML
    ComboBox<String> startState;

    /** Множество заключительных состояний. */
    @FXML
    ListView<String> userEndStates;

    /** Вводимая пользователем функция переходов. */
    @FXML
    @SuppressWarnings("rawtypes")
    TableView<Map> transitions;

    /** Введенная пользователем цепочка символов языка. */
    @FXML
    TextArea userChain;

    /** Кнопка запуска процесса проверки введенной цепочки. */
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

    /** Вычисленное множество состояний ДКА. */
    private HashSet<String> states = new HashSet<>();

    /** Вычисленный алфавит языка. */
    private HashSet<Character> alphabet = new HashSet<>();

    /** Вычисленное множество заключительных состояний. */
    private final HashSet<String> endStates = new HashSet<>();

    /** Проверяемая на принадлежность алфавиту языка цепочка символов. */
    private LinkedList<Character> chain = new LinkedList<>();

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
                        transitions.getColumns().setAll(
                                generateColumns(states, alphabet)
                        );
                        transitions.getItems().setAll(
                                generateTableData(states, alphabet)
                        );
                    }
                    toggleTestButton();
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
                    }
                    transitions.getColumns().setAll(
                            generateColumns(states, alphabet)
                    );
                    transitions.getItems().setAll(
                            generateTableData(states, alphabet)
                    );
                    toggleTestButton();
                });

        // Обработчик автоматического изменения длины элемента управления начальным состоянием
        startState.widthProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.longValue() > userAlphabet.widthProperty().longValue()) {
                        startState.setPrefWidth(userAlphabet.widthProperty().longValue());
                    }
        });

        // Обработчик автоматического изменения длины элемента управления конечными состояниями
        userEndStates.widthProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.longValue() > userAlphabet.widthProperty().longValue()) {
                        userEndStates.setPrefWidth(userAlphabet.widthProperty().longValue());
                    }
        });

        userEndStates.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Обработчик выбора заключительных состояний
        userEndStates.setOnMouseClicked(event -> {
            endStates.clear();
            endStates.addAll(userEndStates.getSelectionModel().getSelectedItems());
            toggleTestButton();
        });

        transitions.getSelectionModel().setCellSelectionEnabled(true);
        transitions.setEditable(true);

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
                        + chain.stream().map(Object::toString).reduce("", String::concat));
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
                    DFSMRecognizer dfsmRecognizer = new DFSMRecognizer(
                            states,
                            alphabet,
                            startState.getValue(),
                            endStates,
                            extractTableData(),
                            new LinkedList<>(chain),
                            process
                    );
                    try {
                        dfsmRecognizer.recognize();
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
                    });
                }
            });

            recognizeThread.start();
        });

        // Обработчик кнопки сброса значений
        reset.setOnAction(event -> {
            userStates.setText("");
            userAlphabet.setText("");
        });
    }

    @SuppressWarnings("rawtypes, unchecked")
    private List<TableColumn<Map, String>> generateColumns(
            HashSet<String> srcStates,
            HashSet<Character> srcAlphabet) {
        // Создаем список столбцов таблицы
        List<TableColumn<Map, String>> table = new LinkedList<>();

        if (srcStates.isEmpty()) {
            return table;
        }

        // Создаем столбец "Состояние"
        TableColumn<Map, String> firstColumn = new TableColumn<>("Состояние");
        firstColumn.setCellValueFactory(new MapValueFactory<>(FIRST_COLUMN_ID));
        firstColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        firstColumn.setEditable(false);
        firstColumn.setPrefWidth(Integer.max(
                (int)startState.getWidth() + 10,
                (int)firstColumn.getWidth())
        );
        table.add(firstColumn);

        // Создаем столбцы "Вход" для алфавита языка
        if (!srcAlphabet.isEmpty()) {
            TableColumn<Map, String> inputColumn = new TableColumn<>("Вход");
            srcAlphabet.forEach(character -> {
                TableColumn<Map, String> nextInputColumn =
                        new TableColumn<>(character.toString());
                nextInputColumn.setCellValueFactory(new MapValueFactory<>(character.toString()));
                nextInputColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                        new DefaultStringConverter(),
                        srcStates.toArray(String[]::new)
                ));
                nextInputColumn.setEditable(true);
                nextInputColumn.setPrefWidth(startState.getWidth() + 10);
                nextInputColumn.setOnEditCommit(event -> {
                    int rowNumber = event.getTablePosition().getRow();
                    String columnName = event.getTablePosition().getTableColumn().getText();
                    event.getTableView()
                            .getItems()
                            .get(rowNumber)
                            .put(columnName, event.getNewValue());
                });
                inputColumn.getColumns().add(nextInputColumn);
            });
            table.add(inputColumn);
        }

        return table;
    }

    ObservableList<Map<String, String>> generateTableData(
            HashSet<String> srcStates,
            HashSet<Character> srcAlphabet) {
        ObservableList<Map<String, String>> allData =
                FXCollections.observableArrayList();
        Map<String, String> dataRowTemplate = new HashMap<>();
        srcAlphabet.forEach(item -> dataRowTemplate.put(item.toString(), ""));
        srcStates.forEach(state -> {
            Map<String, String> dataRow = new HashMap<>();
            dataRow.put(FIRST_COLUMN_ID, state);
            dataRow.putAll(dataRowTemplate);
            allData.add(dataRow);
        });
        return allData;
    }

    private void setAllDisable(boolean disable) {
        userStates.setDisable(disable);
        userAlphabet.setDisable(disable);
        startState.setDisable(disable);
        userEndStates.setDisable(disable);
        transitions.setDisable(disable);
        userChain.setDisable(disable);
        test.setDisable(disable);
    }

    private void toggleTestButton() {
        test.setDisable(
                states.size() == 0
                        || alphabet.size() == 0
                        || startState.getValue() == null
                        || endStates.size() == 0
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<Character, String>> extractTableData() {
        return transitions.getItems()
                .stream()
                .collect(
                        HashMap::new,
                        (table, map) -> {
                            HashMap<String, String> row = (HashMap<String, String>) map;
                            table.put(
                                    (String)map.get(FIRST_COLUMN_ID),
                                    row.entrySet().stream()
                                            .filter(entry ->
                                                !FIRST_COLUMN_ID.equals(entry.getKey()))
                                            .collect(Collectors.toMap(
                                                    entry -> entry.getKey().charAt(0),
                                                    Map.Entry::getValue))
                            );
                        },
                        HashMap::putAll
                );
    }

}
