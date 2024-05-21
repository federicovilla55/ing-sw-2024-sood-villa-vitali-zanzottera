package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TurnStateListener;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class PlayerSymbolsController extends AbstractController implements TurnStateListener {
    private static final ArrayList<String> symbolImages = new ArrayList<>(Arrays.asList(
        "vegetable",
        "scroll",
        "potion",
        "mushroom",
        "insect",
        "feather",
        "animal"
    ));

    private static final String[] stateStrings = {
        "Placing a card",
        "Picking a card",
        "Not your turn"
    };

    public BorderPane borderPane;


    public PlayerSymbolsController(AbstractController controller) {
        super(controller);
    }

    public void initialize() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/GameInformationScene.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        borderPane = (BorderPane) Objects.requireNonNull(root).lookup("#symbolsBorderPane");
        borderPane.setPrefSize(400, 200);
        borderPane.setMaxSize(400, 200);

        updatePlayerState(getLocalModel().getFirstPlayer(), TurnState.PLACE);
    }

    public void updatePlayerState(String nickname, TurnState turnState){
        String firstPlayer = getLocalModel().getFirstPlayer();
        Label nicknameLabel = new Label(firstPlayer);
        Label stateLabel = new Label(stateStrings[0]);
        HBox hbox = (HBox) borderPane.lookup("#hbox");
        hbox.getChildren().clear();
        hbox.getChildren().addAll(nicknameLabel, stateLabel);

        GridPane gridPane = (GridPane) borderPane.lookup("#gridPane");
        gridPane.getChildren().clear();
        for(String s : symbolImages) {
            Image symbolImage = new Image(getClass().getResourceAsStream("/symbols/"+s+".png"));
            ImageView symbolImageView = new ImageView(symbolImage);
            symbolImageView.setFitHeight(40);
            symbolImageView.setFitWidth(40);
            gridPane.add(symbolImageView, symbolImages.indexOf(s), 0);
            GridPane.setHalignment(symbolImageView, HPos.CENTER);
            GridPane.setValignment(symbolImageView, VPos.CENTER);
            Label countLabel = new Label(String.valueOf(0));
            gridPane.add(countLabel, symbolImages.indexOf(s), 1);
            GridPane.setHalignment(countLabel, HPos.CENTER);
            GridPane.setValignment(countLabel, VPos.CENTER);

        }
    }

    @Override
    public void notify(String nick, TurnState turnState) {
        Platform.runLater(() -> {
            updatePlayerState(nick, turnState);
        });
    }
}
