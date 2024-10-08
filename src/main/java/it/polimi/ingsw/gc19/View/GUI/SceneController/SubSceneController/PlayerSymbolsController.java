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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSymbolsController extends GUIController implements TurnStateListener, StationListener {

    @FXML
    public BorderPane symbolsBorderPane;

    @FXML
    private TabPane tabPane;

    /**
     * A hashmap that connects the name of each {@link Symbol} to the corresponding image.
     */
    private final ConcurrentHashMap<String, Image> symbolImages;

    /**
     * A hashmap that connects the name of each player to the corresponding {@link Tab}.
     */
    private final ConcurrentHashMap<String, Tab> playerTabs;

    /**
     * A hashmap that connects the name of each symbol to the corresponding element in the GridPanel.
     */
    private final ConcurrentHashMap<String, GridPane> tableElements;

    public PlayerSymbolsController(GUIController controller) {
        super(controller);

        getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
        getClientController().getListenersManager().attachListener(ListenerType.TURN_LISTENER, this);

        tableElements = new ConcurrentHashMap<>();
        playerTabs = new ConcurrentHashMap<>();
        symbolImages = new ConcurrentHashMap<>();
    }

    /**
     * Initializes player' visible symbols sub-scene.
     * It builds images with {@link #initializeImages()} and
     * visible symbols table with {@link #initializeVisibleSymbolsTable()}
     */
    @FXML
    private void initialize() {
        initializeImages();
        initializeVisibleSymbolsTable();

        tabPane.getStyleClass().add("floating");
    }

    /**
     * The method initializes the images of the symbols and save them in the hashmap.
     */
    private void initializeImages(){
        for (Symbol s : Symbol.values()) {
            Image symbolImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/symbols/" + s.toString().toLowerCase() + ".png")));
            symbolImages.put(s.toString().toLowerCase(), symbolImage);
        }
    }

    /**
     * Setter for current {@link Tab} seen by the user
     * @param nickname the nickname of the player whose
     *                 {@link Tab} is currently seen by the user
     */
    public void setActiveVisibleTab(String nickname){
        Tab selectedTab = playerTabs.get(nickname);
        tabPane.getSelectionModel().select(selectedTab);
    }

    /**
     * To initialize the table with the player symbols and their relative value.
     */
    private void initializeVisibleSymbolsTable() {
        this.tabPane.getTabs().clear();

        for(String nickname : new ArrayList<>(getLocalModel().getStations().keySet())) {
            Tab symbolsContainer = new Tab(nickname);

            VBox symbolsBorderVBOX = new VBox();
            symbolsBorderVBOX.setAlignment(Pos.CENTER);
            symbolsBorderVBOX.setSpacing(10);
            VBox.setVgrow(symbolsBorderVBOX, Priority.ALWAYS);

            StackPane gridPaneWrapper = new StackPane();
            gridPaneWrapper.setAlignment(Pos.CENTER);
            StackPane.setAlignment(gridPaneWrapper, Pos.CENTER);
            VBox.setVgrow(gridPaneWrapper, Priority.ALWAYS);

            VBox.setMargin(gridPaneWrapper, new Insets(7, 0, 7, 0));

            GridPane symbolsGrid = new GridPane();
            symbolsGrid.setAlignment(Pos.CENTER);
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

            gridPaneWrapper.getChildren().add(symbolsGrid);
            symbolsBorderVBOX.getChildren().add(gridPaneWrapper);
            tableElements.put(nickname, symbolsGrid);
            symbolsContainer.setContent(symbolsBorderVBOX);
            tabPane.getTabs().add(symbolsContainer);
            playerTabs.put(nickname, symbolsContainer);
        }
    }

    /**
     * To update the number of each symbol of a player given its nickname
     * @param nick the nickname of the player we want to update its symbol numbers.
     */
    private void updatePlayerState(String nick){
        GridPane grid = tableElements.get(nick);

        if(grid != null) {
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
    public void notifyErrorStation(String... varArgs) { }

}