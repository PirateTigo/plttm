package ru.sibsutis.piratetigo.plttm.forms;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

/**
 * Контроллер формы сообщения пользователю.
 */
@RequiredArgsConstructor
public class MessageForm {
    /**
     * Сообщение пользователю.
     */
    @FXML
    Label message;

    /**
     * Кнопка "ОК".
     */
    @FXML
    Button ok;

    /** Окно сообщения пользователю. */
    private final Stage messageStage;

    public void setMessage(String msg) {
        message.setText(msg);
    }

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        ok.setOnAction(event -> messageStage.hide());
    }
}
