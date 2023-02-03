package ru.sibsutis.piratetigo.plttm.forms;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.extern.java.Log;

/**
 * Контроллер формы основного окна приложения.
 */
@Log
public class MainForm {

    /**
     * Основное окно приложения.
     */
    @SuppressWarnings("unused, FieldCanBeLocal")
    private Stage mainStage;

    /**
     * Метка отображения версий инструментов, использующихся при создании приложения.
     */
    @FXML
    Label versionsLabel;

    /**
     * Устанавливает основное окно приложения.
     */
    public void setMainStage(Stage stage) {
        mainStage = stage;
        // Устанавливаем обработчик закрытия окна приложения
        stage.setOnCloseRequest(windowEvent -> Platform.exit());
    }

    /**
     * Вызывается автоматически после загрузки формы.
     */
    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        versionsLabel.setText("JavaFX: " + javafxVersion + ", Java: " + javaVersion);
    }

}
