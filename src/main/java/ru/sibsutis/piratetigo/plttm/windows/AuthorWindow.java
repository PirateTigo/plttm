package ru.sibsutis.piratetigo.plttm.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Диалоговое окно информации об авторе.
 */
public class AuthorWindow {
    public static final String AUTHOR_FORM_PATH = "/forms/author.fxml";
    private static final String AUTHOR_WINDOW_TITLE = "Информация об авторе";

    /**
     * Наполняет контейнер компонентов окна.
     *
     * @param stage Контейнер компонентов окна.
     * @param faviconLocation URL размещения иконки приложения.
     * @param authorFormLocation URL размещения fxml-файла
     * описания формы информации об авторе.
     * @param controller Контроллер для формы.
     * @throws IOException Если fxml-файл описания формы недоступен.
     */
    public static void prepareStage(
            Stage stage,
            URL faviconLocation,
            URL authorFormLocation,
            Object controller
    ) throws IOException {
        FXMLLoader authorFormLoader = new FXMLLoader(authorFormLocation);
        authorFormLoader.setController(controller);
        stage.setScene(new Scene(authorFormLoader.load()));
        stage.setTitle(AUTHOR_WINDOW_TITLE);
        stage.getIcons().add(new Image(faviconLocation.toExternalForm()));
        stage.setResizable(false);
    }
}
