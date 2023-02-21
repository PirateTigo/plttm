package ru.sibsutis.piratetigo.plttm.windows;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import ru.sibsutis.piratetigo.plttm.forms.MainForm;

import java.io.IOException;
import java.net.URL;

/**
 * Основное окно приложения.
 */
@UtilityClass
public class MainWindow {

    public final String MAIN_WINDOW_TITLE =
            "Теория языков программирования и методы трансляции";

    public final String MAIN_FORM_PATH = "/forms/main.fxml";

    private final double MAIN_WINDOW_WIDTH = 1200;
    private final double MAIN_WINDOW_HEIGHT = 620;

    /**
     * Наполняет контейнер компонентов окна.
     *
     * @param stage Контейнер компонентов окна.
     * @param faviconLocation URL размещения иконки приложения.
     * @param mainFormLocation URL размещения fxml-файла описания основной формы.
     * @param controller Контроллер для формы.
     * @throws IOException Если fxml-файл описания формы недоступен.
     */
    public void prepareStage(
            Stage stage,
            URL faviconLocation,
            URL mainFormLocation,
            Object controller) throws IOException {
        FXMLLoader mainFormLoader = new FXMLLoader(mainFormLocation);
        mainFormLoader.setController(controller);
        stage.setScene(new Scene(mainFormLoader.load(), MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT));
        stage.setTitle(MAIN_WINDOW_TITLE);
        stage.getIcons().add(new Image(faviconLocation.toExternalForm()));
        MainForm mainForm = ((MainForm) controller);
        mainForm.setMainStage(stage);
        mainForm.setFaviconPath(faviconLocation);
    }

}
