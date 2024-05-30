package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.*;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GUI.Utils.GoalCardButton;
import it.polimi.ingsw.gc19.View.GUI.Utils.PlayableCardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
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
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SetupController extends AbstractController implements SetupListener, StateListener, LocalModelListener {

    @FXML
    private VBox leftVBox, rightVBox, chat;
    @FXML
    private HBox hbox, goalCardsHBox, initialCardHBox, infoHBox;
    @FXML
    private BorderPane availableColorsPane, initialCardOrientationPane, privateGoalCardSelectionPane, table;
    @FXML
    private StackPane stackPane;
    @FXML
    private TabPane tabPane;
    private AbstractController chatController, tableController, localStationController;

    public SetupController(AbstractController controller) {
        super(controller);

        getClientController().getListenersManager().attachListener(ListenerType.SETUP_LISTENER, this);
        getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
        getClientController().getListenersManager().attachListener(ListenerType.LOCAL_MODEL_LISTENER, this);
    }

    public void initialize(){
        buildAvailableColorsPane();
        buildPrivateGoalCardSelectionHBox();
        buildInitialCardHBox();

        buildInfoHBox();

        leftVBox.prefWidthProperty().bind(super.getStage().widthProperty().multiply(0.75));
        rightVBox.prefWidthProperty().bind(super.getStage().widthProperty().multiply(0.25));

        leftVBox.prefHeightProperty().bind(super.getStage().heightProperty());
        rightVBox.prefHeightProperty().bind(super.getStage().heightProperty());

        leftVBox.spacingProperty().bind(super.getStage().heightProperty().divide(75));
        rightVBox.spacingProperty().bind(super.getStage().heightProperty().divide(75));

        ((HBox) stackPane.getChildren().getFirst()).spacingProperty().bind(super.getStage().widthProperty().divide(100));

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/ChatScene.fxml").toURL());
            chatController = new ChatController(this);
            loader.setController(chatController);

            chat = loader.load();

            rightVBox.getChildren().add(chat);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ((Region) chat.getChildren().getFirst()).setPrefHeight(super.getStage().getHeight() -
                                                                           (this.availableColorsPane.getHeight() + this.privateGoalCardSelectionPane.getHeight() + this.privateGoalCardSelectionPane.getHeight()));

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

            tabPane = loader.load();

            leftVBox.getChildren().add(tabPane);

            VBox.setVgrow(tabPane, Priority.SOMETIMES);

            tabPane.heightProperty().addListener((observable, oldValue, newValue) -> System.out.println("stack " + tabPane.getHeight()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void buildInfoHBox(){
        this.infoHBox.getChildren().clear();

        this.infoHBox.getChildren().add(new Label("Username: " + super.getClientController().getNickname()));

        this.infoHBox.getChildren().add(new Label("Game name: " + super.getLocalModel().getGameName()));

        this.infoHBox.getChildren().add(new Label("Required number of player: " + super.getLocalModel().getNumPlayers()));

        this.infoHBox.getChildren().add(new Label("Current number of players: " + super.getLocalModel().getStations().size()));

        gameStateFactory(super.getLocalModel().gameCanStart());

        this.infoHBox.spacingProperty().bind(((Region) this.infoHBox.getParent()).widthProperty()
                                                                                 .subtract(this.infoHBox.getChildren().stream()
                                                                                                                      .map(c -> ((Region) c).getWidth())
                                                                                                                      .mapToDouble(Double::doubleValue)
                                                                                                                      .sum())
                                                                                 .divide(20));
    }

    private void gameStateFactory(boolean canStart){
        String fileName = canStart ? "can_start" : "cannot_start";

        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/game state/" + fileName + ".png")).toExternalForm()));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(25);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(25);

        hBox.getChildren().add(new Label("Game can start: "));
        hBox.getChildren().add(imageView);

        this.infoHBox.getChildren().add(hBox);
    }

    private void buildInitialCardHBox(){
        if(this.getLocalModel().getPersonalStation().getPlacedCardSequence().isEmpty()) {

            PlayableCardButton initialCardUp = new PlayableCardButton(this.getLocalModel().getPersonalStation().getInitialCard(), super.getStage(), (double) 1 / 10, (double) 1 /7);
            PlayableCardButton initialCardDown = new PlayableCardButton(this.getLocalModel().getPersonalStation().getInitialCard(), super.getStage(), (double) 1 / 10, (double) 1 /7);
            initialCardDown.swap();

            this.initialCardHBox.getChildren().addAll(List.of(initialCardUp, initialCardDown));

            for(var b : List.of(initialCardUp, initialCardDown)){
                b.setOnMouseClicked((event) -> {
                    getClientController().placeInitialCard(b.getCardOrientation());
                    initialCardHBox.getChildren().clear();
                    rightVBox.getChildren().remove(initialCardOrientationPane);

                    resizeChat();
                });
            }
        }
    }

    private void buildPrivateGoalCardSelectionHBox(){
        if(this.getLocalModel().getPersonalStation().getPrivateGoalCardInStation() == null){
            List<GoalCardButton> cardButtons = new ArrayList<>(List.of(
                    new GoalCardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardsInStation()[0], super.getStage(), (double) 1 / 10, (double) 1 /7),
                    new GoalCardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardsInStation()[1], super.getStage(), (double) 1 / 10, (double) 1 /7)));

            cardButtons.forEach(c -> c.setOnMouseClicked((event) -> {

                getClientController().chooseGoal(cardButtons.indexOf(c));

                cardButtons.forEach(b -> b.setOnMouseClicked(b.getDefaultMouseClickedHandler()));

                rightVBox.getChildren().remove(privateGoalCardSelectionPane);

                resizeChat();
            }));

            this.goalCardsHBox.getChildren().addAll(cardButtons);
        }
    }

    private void buildAvailableColorsPane(){
        if(getLocalModel().getAvailableColors() != null && getLocalModel().getPersonalStation() != null && getLocalModel().getPersonalStation().getChosenColor() == null){
            this.hbox.getChildren().clear();
            hbox.getChildren().addAll(colorButtonFactory(this.getLocalModel().getAvailableColors()));
        }

        if(getLocalModel().getPersonalStation().getChosenColor() != null){
            ArrayList<Button> button = colorButtonFactory(List.of(this.getLocalModel().getPersonalStation().getChosenColor()));

            this.hbox.getChildren().clear();
            this.hbox.getChildren().add(button.getFirst());
        }
    }

    private ArrayList<Button> colorButtonFactory(List<Color> availableColors){
        ArrayList<Button> buttons = new ArrayList<>();

        for(Color c : availableColors){
            Circle circlePawn = new Circle(25);

            circlePawn.setFill(new ImagePattern(
                    new Image(Objects.requireNonNull(getClass().getResource("/pawns/" + c.toString().toLowerCase() + "_pawn.png"))
                                     .toExternalForm())));

            circlePawn.radiusProperty().bind(super.getStage().heightProperty().divide(58));

            Button button = buildColorButton(c, circlePawn);

            buttons.add(button);
        }

        return buttons;
    }

    @NotNull
    private Button buildColorButton(Color c, Circle circlePawn) {
        Button button = new Button();
        button.setShape(new Circle(25));

        button.setGraphic(circlePawn);
        button.setPadding(Insets.EMPTY);

        button.setId(c + "_button");
        button.setOnMouseClicked(event -> {

            SetupController.super.getClientController().chooseColor(Color.valueOf(button.getId().toUpperCase().split("_")[0]));

            rightVBox.getChildren().remove(availableColorsPane);

            resizeChat();
        });

        return button;
    }

    private void resizeChat(){
        ((Region) chat.getChildren().getFirst()).setPrefHeight(((ScrollPane) chat.getChildren().getFirst()).getHeight() + 0.25 * ((Region) chat.getParent()).getHeight());
    }

    @Override
    public void notify(SetupEvent type) {
        Platform.runLater(() ->{
            switch (type){
                case SetupEvent.ACCEPTED_COLOR, SetupEvent.AVAILABLE_COLOR -> buildAvailableColorsPane();
                case SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD -> buildPrivateGoalCardSelectionHBox();
                case SetupEvent.ACCEPTED_INITIAL_CARD -> buildInitialCardHBox();
            }
        });
    }

    @Override
    public void notify(SetupEvent type, String errorDescription){
        Platform.runLater(() -> {
            super.notify(errorDescription);

            if (type == SetupEvent.COLOR_NOT_AVAILABLE) {
                buildAvailableColorsPane();
            }
        });
    }

    @Override
    public void notify(ViewState viewState) {

        switch (viewState){
            case ViewState.PICK, ViewState.PLACE, ViewState.OTHER_TURN -> super.changeToNextScene(SceneStatesEnum.PLAYING_AREA_SCENE);
            case ViewState.DISCONNECT -> {
                super.getClientController().getListenersManager().removeListener(this);
                super.notifyPossibleDisconnection(this.stackPane);
            }
            case ViewState.NOT_PLAYER -> super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
            case ViewState.NOT_GAME -> super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
            default -> {return;}
        }

        super.getClientController().getListenersManager().removeListener(chatController);
        super.getClientController().getListenersManager().removeListener(tableController);
        super.getClientController().getListenersManager().removeListener(localStationController);
    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(super.getStage());
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

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    alert.close();
                }

            }, 5000);
        });
    }

}
