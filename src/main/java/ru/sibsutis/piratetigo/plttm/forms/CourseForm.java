package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.chain.generator.Generator;
import ru.sibsutis.piratetigo.plttm.common.InferenceType;
import ru.sibsutis.piratetigo.plttm.grammar.ContextFreeGrammar;
import ru.sibsutis.piratetigo.plttm.windows.MessageWindow;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

import static ru.sibsutis.piratetigo.plttm.common.Tools.*;
import static ru.sibsutis.piratetigo.plttm.windows.MessageWindow.MESSAGE_FORM_PATH;

/**
 * Контроллер формы вкладки "Курсовая работа".
 */
@Log
public class CourseForm {

    private static CourseForm instance;

    /** Работает с файлами приложения. */
    FileChooser fileChooser;

    /** Кнопка чтения КС-грамматики из файла. */
    @FXML
    Button grammarFromFile;

    /** Кнопка записи КС-грамматики в файл. */
    @FXML
    Button grammarToFile;

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

    /** Кнопка сброса всех полей. */
    @FXML
    Button reset;

    /** Кнопка перевода КС-грамматики в канонический вид. */
    @FXML
    Button toCanonical;

    /** Кнопка перевода КС-грамматики из канонического вида в форму Хомского. */
    @FXML
    Button toChomsky;

    /** Кнопка сохранения БНФ-грамматики в файл. */
    @FXML
    Button grammarChomskyToFile;

    /** Терминальные символы БНФ. */
    @FXML
    TextArea userChomskyTerminals;

    /** Нетерминальные символы БНФ. */
    @FXML
    TextArea userChomskyNonTerminals;

    /** Целевой символ БНФ. */
    @FXML
    ComboBox<String> chomskyGoalCharacter;

    /** Множество правил вывода БНФ. */
    @FXML
    ListView<String> chomskyRules;

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

    /** Вывод цепочек грамматики Хомского. */
    @FXML
    ListView<String> chomskyInference;

    /** Счетчик числа сгенерированных цепочек грамматики Хомского. */
    @FXML
    Label chomskyChainCount;

    /** Контекстно-свободная грамматика. */
    private ContextFreeGrammar contextFreeGrammar;

    /** Грамматика Хомского (БНФ-грамматика). */
    private ContextFreeGrammar chomskyGrammar;

    /** Признак возможности добавления текущего введенного правила вывода в список. */
    private boolean isCorrectRule = false;

    /** Генератор цепочек. */
    private Generator generator;

    /** Генератор цепочек для грамматики Хомского. */
    private Generator chomskyGenerator;

    /**
     * Устанавливает ссылку на экземпляр формы "Курсовая работа".
     */
    static void setInstance(CourseForm courseForm) {
        CourseForm.instance = courseForm;
    }

    /**
     * Получает экземпляр формы "Курсовая работа".
     */
    static CourseForm getInstance() {
        return CourseForm.instance;
    }

    /**
     * Обработчик нажатия на кнопку "Прочитать из файла".
     */
    public void openFileHandler() {
        File file = fileChooser.showOpenDialog(
                MainForm.getInstance().getMainStage()
        );
        if (file != null) {
            LOGGER.info(
                    String.format("Указан файл %s для загрузки КС-грамматики", file)
            );

            try {
                reset.fire();
                contextFreeGrammar.load(file);
                clearChomsky();
                toggleSaveGrammarButton();
                toggleStartButton();
            } catch (IOException ex) {
                showMessage("Ошибка чтения файла");
                LOGGER.warning(
                        String.format("Ошибка чтения файла\n%s", ex.getMessage())
                );
                reset.fire();
            } catch (Throwable ex) {
                showMessage(ex.getMessage());
                reset.fire();
            }
        }
    }

    /**
     * Обработчик нажатия на кнопку "Сохранить в файл" в блоке "КС-грамматика".
     */
    public void saveSrcGrammarToFileHandler() {
        File file = fileChooser.showSaveDialog(
                MainForm.getInstance().getMainStage()
        );
        if (file != null) {
            LOGGER.info(
                    String.format("Указан файл %s для сохранения КС-грамматики", file)
            );

            try {
                contextFreeGrammar.save(file);
                showMessage(
                        String.format("КС-грамматика сохранена в файл %s", file)
                );
            } catch (IOException ex) {
                showMessage("Ошибка записи в файл");
                LOGGER.warning(
                        String.format("Ошибка записи в файл\n%s", ex.getMessage())
                );
            } catch (Throwable ex) {
                showMessage(ex.getMessage());
            }
        }
    }

    /**
     * Обработчик нажатия на кнопку "Сохранить в файл" в блоке "БНФ-грамматика".
     */
    public void saveChomskyGrammarToFileHandler() {
        File file = fileChooser.showSaveDialog(
                MainForm.getInstance().getMainStage()
        );
        if (file != null) {
            LOGGER.info(
                    String.format("Указан файл %s для сохранения БНФ-грамматики", file)
            );

            try {
                chomskyGrammar.save(file);
                showMessage(
                        String.format("БНФ-грамматика сохранена в файл %s", file)
                );
            } catch (IOException ex) {
                showMessage("Ошибка записи в файл");
                LOGGER.warning(
                        String.format("Ошибка записи в файл\n%s", ex.getMessage())
                );
            } catch (Throwable ex) {
                showMessage(ex.getMessage());
            }
        }
    }

