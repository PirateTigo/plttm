module plttm {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires com.google.common;
    opens ru.sibsutis.piratetigo.plttm.forms to javafx.fxml;
    opens ru.sibsutis.piratetigo.plttm.common to javafx.fxml;
    exports ru.sibsutis.piratetigo.plttm;
    exports ru.sibsutis.piratetigo.plttm.grammar to com.fasterxml.jackson.databind;
}