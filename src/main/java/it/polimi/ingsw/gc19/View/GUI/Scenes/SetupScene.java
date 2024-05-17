package it.polimi.ingsw.gc19.View.GUI.Scenes;

import it.polimi.ingsw.gc19.Enums.Color;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

public class SetupScene extends Scene {

    public SetupScene(Parent root) {
        super(root);
    }

    @FXML
    private TitledPane availableColors;
    @FXML
    private HBox hBox;
    @FXML
    private Button redButton, yellowButton, greenButton, blueButton;

}
