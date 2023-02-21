package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import lombok.extern.java.Log;

/**
 * Контроллер формы справки.
 */
@Log
public class HelpForm {
    /**
     * Контейнер элементов формы.
     */
    @FXML
    AnchorPane helpContainer;
    /**
     * Основное текстовое поле справки.
     */
    @FXML
    TextArea mainText;

    /**
     * Вызывается автоматически после загрузки формы.
     */
    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        LOGGER.info("Открыто диалоговое окно \"Справка\"");

        mainText.setOnMouseClicked(event ->
                // Сбрасываем фокус принудительно с текстового поля
                helpContainer.requestFocus());

        Platform.runLater(() -> {
            try {
                // Сбрасываем фокус принудительно с текстового поля
                Thread.sleep(10);
                helpContainer.requestFocus();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
