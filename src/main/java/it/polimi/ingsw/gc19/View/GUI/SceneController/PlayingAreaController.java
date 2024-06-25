package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.*;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.*;

/**
 * A scene controller used to manage playing area scene.
 */
public class PlayingAreaController extends GUIController implements StateListener, LocalModelListener {

    @FXML
    private TabPane tabPane;
    @FXML
    private VBox leftVBox, rightVBox, chat;
    @FXML
    private HBox hbox, infoHBox;
    @FXML
    private BorderPane table;
    @FXML
    private StackPane stackPane;

    /**
     * A pane used for displaying fireworks at the end of the game
     */
    private PaneFireworks paneFireworks;

    /**
     * The {@link TabPane} inn which all players' personal stations are displayed
     */
    private TabPane stations;

    /**
     * Current tab seen byy the user
     */
    private Tab currentTab;

    /**
     * One of the sub-scene controllers used by {@link PlayingAreaController}.
     */
    private GUIController chatController, tableController, localStationController;

    public PlayingAreaController(GUIController controller) {
        super(controller);

        getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
        getClientController().getListenersManager().attachListener(ListenerType.LOCAL_MODEL_LISTENER, this);
    }

    /**
     * Initializes playing area scene. First, it builds
     * {@link #infoHBox}, then sets listeners for width and height of scene.
     * Finally, loads all sub-scenes controllers and places them on stage.
     */
    @FXML
    private void initialize(){
        buildInfoHBox();

        leftVBox.prefWidthProperty().bind(super.getStage().widthProperty().multiply(0.75));
        rightVBox.prefWidthProperty().bind(super.getStage().widthProperty().multiply(0.25));

        rightVBox.prefHeightProperty().bind(super.getStage().heightProperty());
        leftVBox.prefHeightProperty().bind(super.getStage().heightProperty());

        leftVBox.spacingProperty().bind(super.getStage().heightProperty().divide(75));
        rightVBox.spacingProperty().bind(super.getStage().heightProperty().divide(75));

        ((HBox) stackPane.getChildren().getFirst()).spacingProperty().bind(super.getStage().widthProperty().divide(100));

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource(SceneStatesEnum.CHAT_SUB_SCENE.value()));
            chatController = new ChatController(this);
            loader.setController(chatController);

