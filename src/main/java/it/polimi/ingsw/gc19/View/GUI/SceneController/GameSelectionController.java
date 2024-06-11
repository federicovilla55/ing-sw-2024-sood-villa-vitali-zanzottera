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
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A scene controller. It manages available games and
 * lets users create or join a certain game.
 */
public class GameSelectionController extends GUIController implements StateListener, GameHandlingListener {

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

    /**
     * Admissible number of players for a game
     */
    private final Integer[] possibleNumPlayer = {2,3,4};

    protected GameSelectionController(GUIController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);
    }

    /**
     * Initializes the scene. It builds all the necessary GUI items
     * and manages their width or height using listeners and proprieties
     */
    @FXML
    private void initialize(){
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

        availableGamesList.setCellFactory(list -> {
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            return cell;
        });

        super.setBackground(stackPane, false);
    }

    /**
     * Loads the logo of Codex Naturalis' and places it inside scene
     */
    private void loadLogo() {
        Image logoImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/logo.png")));
        logoImageView.setImage(logoImage);
        logoImageView.setPreserveRatio(true);
    }

    /**
     * Used to notify {@link GameSelectionController} about updates on {@link ViewState}.
     * @param viewState the new {@link ViewState}
     */
    @Override
    public void notify(ViewState viewState) {
        if(viewState == ViewState.DISCONNECT){
            super.getClientController().getListenersManager().removeListener(this);
            super.notifyPossibleDisconnection(this.stackPane);
        }
    }

    /**
     * Used to notify {@link GameSelectionController} about events concerning
     * game handling (such as available games or game creation).
     * @param type the {@link GameHandlingEvents} type of the event
     * @param varArgs variable {@link String} arguments
     */
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