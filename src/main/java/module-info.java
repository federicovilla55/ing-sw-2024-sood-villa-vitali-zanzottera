module it.polimi.ingsw.gc19 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens it.polimi.ingsw.gc19 to javafx.fxml;
    exports it.polimi.ingsw.gc19;
}