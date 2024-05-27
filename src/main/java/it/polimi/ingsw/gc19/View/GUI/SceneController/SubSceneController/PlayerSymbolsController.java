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
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class PlayerSymbolsController extends AbstractController implements TurnStateListener {

    private static final String[] stateStrings = {
        "Placing a card",
        "Picking a card",
        "Not your turn"
    };

    public BorderPane borderPane;


    public PlayerSymbolsController(AbstractController controller) {
        super(controller);

        getClientController().getListenersManager().attachListener(ListenerType.TURN_LISTENER, this);
    }

    public void initialize() {
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/GameInformationScene.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        borderPane = (BorderPane) Objects.requireNonNull(root).lookup("#symbolsBorderPane");

        updatePlayerState(getLocalModel().getFirstPlayer(), TurnState.PLACE);
    }

    public void updatePlayerState(String nickname, TurnState turnState) {
        Label nicknameLabel = new Label(nickname);
        Label stateLabel = switch (turnState) {
            case TurnState.PLACE -> new Label(stateStrings[0]);
            case TurnState.DRAW -> new Label(stateStrings[1]);
        };

        HBox hbox = (HBox) borderPane.lookup("#hbox");
        hbox.getChildren().clear();
        hbox.getChildren().addAll(nicknameLabel, stateLabel);

        GridPane gridPane = (GridPane) borderPane.lookup("#gridPane");
        gridPane.getChildren().clear();

        gridPane.hgapProperty().bind(super.getStage().widthProperty().multiply(0.25 / 7).divide(7));

        gridPane.vgapProperty().bind(super.getStage().widthProperty().multiply(0.25 / 500));

        for (Symbol s : Symbol.values()) {
            Image symbolImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/symbols/" + s.toString().toLowerCase() + ".png")));
            ImageView symbolImageView = new ImageView(symbolImage);
            symbolImageView.setPreserveRatio(true);
            symbolImageView.fitWidthProperty().bind(super.getStage().widthProperty().multiply(0.2 / 10));

            gridPane.add(symbolImageView, s.ordinal(), 0);
            GridPane.setHalignment(symbolImageView, HPos.CENTER);
            GridPane.setValignment(symbolImageView, VPos.CENTER);
            Label countLabel = new Label(String.valueOf(
                    this.getLocalModel().getStations().get(nickname).getVisibleSymbols().get(s)
            ));
            gridPane.add(countLabel, s.ordinal(), 1);
            GridPane.setHalignment(countLabel, HPos.CENTER);
            GridPane.setValignment(countLabel, VPos.CENTER);
        }
    }


    @Override
    public void notify(String nick, TurnState turnState) {
        Platform.runLater(() -> updatePlayerState(nick, turnState));
    }
}
