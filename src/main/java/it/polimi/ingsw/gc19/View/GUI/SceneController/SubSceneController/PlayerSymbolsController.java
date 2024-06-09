package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.GUIController;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TurnStateListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.*;

public class PlayerSymbolsController extends GUIController implements TurnStateListener, StationListener {

    @FXML
    public BorderPane symbolsBorderPane;

    @FXML
    private VBox symbolsBorderVBOX;

    /**
     * A hashmap that connects the name of each symbol to the corresponding image.
     */
    private HashMap<String, Image> symbolImages;

    /**
     * A hashmap that connects the name of each symbol to the corresponding element in the GridPanel.
     */
    private HashMap<String, GridPane> tableElements;

    public PlayerSymbolsController(GUIController controller) {
        super(controller);

        getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
        getClientController().getListenersManager().attachListener(ListenerType.TURN_LISTENER, this);
    }

    public void initialize() {
        initializeImages();
        initializeVisibleSymbolsTable();
    }

    /**
     * The method to initializes the image of the symbols and save them in the hashmap.
     */
    private void initializeImages(){
        symbolImages = new HashMap<>();
        for (Symbol s : Symbol.values()) {
            Image symbolImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/symbols/" + s.toString().toLowerCase() + ".png")));
            symbolImages.put(s.toString().toLowerCase(), symbolImage);
        }
    }

    /**
     * To initialize the table with the player symbols and their relative value.
     */
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

    /**
     * To update the number of each symbol of a player given its nickname
     * @param nick the nickname of the player we want to update its symbol numbers.
     */
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

    /**
     * To update the number of symbols of a player at the beginning of its turn.
     * @param nick the nickname of the player currently playing
     * @param turnState the {@link TurnState} of that player
     */
    @Override
    public void notify(String nick, TurnState turnState) {
        Platform.runLater(() -> updatePlayerState(nick));
    }

    /**
     * To update the number of symbols of a player given the {@link PersonalStation}.
     * @param localStationPlayer is the {@link PersonalStation} that has changed
     */
    @Override
    public void notify(PersonalStation localStationPlayer) {
        Platform.runLater(() -> updatePlayerState(localStationPlayer.getOwnerPlayer()));
    }

    /**
     * To update the number of symbols of a player given the {@link OtherStation}.
     * @param otherStation is the {@link OtherStation} that has changed
     */
    @Override
    public void notify(OtherStation otherStation) {
        Platform.runLater(() -> updatePlayerState(otherStation.getOwnerPlayer()));
    }

    /**
     * This method is not implemented as those kind of errors be handled in another part of the GUI view.
     * @param varArgs strings describing the error
     */
    @Override
    public void notifyErrorStation(String... varArgs) {
        
    }
}
