package ru.sibsutis.piratetigo.plttm.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Диалоговое окно справки.
 */
public class HelpWindow {
    public static final String HELP_FORM_PATH = "/forms/help.fxml";
    private static final String HELP_WINDOW_TITLE = "Справка";

    /**
     * Наполняет контейнер компонентов окна.
     *
     * @param stage Контейнер компонентов окна.
     * @param faviconLocation URL размещения иконки приложения.
     * @param helpFormLocation URL размещения fxml-файла
     * описания формы справки.
     * @param controller Контроллер для формы.
     * @throws IOException Если fxml-файл описания формы недоступен.
     */
    public static void prepareStage(
            Stage stage,
            URL faviconLocation,
            URL helpFormLocation,
            Object controller
    ) throws IOException {
        FXMLLoader helpFormLoader = new FXMLLoader(helpFormLocation);
        helpFormLoader.setController(controller);
        stage.setScene(new Scene(helpFormLoader.load()));
        stage.setTitle(HELP_WINDOW_TITLE);
        stage.getIcons().add(new Image(faviconLocation.toExternalForm()));
        stage.setResizable(false);
    }
}