            chat = loader.load();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ((Region) chat.getChildren().getFirst()).setPrefHeight(super.getStage().getHeight());

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource(SceneStatesEnum.TABLE_SUB_SCENE.value()));
            tableController = new TableController(this);
            loader.setController(tableController);

            table = loader.load();

            leftVBox.getChildren().add(table);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource(SceneStatesEnum.LOCAL_STATION_TAB_SUB_SCENE.value()));
            localStationController = new LocalStationTabController(this);
            loader.setController(localStationController);

            stations = loader.load();

            leftVBox.getChildren().add(stations);
            VBox.setVgrow(stations, Priority.SOMETIMES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buildTabPane();
        tabPane.getStyleClass().add("floating");

        setBackgrounds();
        paneFireworks = new PaneFireworks(stackPane, super.getLocalModel());
    }

    /**
     * Builds right {@link TabPane} containing chat and
     * game stats. It manages height and width listeners and properties for that.
     */
    private void buildTabPane() {
        this.tabPane.getTabs().clear();

        Tab chatTab = new Tab("Chat");
        Tab gameStatsTab = new Tab("Game Stats");

        chatTab.setContent(chat);

        VBox gamesStatsVBox = new VBox();
        gamesStatsVBox.setPadding(new Insets(20, 0, 0, 0));
        gamesStatsVBox.spacingProperty().bind(super.getStage().heightProperty().divide(20));
        gamesStatsVBox.setAlignment(Pos.CENTER);

        HBox scoreboardHBox = new HBox();
        HBox visibleSymbolsHBox = new HBox();
        scoreboardHBox.setAlignment(Pos.CENTER);
        visibleSymbolsHBox.setAlignment(Pos.CENTER);

        ScoreboardController scoreboardController = new ScoreboardController(this);
        scoreboardController.initialize();

        scoreboardHBox.getChildren().add(scoreboardController.getScoreboardPane());

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource(SceneStatesEnum.GAME_INFOS_SUB_SCENE.value()));
            PlayerSymbolsController controller = new PlayerSymbolsController(this);
            loader.setController(controller);

            visibleSymbolsHBox.getChildren().add(loader.load());

            stations.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
                if (newTab != null && !newTab.equals(oldTab)) {
                    controller.setActiveVisibleTab(newTab.getText());
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gamesStatsVBox.getChildren().addAll(scoreboardHBox,visibleSymbolsHBox);

        gameStatsTab.setContent(gamesStatsVBox);
        tabPane.getTabs().addAll(chatTab, gameStatsTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> {
                    if(newTab != null && !newTab.equals(oldTab)){
                        currentTab = newTab;
                    }
                }
        );

        tabPane.getSelectionModel().select(Objects.requireNonNullElse(currentTab, gameStatsTab));

        if (!rightVBox.getChildren().contains(tabPane)) {
            rightVBox.getChildren().add(tabPane);
        }

    }

    /**
     * Builds {@link #infoHBox}: it contains all infos about user,
     * game and other players state.
     */
    private void buildInfoHBox(){
        if(this.infoHBox.getChildren().isEmpty()) {

            this.infoHBox.getChildren().add(new Label("Username: " + super.getClientController().getNickname()));

            this.infoHBox.getChildren().add(new Label("Game name: " + super.getLocalModel().getGameName()));

            this.infoHBox.getChildren().add(new Label("Number of players: " + super.getLocalModel().getStations().size()));

            this.infoHBox.getChildren().add(new Label("Current game state: " + super.getClientController().getState().toString().toLowerCase().replace('_', ' ')));

            allPlayersConnectedFactory(super.getLocalModel().checkAllPlayersConnected());

            this.infoHBox.spacingProperty().bind(((Region) this.infoHBox.getParent()).widthProperty()
                                                                                     .subtract(this.infoHBox.getChildren().stream()
                                                                                                            .map(c -> ((Region) c).getWidth())
                                                                                                            .mapToDouble(Double::doubleValue)
                                                                                                            .sum())
                                                                                    .divide(20));
        }
        else {
            ((Label) infoHBox.getChildren().get(0)).setText("Username: " + super.getClientController().getNickname());

            ((Label) infoHBox.getChildren().get(1)).setText("Game name: " + super.getLocalModel().getGameName());

            ((Label) infoHBox.getChildren().get(2)).setText("Number of players: " + super.getLocalModel().getStations().size());

            ((Label) infoHBox.getChildren().get(3)).setText("Current game state: " + super.getClientController().getState().toString().toLowerCase().replace('_', ' '));

            infoHBox.getChildren().remove(4, infoHBox.getChildren().size());

            allPlayersConnectedFactory(super.getLocalModel().checkAllPlayersConnected());
        }
    }

    /**
     * Displays an image (tick or X) in {@link #infoHBox}
     * whether all players are connected to the game
     * @param allPlayersConnected <code>true</code> if and only
     *                           all players are connected to the game
     */
    private void allPlayersConnectedFactory(boolean allPlayersConnected){
        String fileName = allPlayersConnected ? "all_connected" : "one_not_connected";

        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/game state/" + fileName + ".png"))));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(25);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(25);

        hBox.getChildren().add(new Label("All players connected: "));
        hBox.getChildren().add(imageView);

        this.infoHBox.getChildren().addLast(hBox);
    }

    /**
     * Sets background to scene and all of its sub-scenes.
     */
    private void setBackgrounds(){
        setBackground(stackPane, true);
        setBackground(infoHBox, false);
        setBackground(table, false);
        for (Tab tab : tabPane.getTabs()) {
            if(tab.getContent() instanceof Pane pane) {
                setBackground(pane, false);
            }
        }
    }

    /**
     * Rebuild {@link #infoHBox} and displays {@link #paneFireworks}
     * when game ends.
     */
    public void endGame(){
        Platform.runLater(() -> {
            buildInfoHBox();
            paneFireworks.start();
        });
    }

    /**
     * Used to notify {@link PlayingAreaController} about events
     * concerning {@link ViewState}.
     * @param viewState the new {@link ViewState}
     */
    @Override
    public void notify(ViewState viewState) {
        Platform.runLater(() -> {
            if (!infoHBox.getChildren().isEmpty())
                ((Label) infoHBox.getChildren().get(3)).setText("Current game state: " + super.getClientController().getState().toString().toLowerCase().replace('_', ' '));
        });

        switch (viewState){
            case ViewState.END -> {
                endGame();
                return;
            }
            case ViewState.DISCONNECT -> super.notifyPossibleDisconnection(stackPane);
            case ViewState.NOT_PLAYER -> super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
            case ViewState.NOT_GAME -> super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
            default -> {return;}
        }

        super.getClientController().getListenersManager().removeListener(chatController);
        super.getClientController().getListenersManager().removeListener(localStationController);
        super.getClientController().getListenersManager().removeListener(tableController);
    }

    /**
     * Used to notify {@link PlayingAreaController} about events
     * concerning {@link LocalModel}.
     * @param type a {@link LocalModelEvents} representing the event type
     * @param localModel the {@link LocalModel} on which the event happened
     * @param varArgs eventual arguments
     */
    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(super.getStage().getScene().getWindow());

            switch (type) {
                case LocalModelEvents.NEW_PLAYER_CONNECTED -> {
                    alert.setTitle("New player connected to game");
                    alert.setContentText(varArgs[0] + " has connected to this game! ");
                }
                case LocalModelEvents.DISCONNECTED_PLAYER -> {
                    alert.setTitle("Disconnected player");
                    alert.setContentText(varArgs[0] + " has disconnected form the game! You will keep seeing his / her infos...");
                }
                case LocalModelEvents.RECONNECTED_PLAYER -> {
                    alert.setTitle("Reconnected player");
                    alert.setContentText(varArgs[0] + " has reconnected to the game!");
                }
            }

            buildInfoHBox();

            alert.showAndWait();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    alert.close();
                }

            }, 5000);
        });
    }

}