package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.windows.AuthorWindow;
import ru.sibsutis.piratetigo.plttm.windows.HelpWindow;
import ru.sibsutis.piratetigo.plttm.windows.ThemeWindow;

import java.io.IOException;
import java.net.URL;

import static ru.sibsutis.piratetigo.plttm.windows.AuthorWindow.AUTHOR_FORM_PATH;
import static ru.sibsutis.piratetigo.plttm.windows.HelpWindow.HELP_FORM_PATH;
import static ru.sibsutis.piratetigo.plttm.windows.ThemeWindow.THEME_FORM_PATH;

/**
 * Контроллер формы основного окна приложения.
 */
@Log
public class MainForm {

    private static MainForm instance;

    /**
     * Метка отображения версий инструментов, использующихся при создании приложения.
     */
    @FXML
    Label versionsLabel;

    /**
     * Основное окно приложения.
     */
    private Stage mainStage;

    /**
     * URL размещения иконки приложения.
     */
    @Setter
    @Getter
    private URL faviconPath;

    /**
     * Контроллер формы справки.
     */
    private HelpForm helpFormController;

    /**
     * Контроллер формы темы курсовой работы.
     */
    private ThemeForm themeFormController;

    /**
     * Контроллер формы информации об авторе.
     */
    private AuthorForm authorFormController;

    /**
     * Устанавливает ссылку на экземпляр основной формы приложения.
     */
    static void setInstance(MainForm mainForm) {
        MainForm.instance = mainForm;
    }

    /**
     * Получает экземпляр контроллера основной формы приложения.\
     */
    static MainForm getInstance() {
        return MainForm.instance;
    }

    /**
     * Устанавливает основное окно приложения.
     */
    public void setMainStage(Stage stage) {
        mainStage = stage;
        // Устанавливаем обработчик закрытия окна приложения
        stage.setOnCloseRequest(windowEvent -> Platform.exit());
    }

    /**
     * Возвращает основное окно приложения.
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * Обрабатывает кнопку меню "Чтение из файла".
     */
    @SuppressWarnings("unused")
    public void fromFileHandler() {
        LOGGER.info("Нажата кнопка \"Чтение из файла\"...");
        if (!CourseForm.getInstance().grammarFromFile.isDisabled()) {
            CourseForm.getInstance().openFileHandler();
        }
    }

    /**
     * Обрабатывает кнопку меню "Ввод с клавиатуры".
     */
    @SuppressWarnings("unused")
    public void fromKeyboardHandler() {
        LOGGER.info("Нажата кнопка \"Ввод с клавиатуры\"");
        CourseForm.getInstance().userTerminals.requestFocus();
    }

    /**
     * Обрабатывает кнопку меню "Запустить".
     */
    @SuppressWarnings("unused")
    public void calculateHandler() {
        LOGGER.info("Нажата кнопка \"Запустить\"");
        if (!CourseForm.getInstance().start.isDisabled()) {
            CourseForm.getInstance().start.fire();
        }
    }

    /**
     * Обрабатывает кнопку меню "Отменить".
     */
    @SuppressWarnings("unused")
    public void cancelHandler() {
        LOGGER.info("Нажата кнопка \"Отменить\"");
        CourseForm.getInstance().cancel.fire();
    }

    /**
     * Обработчик нажатия на кнопку "Сохранение КС-грамматики в файл...".
     */
    @SuppressWarnings("unused")
    public void saveSrcGrammarToFileHandler() {
        LOGGER.info("Нажата кнопка \"Сохранение КС-грамматики в файл...\"");
        if (!CourseForm.getInstance().grammarToFile.isDisabled()) {
            CourseForm.getInstance().saveSrcGrammarToFileHandler();
        }
    }

    /**
     * Обработчик нажатия на кнопку "Сохранение БНФ-грамматики в файл...".
     */
    @SuppressWarnings("unused")
    public void saveChomskyGrammarToFileHandler() {
        LOGGER.info("Нажата кнопка \"Сохранение БНФ-грамматики в файл...\"");
        if (!CourseForm.getInstance().grammarChomskyToFile.isDisabled()) {
            CourseForm.getInstance().saveChomskyNormalFormGrammarToFileHandler();
        }
    }

    /**
     * Обрабатывает кнопку меню "Справка".
     */
    @SuppressWarnings("unused")
    public void helpHandler() {
        LOGGER.info("Нажата кнопка \"Справка\"");

        try {
            Stage stage = new Stage();
            HelpWindow.prepareStage(
                    stage,
                    faviconPath,
                    getClass().getResource(HELP_FORM_PATH),
                    helpFormController
            );
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            LOGGER.warning(
                    String.format(
                            "Не удалось загрузить форму справки\n%s",
                            ex.getMessage()
                    )
            );
        }
    }

    /**
     * Обрабатывает кнопку меню "Тема".
     */
    @SuppressWarnings("unused")
    public void themeHandler() {
        LOGGER.info("Нажата кнопка \"Тема\"");

        try {
            Stage stage = new Stage();
            ThemeWindow.prepareStage(
                    stage,
                    faviconPath,
                    getClass().getResource(THEME_FORM_PATH),
                    themeFormController
            );
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            LOGGER.warning(
                    String.format(
                            "Не удалось загрузить форму темы курсовой работы\n%s",
                            ex.getMessage()
                    )
            );
        }
    }

    /**
     * Обрабатывает кнопку меню "Автор".
     */
    @SuppressWarnings("unused")
    public void authorHandler() {
        LOGGER.info("Нажата кнопка \"Автор\"");

        try {
            Stage stage = new Stage();
            AuthorWindow.prepareStage(
                    stage,
                    faviconPath,
                    getClass().getResource(AUTHOR_FORM_PATH),
                    authorFormController
            );
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showAndWait();
        } catch (IOException ex) {
            LOGGER.warning(
                    String.format(
                            "Не удалось загрузить форму информации об авторе\n%s",
                            ex.getMessage()
                    )
            );
        }
    }

    /**
     * Вызывается автоматически после загрузки формы.
     */
    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        setInstance(this);
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        versionsLabel.setText("JavaFX: " + javafxVersion + ", Java: " + javaVersion);
        helpFormController = new HelpForm();
        themeFormController = new ThemeForm();
        authorFormController = new AuthorForm();
    }

}
