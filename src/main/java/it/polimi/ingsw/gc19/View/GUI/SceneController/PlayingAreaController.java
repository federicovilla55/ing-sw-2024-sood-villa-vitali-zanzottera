package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.*;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TurnStateListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class PlayingAreaController extends AbstractController implements StateListener, LocalModelListener {

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

    private PaneFireworks paneFireworks;

    private Tab currentTab;
    private AbstractController chatController, tableController, localStationController;

    public PlayingAreaController(AbstractController controller) {
        super(controller);

        getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
        getClientController().getListenersManager().attachListener(ListenerType.LOCAL_MODEL_LISTENER, this);
    }

    @FXML
    public void initialize(){
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
            loader.setLocation(new File("src/main/resources/fxml/ChatScene.fxml").toURL());
            chatController = new ChatController(this);
            loader.setController(chatController);

            chat = loader.load();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ((Region) chat.getChildren().getFirst()).setPrefHeight(super.getStage().getHeight());

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/TableScene.fxml").toURL());
            tableController = new TableController(this);
            loader.setController(tableController);

            table = loader.load();

            leftVBox.getChildren().add(table);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/LocalStationTab.fxml").toURL());
            localStationController = new LocalStationTabController(this);
            loader.setController(localStationController);

            TabPane stations = loader.load();

            leftVBox.getChildren().add(stations);
            VBox.setVgrow(stations, Priority.SOMETIMES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buildTabPane();
        tabPane.getStyleClass().add("floating");

        setBackgrounds();
        paneFireworks = new PaneFireworks(stackPane, super.getStage(), super.getLocalModel());
    }

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

        scoreboardHBox.getChildren().add(scoreboardController.scoreboardPane);

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/GameInformationScene.fxml").toURL());
            PlayerSymbolsController controller = new PlayerSymbolsController(this);
            loader.setController(controller);

            visibleSymbolsHBox.getChildren().add(loader.load());

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

        if(currentTab == null){
            tabPane.getSelectionModel().select(gameStatsTab);
        }
        else {
            tabPane.getSelectionModel().select(currentTab);
        }

        if (!rightVBox.getChildren().contains(tabPane)) {
            rightVBox.getChildren().add(tabPane);
        }
    }

    private void buildInfoHBox(){
        if(this.infoHBox.getChildren().isEmpty()) {

            this.infoHBox.getChildren().add(new Label("Username: " + super.getClientController().getNickname()));

            this.infoHBox.getChildren().add(new Label("Game name: " + super.getLocalModel().getGameName()));

            this.infoHBox.getChildren().add(new Label("Current number of players: " + super.getLocalModel().getStations().size()));

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

            ((Label) infoHBox.getChildren().get(2)).setText("Current number of players: " + super.getLocalModel().getStations().size());

            ((Label) infoHBox.getChildren().get(3)).setText("Current game state: " + super.getClientController().getState().toString().toLowerCase().replace('_', ' '));

            allPlayersConnectedFactory(super.getLocalModel().checkAllPlayersConnected());
        }
    }

    private void allPlayersConnectedFactory(boolean canStart){
        String fileName = canStart ? "all_connected" : "one_not_connected";

        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/game state/" + fileName + ".png")).toExternalForm()));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(25);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(25);

        hBox.getChildren().add(new Label("All players connected: "));
        hBox.getChildren().add(imageView);

        this.infoHBox.getChildren().addLast(hBox);
    }

    private void setBackgrounds(){
        setBackground(stackPane, true);
        setBackground(infoHBox, false);
        setBackground(table, false);
        for (Tab tab : tabPane.getTabs()) {
            if(tab.getContent() instanceof Pane pane) {
                setBackground(pane, false);
            }
        }
        /*for (Tab tab : stations.getTabs()) {
            if(tab.getContent() instanceof Pane pane) {
                setBackground(pane, false);
            }
        }*/
    }

    private void setBackground(Pane pane, Boolean element){
        Image backgroundImage = null;
        String location = "";
        if(element) {
            location = "src/main/resources/images/background_dark.png";
        }else {
            location = "src/main/resources/images/background_light.png";
        }
        try {
            backgroundImage = new Image(new FileInputStream(location));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BackgroundSize backgroundSize = new BackgroundSize(360, 360, false, false, false, false);
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);

        pane.setBackground(new Background(background));
    }

    public void endGame(){
        Platform.runLater(() -> {
            buildInfoHBox();
            paneFireworks.start();
        });
        System.out.println("GIOCO FINITO...");

    }

    @Override
    public void notify(ViewState viewState) {
        Platform.runLater(() -> {
            if (!infoHBox.getChildren().isEmpty())
                ((Label) infoHBox.getChildren().get(3)).setText("Current game state: " + super.getClientController().getState().toString().toLowerCase().replace('_', ' '));
        });

        switch (viewState){
            case ViewState.END -> endGame();
            case ViewState.DISCONNECT -> super.notifyPossibleDisconnection(stackPane);
            case ViewState.NOT_PLAYER -> super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
            case ViewState.NOT_GAME -> super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
            default -> {return;}
        }

        super.getClientController().getListenersManager().removeListener(chatController);
        super.getClientController().getListenersManager().removeListener(localStationController);
        super.getClientController().getListenersManager().removeListener(tableController);
    }

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
