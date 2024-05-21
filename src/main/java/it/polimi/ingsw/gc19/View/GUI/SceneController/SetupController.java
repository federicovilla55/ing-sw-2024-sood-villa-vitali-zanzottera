package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.ChatController;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.LocalStationTabController;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController.TableController;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardButton;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetupController extends AbstractController implements SetupListener, StateListener {

    private CardButton initialCardUp, initialCardDown;

    @FXML
    private VBox leftVBox, rightVBox, chat;

    @FXML
    private HBox hbox, goalCardsHBox, initialCardHBox;

    @FXML
    private BorderPane availableColorsPane, initialCardOrientationPane, privateGoalCardSelectionPane, table, stations;

    protected SetupController(AbstractController controller) {
        super(controller);
    }

    public void initialize(){

        getClientController().getListenersManager().attachListener(ListenerType.SETUP_LISTENER, this);
        getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);

        buildAvailableColorsHBox();
        buildPrivateGoalCardSelectionHBox();
        buildInitialCardHBox();

        leftVBox.prefWidthProperty().bind(super.getStage().widthProperty().multiply(0.75));
        rightVBox.prefWidthProperty().bind(super.getStage().widthProperty().multiply(0.25));

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new File("src/main/resources/fxml/ChatScene.fxml").toURL());
            ChatController controller = new ChatController(this);
            loader.setController(controller);

            chat = loader.load();

            rightVBox.getChildren().add(chat);

            //controller.resize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

            stations.prefWidthProperty().bind(super.getStage().widthProperty().multiply(0.75));

            leftVBox.getChildren().add(stations);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildInitialCardHBox(){
        if(this.getLocalModel().getPersonalStation().getPlacedCardSequence().isEmpty()) {

            initialCardUp = new CardButton(this.getLocalModel().getPersonalStation().getInitialCard());
            initialCardDown = new CardButton(this.getLocalModel().getPersonalStation().getInitialCard());
            initialCardDown.swap();

            this.initialCardHBox.getChildren().addAll(List.of(initialCardUp, initialCardDown));

            for(var b :List.of(initialCardUp, initialCardDown)){

                b.setOnMouseClicked((event) -> {
                    getClientController().placeInitialCard(b.getCardOrientation());
                    initialCardHBox.getChildren().clear();
                    rightVBox.getChildren().remove(initialCardOrientationPane);

                    double size = initialCardOrientationPane.getHeight();

                    resizeChat(size);
                });

            }

        }
    }

    private void buildPrivateGoalCardSelectionHBox(){
        if(this.getLocalModel().getPersonalStation().getPrivateGoalCardInStation() == null){
            List<CardButton> cardButtons = new ArrayList<>(List.of(
                    new CardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardsInStation()[0]),
                    new CardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardsInStation()[1])));

            cardButtons.forEach(c -> c.setOnMouseClicked((event) -> {
                getClientController().chooseGoal(cardButtons.indexOf(c));

                cardButtons.forEach(b -> b.setOnMouseClicked(b.getDefaultMouseClickedHandler()));

                rightVBox.getChildren().remove(privateGoalCardSelectionPane);

                double size = privateGoalCardSelectionPane.getHeight();

                resizeChat(size);
            }));

            this.goalCardsHBox.getChildren().addAll(cardButtons);
        }
    }

    private void buildAvailableColorsHBox(){
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

            circlePawn.setFill(new ImagePattern(new Image(Objects.requireNonNull(getClass().getResource("/pawns/" + c.toString().toLowerCase() + "_pawn.png")).toExternalForm())));

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

            double size = availableColorsPane.getHeight();

            rightVBox.getChildren().remove(availableColorsPane);

            resizeChat(size);
        });

        return button;
    }

    private void resizeChat(double additionalSize){
        ((Region) chat.getChildren().getFirst()).setPrefHeight(((ScrollPane) chat.getChildren().getFirst()).getHeight() + 0.25 * ((Region) chat.getParent()).getHeight());
    }

    @Override
    public void notify(SetupEvent type) {
        Platform.runLater(() ->{
            switch (type){
                case SetupEvent.ACCEPTED_COLOR, SetupEvent.AVAILABLE_COLOR -> buildAvailableColorsHBox();
                case SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD -> buildPrivateGoalCardSelectionHBox();
                case SetupEvent.ACCEPTED_INITIAL_CARD -> buildInitialCardHBox();
            }
        });
    }

    @Override
    public void notify(SetupEvent type, String errorDescription){
        super.notify(errorDescription);
        switch (type){
            case SetupEvent.COLOR_NOT_AVAILABLE -> {
                this.hbox.getChildren().clear();
                buildAvailableColorsHBox();
            }
        }
    }

    @Override
    public void notify(ViewState viewState) {

    }
}
