package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.*;
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
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class PlayingAreaController extends AbstractController implements StateListener, LocalModelListener {

    @FXML
    private TabPane tabPane;
    
    @FXML
    private TabPane stations;

    @FXML
    private VBox leftVBox, rightVBox, chat;

    @FXML
    private HBox hbox, infoHBox;

    @FXML
    private BorderPane table;

    @FXML
    private StackPane stackPane;

    private Tab currentTab;

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

        leftVBox.prefHeightProperty().bind(super.getStage().heightProperty());
        rightVBox.prefHeightProperty().bind(super.getStage().heightProperty());

        leftVBox.spacingProperty().bind(super.getStage().heightProperty().divide(75));
        rightVBox.spacingProperty().bind(super.getStage().heightProperty().divide(75));

        ((HBox) stackPane.getChildren().getFirst()).spacingProperty().bind(super.getStage().widthProperty().divide(100));

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/ChatScene.fxml").toURL());
            ChatController controller = new ChatController(this);
            loader.setController(controller);

            chat = loader.load();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        ((Region) chat.getChildren().getFirst()).setPrefHeight(super.getStage().getHeight());

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/TableScene.fxml").toURL());
            TableController controller = new TableController(this);
            loader.setController(controller);

            table = loader.load();

            leftVBox.getChildren().add(table);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/LocalStationTab.fxml").toURL());
            LocalStationTabController controller = new LocalStationTabController(this);
            loader.setController(controller);

            stations = loader.load();

            stations.prefHeightProperty().bind(super.getStage().heightProperty().subtract(((Region) this.infoHBox.getParent()).getPrefHeight()).subtract(((Region) this.table.getParent()).getPrefHeight()));
            stations.prefWidthProperty().bind(leftVBox.widthProperty());
            stations.prefWidthProperty().addListener(
                    (observable, oldValue, newValue) -> stations.setMaxSize(stations.getPrefWidth(), stations.getPrefHeight())
            );
            stations.prefHeightProperty().addListener(
                    (observable, oldValue, newValue) -> stations.setMaxSize(stations.getPrefWidth(), stations.getPrefHeight())
            );


            leftVBox.getChildren().add(stations);
            VBox.setVgrow(stations, Priority.ALWAYS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buildTabPane();
        stations.getStyleClass().add("floating");
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

        if(this.infoHBox.getChildren().size() == 0) {

            this.infoHBox.getChildren().add(new Label("Username: " + super.getClientController().getNickname()));

            this.infoHBox.getChildren().add(new Label("Game name: " + super.getLocalModel().getGameName()));

            this.infoHBox.getChildren().add(new Label("Current number of players: " + super.getLocalModel().getStations().size()));

            this.infoHBox.getChildren().add(new Label("Current game state: " + super.getClientController().getState().toString()));

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

            ((Label) infoHBox.getChildren().get(3)).setText("Current game state: " + super.getClientController().getState().toString());
        }
    }

    private void notifyPossibleDisconnection(){
        for(Node n : this.stackPane.getChildrenUnmodifiable()){
            n.setOpacity(0.15);
        }

        super.getStage().getScene().setFill(javafx.scene.paint.Color.GREY);

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/ReconnectionWaitScene.fxml").toURL());
            ReconnectionWaitController controller = new ReconnectionWaitController(this);
            loader.setController(controller);

            StackPane waitStack = loader.load();

            this.stackPane.getChildren().add(waitStack);
            this.stackPane.requestLayout();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notify(ViewState viewState) {
        Platform.runLater(() -> {
            if (!infoHBox.getChildren().isEmpty())
                ((Label) infoHBox.getChildren().get(3)).setText("Current game state: " + super.getClientController().getState().toString());
        });

        switch (viewState){
            case ViewState.DISCONNECT -> notifyPossibleDisconnection();
            //@TODO: Handle pause and end of game
            //@TODO: handle DISCONNECTED STATE
        }
    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

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

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    alert.close();
                }

            }, 5000);
        });
    }
}
