module plttm {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires java.logging;
    opens ru.sibsutis.piratetigo.plttm.forms to javafx.fxml;
    opens ru.sibsutis.piratetigo.plttm.common to javafx.fxml;
    exports ru.sibsutis.piratetigo.plttm;
}