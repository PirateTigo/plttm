package ru.sibsutis.piratetigo.plttm.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Диалоговое окно для отображения сообщения пользователю.
 */
public class MessageWindow {
    public static final String MESSAGE_FORM_PATH = "/forms/message.fxml";
    private static final String MESSAGE_WINDOW_TITLE = "Сообщение";

    /**
     * Наполняет контейнер компонентов окна.
     *
     * @param stage Контейнер компонентов окна.
     * @param faviconLocation URL размещения иконки приложения.
     * @param messageFormLocation URL размещения fxml-файла
     * описания формы окна сообщения пользователю.
     * @param controller Контроллер для формы.
     * @throws IOException Если fxml-файл описания формы недоступен.
     */
    public static void prepareStage(
            Stage stage,
            URL faviconLocation,
            URL messageFormLocation,
            Object controller
    ) throws IOException {
        FXMLLoader messageFormLoader = new FXMLLoader(messageFormLocation);
        messageFormLoader.setController(controller);
        stage.setScene(new Scene(messageFormLoader.load()));
        stage.setTitle(MESSAGE_WINDOW_TITLE);
        stage.getIcons().add(new Image(faviconLocation.toExternalForm()));
        stage.setResizable(false);
    }
}
