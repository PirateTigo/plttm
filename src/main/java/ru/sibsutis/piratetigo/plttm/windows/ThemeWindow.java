package ru.sibsutis.piratetigo.plttm.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Диалоговое окно описания темы курсовой работы.
 */
public class ThemeWindow {
    public static final String THEME_FORM_PATH = "/forms/theme.fxml";
    private static final String THEME_WINDOW_TITLE = "Тема курсовой работы";

    /**
     * Наполняет контейнер компонентов окна.
     *
     * @param stage Контейнер компонентов окна.
     * @param faviconLocation URL размещения иконки приложения.
     * @param themeFormLocation URL размещения fxml-файла
     * описания формы темы курсовой работы.
     * @param controller Контроллер для формы.
     * @throws IOException Если fxml-файл описания формы недоступен.
     */
    public static void prepareStage(
            Stage stage,
            URL faviconLocation,
            URL themeFormLocation,
            Object controller
    ) throws IOException {
        FXMLLoader themeFormLoader = new FXMLLoader(themeFormLocation);
        themeFormLoader.setController(controller);
        stage.setScene(new Scene(themeFormLoader.load()));
        stage.setTitle(THEME_WINDOW_TITLE);
        stage.getIcons().add(new Image(faviconLocation.toExternalForm()));
        stage.setResizable(false);
    }
}
