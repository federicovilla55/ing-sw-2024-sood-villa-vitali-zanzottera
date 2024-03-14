module it.polimi.ingsw.gc19 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens it.polimi.ingsw.gc19 to javafx.fxml;
    exports it.polimi.ingsw.gc19;
    exports it.polimi.ingsw.gc19.Model.Card to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.gc19.Model.Card to com.fasterxml.jackson.databind;
    exports it.polimi.ingsw.gc19.Model.Enums to com.fasterxml.jackson.databind;
    opens it.polimi.ingsw.gc19.Model.Enums to com.fasterxml.jackson.databind;
}