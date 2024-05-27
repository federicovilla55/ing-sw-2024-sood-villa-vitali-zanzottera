package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TurnStateListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class PlayerSymbolsController extends AbstractController implements TurnStateListener {

    @FXML
    public BorderPane symbolsBorderPane;

    @FXML
    private VBox symbolsBorderVBOX;

    private HashMap<String, Image> symbolImages;

    private HashMap<String, GridPane> tableElements;

    public PlayerSymbolsController(AbstractController controller) {
        super(controller);

        getClientController().getListenersManager().attachListener(ListenerType.TURN_LISTENER, this);
    }

    public void initialize() {
        initializeImages();
        initializeVisibleSymbolsTable();
    }

    private void initializeImages(){
        symbolImages = new HashMap<>();
        for (Symbol s : Symbol.values()) {
            Image symbolImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/symbols/" + s.toString().toLowerCase() + ".png")));
            symbolImages.put(s.toString().toLowerCase(), symbolImage);
        }
    }

    public void initializeVisibleSymbolsTable() {
        symbolsBorderVBOX.getChildren().clear();
        tableElements = new HashMap<>();

        for(String nickname : getLocalModel().getStations().keySet()) {
            Label nicknameLabel = new Label(nickname);

            HBox labelBox = new HBox();
            labelBox.getChildren().addAll(nicknameLabel);

            GridPane symbolsGrid = new GridPane();
            symbolsGrid.hgapProperty().bind(super.getStage().widthProperty().multiply(0.25 / 7).divide(7));
            symbolsGrid.vgapProperty().bind(super.getStage().widthProperty().multiply(0.25 / 500));

            for (Symbol s : Symbol.values()) {
                ImageView symbolImageView = new ImageView(symbolImages.get(s.toString().toLowerCase()));
                symbolImageView.setPreserveRatio(true);
                symbolImageView.fitWidthProperty().bind(super.getStage().widthProperty().multiply(0.2 / 10));

                symbolsGrid.add(symbolImageView, s.ordinal(), 0);
                GridPane.setHalignment(symbolImageView, HPos.CENTER);
                GridPane.setValignment(symbolImageView, VPos.CENTER);
                Label countLabel = new Label(String.valueOf(
                        this.getLocalModel().getStations().get(nickname).getVisibleSymbols().get(s)
                ));
                symbolsGrid.add(countLabel, s.ordinal(), 1);
                GridPane.setHalignment(countLabel, HPos.CENTER);
                GridPane.setValignment(countLabel, VPos.CENTER);
            }

            symbolsBorderVBOX.getChildren().addAll(labelBox, symbolsGrid);
            tableElements.put(nickname, symbolsGrid);
        }
    }

    public void updatePlayerState(String nick){
        GridPane grid = tableElements.get(nick);

        ObservableList<Node> children = grid.getChildren();
        children.removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == 1);

        for (Symbol s : Symbol.values()) {
            Label countLabel = new Label(String.valueOf(
                    this.getLocalModel().getStations().get(nick).getVisibleSymbols().get(s)
            ));
            grid.add(countLabel, s.ordinal(), 1);
            GridPane.setHalignment(countLabel, HPos.CENTER);
            GridPane.setValignment(countLabel, VPos.CENTER);
        }
    }




    @Override
    public void notify(String nick, TurnState turnState) {
        Platform.runLater(() -> updatePlayerState(nick));
    }
}
