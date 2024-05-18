package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameSelectionController extends AbstractController implements StateListener, GameHandlingListener, Initializable {

    @FXML
    private ChoiceBox<Integer> numPlayerBox;
    private Integer[] possibleNumPlayer = {2,3,4};

    @FXML
    private ListView<String> availableGamesList;

    @FXML
    TextField gameNameField;

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
            case GameHandlingEvents.CREATED_GAME -> System.out.println("The requested game '" + varArgs.getFirst() + "' has been created!");
            case GameHandlingEvents.JOINED_GAMES -> System.out.println("You have been registered to game named '" + varArgs.getFirst() + "'.");
            case GameHandlingEvents.AVAILABLE_GAMES -> updateAvalaibleGames(new ArrayList<>(varArgs));
        }
        System.out.println();
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        numPlayerBox.getItems().addAll(possibleNumPlayer);
        numPlayerBox.setValue(2);
        Timer t = new java.util.Timer();
        t.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        GameSelectionController.super.getClientController().availableGames();
                        t.cancel();
                    }
                },
                1000
        );

    }
    @FXML
    public void onJoinPress(ActionEvent e) {
        String gameName = availableGamesList.getSelectionModel().getSelectedItem();
        if(gameName != null) {
            System.out.println(gameName);
            super.getClientController().joinGame(gameName);
        }
    }
    @FXML
    public void onCreatePress(ActionEvent e){
        String gameName = gameNameField.getText();
        int numPlayer = numPlayerBox.getSelectionModel().getSelectedItem();
        if(gameName != null) {
            super.getClientController().createGame(gameName,numPlayer);
        }
    }

}
