package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GameSelectionController extends AbstractController implements StateListener, GameHandlingListener, Initializable {

    @FXML
    private ChoiceBox<Integer> numPlayerBox;
    private Integer[] possibleNumPlayer = {2,3,4};

    @FXML
    private ListView<String> availableGamesList;
    @Override
    public void notify(ViewState viewState) {

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
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            super.getClientController().availableGames();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
