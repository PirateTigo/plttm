package ru.sibsutis.piratetigo.plttm;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import ru.sibsutis.piratetigo.plttm.forms.MainForm;

import java.io.IOException;
import java.net.URL;

import static ru.sibsutis.piratetigo.plttm.windows.MainWindow.*;

/**
 * JavaFX приложение "Теория языков программирования и методы трансляции".
 */
@Log
public class GUIStarter extends Application {

    public static final String FAVICON_PATH = "/icons/compiler.png";

    /**
     * Точка входа в JavaFX приложение.
     *
     * @param stage Контейнер компонентов.
     */
    @Override
    public void start(Stage stage) {
        LOGGER.info(MAIN_WINDOW_TITLE);
        Class<? extends GUIStarter> clazz = getClass();
        URL faviconPath = clazz.getResource(FAVICON_PATH);
        URL formPath = clazz.getResource(MAIN_FORM_PATH);

        if (faviconPath == null) {
            LOGGER.warning("Не указан путь до иконки приложения");
            Platform.exit();
            return;
        }

        if (formPath == null) {
            LOGGER.warning("Не указан путь до описания основной формы");
            Platform.exit();
        }

        try {
            prepareStage(
                    stage,
                    faviconPath,
                    formPath,
                    new MainForm()
            );
            stage.show();
        } catch (IOException e) {
            LOGGER.info("Не удалось загрузить основную форму приложения");
            e.printStackTrace();
        }
    }

    /**
     * Точка входа в Java приложение.
     * <p>
     * Необходима для запуска из ОС.
     *
     * @param args Параметры приложения.
     */
    public static void main(String[] args) {
        launch(args);
    }

}
