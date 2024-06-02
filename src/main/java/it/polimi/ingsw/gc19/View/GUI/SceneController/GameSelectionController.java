package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameSelectionController extends AbstractController implements StateListener, GameHandlingListener {

    @FXML
    private Button joinButton, createButton;
    @FXML
    private TextField gameName;
    @FXML
    private ChoiceBox<Integer> numPlayerBox;
    @FXML
    private ListView<String> availableGamesList;
    @FXML
    private StackPane stackPane;

    @FXML
    private ImageView logoImageView;

    @FXML
    private VBox contentVBox, leftVBox, rightVBox;

    @FXML
    private HBox createAndJoin;

    @FXML
    private Label createGameLabel, joinGameLabel, gameNameLabel, numberOfPlayers, availableGamesText;

    private final Integer[] possibleNumPlayer = {2,3,4};

    protected GameSelectionController(AbstractController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);
    }

    @FXML
    public void initialize(){
        numPlayerBox.getItems().addAll(possibleNumPlayer);
        numPlayerBox.setValue(2);

        joinButton.setOnMouseClicked((event) -> {
            String gameName = availableGamesList.getSelectionModel().getSelectedItem();

            if(gameName != null) {
                super.getClientController().joinGame(gameName);
            }
        });

        createButton.setOnMouseClicked((event) -> {
            String name = gameName.getText();
            int numPlayer = numPlayerBox.getValue();
            if(name != null && !name.isEmpty()) {
                super.getClientController().createGame(name, numPlayer);
            }
        });

        super.getClientController().availableGames();

        loadLogo();
        contentVBox.spacingProperty().bind(super.getStage().heightProperty().divide(14));
        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(4));

        createAndJoin.spacingProperty().bind(super.getStage().widthProperty().divide(15));

        leftVBox.spacingProperty().bind(super.getStage().heightProperty().divide(16));
        rightVBox.spacingProperty().bind(super.getStage().heightProperty().divide(16));

        leftVBox.prefWidthProperty().bind(createAndJoin.widthProperty().subtract(createAndJoin.getSpacing()).divide(2));
        rightVBox.prefWidthProperty().bind(createAndJoin.widthProperty().subtract(createAndJoin.getSpacing()).divide(2));

        createGameLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 30),
                super.getStage().heightProperty()
        ));
        joinGameLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 30),
                super.getStage().heightProperty()
        ));
        joinButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 50),
                super.getStage().heightProperty()
        ));
        createButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 50),
                super.getStage().heightProperty()
        ));
        gameNameLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 50),
                super.getStage().heightProperty()
        ));
        numberOfPlayers.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 50),
                super.getStage().heightProperty()
        ));
        availableGamesText.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 50),
                super.getStage().heightProperty()
        ));

        availableGamesList.setCellFactory(list -> {
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font(super.getStage().getHeight() / 50),
                    super.getStage().heightProperty()
            ));
            return cell;
        });
    }

    private void loadLogo() {
        try {
            Image logoImage = new Image(new FileInputStream("src/main/resources/images/logo.png"));
            logoImageView.setImage(logoImage);
            System.out.println("Logo loaded");
            logoImageView.setPreserveRatio(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notify(ViewState viewState) {
        if(viewState == ViewState.DISCONNECT){
            super.getClientController().getListenersManager().removeListener(this);
            super.notifyPossibleDisconnection(this.stackPane);
        }
    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {
        switch (type){
            case GameHandlingEvents.CREATED_GAME, GameHandlingEvents.JOINED_GAMES -> {
                changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            }
            case GameHandlingEvents.AVAILABLE_GAMES -> Platform.runLater(() -> availableGamesList.setItems(FXCollections.observableArrayList(new ArrayList<>(varArgs))));
        }
    }

}
