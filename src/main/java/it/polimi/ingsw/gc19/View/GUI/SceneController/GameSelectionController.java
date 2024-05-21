package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GameSelectionController extends AbstractController implements StateListener, GameHandlingListener {

    @FXML
    private Button joinButton, createButton;
    @FXML
    private TextField gameName;
    @FXML
    private ChoiceBox<Integer> numPlayerBox;

    private final Integer[] possibleNumPlayer = {2,3,4};
    @FXML
    private ListView<String> availableGamesList;

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
                System.out.println(gameName);
                super.getClientController().joinGame(gameName);
            }
        });

        createButton.setOnMouseClicked((event) -> {
            String name = gameName.getText();
            int numPlayer = numPlayerBox.getValue();
            if(name != null && !name.isEmpty()) {
                System.out.println(gameName);
                super.getClientController().createGame(name, numPlayer);
            }
        });
        super.getClientController().availableGames();
    }
    @Override
    public void notify(ViewState viewState) {
        System.out.println(viewState);
    }

    void updateAvalaibleGames(ArrayList<String> availableGames) {
        Platform.runLater(() -> {
        availableGamesList.setItems(FXCollections.observableArrayList(availableGames));
        });
    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {
        switch (type){
            case GameHandlingEvents.CREATED_GAME -> {
                super.getClientController().getListenersManager().removeListener(ListenerType.STATE_LISTENER, this);
                super.getClientController().getListenersManager().removeListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);

                changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            }
            case GameHandlingEvents.JOINED_GAMES -> {
                super.getClientController().getListenersManager().removeListener(ListenerType.STATE_LISTENER, this);
                super.getClientController().getListenersManager().removeListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);

                changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            }
            case GameHandlingEvents.AVAILABLE_GAMES -> updateAvalaibleGames(new ArrayList<>(varArgs));
        }
        System.out.println();
    }

}