    @FXML
    private void initialize() {
        setInstance(this);

        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Programming Languages Theory and Translation Methods",
                        "*.plttm"
                )
        );

        contextFreeGrammar = new ContextFreeGrammar(
                userTerminals,
                userNonTerminals,
                goalCharacter,
                rules
        );

        // Обработчик кнопки "Прочитать из файла" для КС-грамматики
        grammarFromFile.setOnAction(event -> openFileHandler());

        // Обработчик кнопки "Сохранить в файл" для КС-грамматики
        grammarToFile.setOnAction(event -> saveSrcGrammarToFileHandler());

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
                        clearChomsky();
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
                        clearChomsky();
                    }
                }
        );

        // Обработчик выбора целевого символа
        goalCharacter.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    toggleSaveGrammarButton();
                    toggleStartButton();
                    if (goalCharacter.getValue() != null) {
                        contextFreeGrammar.setGoal(goalCharacter.getValue());
                    }
                });

        // Обработчик выбора целевого символа БНФ
        chomskyGoalCharacter.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (chomskyGoalCharacter.getValue() != null
                        && chomskyGrammar != null) {
                        chomskyGrammar.setGoal(chomskyGoalCharacter.getValue());
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

        // Обработчик сохранения БНФ-грамматики в файл
        grammarChomskyToFile.setOnAction(event ->
                saveChomskyGrammarToFileHandler()
        );

        // Обработчик кнопки генерации БНФ-грамматики
        toChomsky.setOnAction(event -> {
            chomskyGrammar = contextFreeGrammar.clone(
                    userChomskyTerminals,
                    userChomskyNonTerminals,
                    chomskyGoalCharacter,
                    chomskyRules
            );
            chomskyGrammar.toChomsky();
            grammarChomskyToFile.setDisable(false);
        });

        // Обработчик изменения количества элементов в списке правил вывода
        rules.getItems().addListener((ListChangeListener<String>) c -> {
            toChomsky.setDisable(true);
            clearChomsky();
            toggleSaveGrammarButton();
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
            chomskyGrammar.log();
            InferenceType type = InferenceType.from(inferenceType.getText());
            inference.getItems().clear();
            chomskyInference.getItems().clear();
            chainCount.setText(COUNTER_DEFAULT);
            chomskyChainCount.setText(COUNTER_DEFAULT);

            generator = new Generator(
                    contextFreeGrammar.getRules(),
                    contextFreeGrammar.getGoal(),
                    type,
                    chainSizeFrom.getValue(),
                    chainSizeTo.getValue(),
                    symbolsToStrings(contextFreeGrammar.getTerminals())
            );

            chomskyGenerator = new Generator(
                    chomskyGrammar.getRules(),
                    chomskyGrammar.getGoal(),
                    type,
                    chainSizeFrom.getValue(),
                    chainSizeTo.getValue(),
                    symbolsToStrings(chomskyGrammar.getTerminals())
            );

            CompletableFuture<String> generation = new CompletableFuture<>();
            Thread chainGenerationTask = Generator.createGenerationTask(
                    generator,
                    inference,
                    chainCount,
                    generation
            );

            CompletableFuture<String> chomskyGeneration = new CompletableFuture<>();
            Thread chainChomskyGenerationTask = Generator.createGenerationTask(
                    chomskyGenerator,
                    chomskyInference,
                    chomskyChainCount,
                    chomskyGeneration
            );

            chainGenerationTask.start();
            chainChomskyGenerationTask.start();

            CompletableFuture.allOf(generation, chomskyGeneration)
                    .thenRun(() ->
                            Platform.runLater(() ->
                                    setAllDisable(false)));
        });

        // Обработчик кнопки отмены генерации цепочек
        cancel.setOnAction(event -> {
            if (generator != null && generator.isRunning()) {
                generator.cancel();
            }
            if (chomskyGenerator != null && chomskyGenerator.isRunning()) {
                chomskyGenerator.cancel();
            }
        });

        // Обработчик кнопки сброса всех полей
        reset.setOnAction(event -> {
            userTerminals.textProperty().set("");
            userNonTerminals.textProperty().set("");
            userTerminals.requestFocus();
        });

        // Обработчик кнопки перевода КС-грамматики в канонический вид
        toCanonical.setOnAction(event -> {
            contextFreeGrammar.toCanonical();
            toChomsky.setDisable(false);
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

    private void toggleSaveGrammarButton() {
        boolean condition = rules.getItems().size() == 0
                || goalCharacter.getValue() == null;
        grammarToFile.setDisable(condition);
        toCanonical.setDisable(condition);
        if (condition) {
            toChomsky.setDisable(true);
        }
    }

    private void clearChomsky() {
        chomskyGrammar = null;
        userChomskyTerminals.clear();
        userChomskyNonTerminals.clear();
        chomskyGoalCharacter.setValue(null);
        chomskyRules.getItems().clear();
        grammarChomskyToFile.setDisable(true);
    }

    private void toggleStartButton() {
        start.setDisable(
                rules.getItems().size() == 0
                        || goalCharacter.getValue() == null
        );
    }

    private void showMessage(String message) {
        try {
            Stage messageStage = new Stage();
            MessageForm messageForm = new MessageForm(messageStage);
            MessageWindow.prepareStage(
                    messageStage,
                    MainForm.getInstance().getFaviconPath(),
                    getClass().getResource(MESSAGE_FORM_PATH),
                    messageForm
            );
            messageStage.initModality(Modality.WINDOW_MODAL);
            messageForm.setMessage(message);
            messageStage.showAndWait();
        } catch (IOException ex) {
            LOGGER.warning(
                    String.format(
                            "Не удалось загрузить форму темы курсовой работы\n%s",
                            ex.getMessage()
                    )
            );
        }
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
        grammarToFile.setDisable(disable);
        grammarFromFile.setDisable(disable);
        toCanonical.setDisable(disable);
        toChomsky.setDisable(disable);
        grammarChomskyToFile.setDisable(disable);
        if (!disable) {
            toggleStartButton();
            toggleSaveGrammarButton();
            toggleAddRuleButton();
        }
    }

}
