package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.Color;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class SetupController extends AbstractController{

    public HBox hbox;
    @FXML
    private BorderPane availableColors;

    @FXML
    private Button redButton, yellowButton, greenButton, blueButton;

    private Button[] colorButtons;

    @FXML
    public void initialize(){
        for(Color c : List.of(Color.GREEN, Color.RED)){
            ImageView pawn = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/pawns/blue_pawn.png")).toExternalForm()));
            Button button = new Button();
            pawn.setFitWidth(50);
            pawn.setFitHeight(50);
            button.setGraphic(pawn);
            button.setId(c + "Button");
            button.setStyle("-fx-background-radius: 7.5em;\n" +
                                    "-fx-border-color: black;\n" +
                                    "-fx-border-width: 0.25;\n" +
                                    "-fx-border-radius: 7.5em;");
            this.hbox.getChildren().add(button);
        }
        this.availableColors.setCenter(hbox);
    }

    private Button factoryButton(Color color){
        Button button = new Button();
        button.setText(color.toString());
        return button;
    }

}
