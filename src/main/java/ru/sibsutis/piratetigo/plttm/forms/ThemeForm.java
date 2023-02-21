package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import lombok.extern.java.Log;

/**
 * Контроллер формы окна темы курсовой работы.
 */
@Log
public class ThemeForm {
    /**
     * Контейнер элементов формы.
     */
    @FXML
    AnchorPane themeContainer;
    /**
     * Основное текстовое поле с описанием темы.
     */
    @FXML
    TextArea mainText;

    /**
     * Вызывается автоматически после загрузки формы.
     */
    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        LOGGER.info("Открыто диалоговое окно \"Тема курсовой работы\"");

        mainText.setOnMouseClicked(event ->
                // Сбрасываем фокус принудительно с текстового поля
                themeContainer.requestFocus());

        Platform.runLater(() -> {
            try {
                // Сбрасываем фокус принудительно с текстового поля
                Thread.sleep(10);
                themeContainer.requestFocus();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
