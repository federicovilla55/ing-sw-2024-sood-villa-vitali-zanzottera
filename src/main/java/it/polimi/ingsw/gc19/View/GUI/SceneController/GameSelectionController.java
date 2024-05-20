package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
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
            if(name != null) {
                System.out.println(gameName);
                super.getClientController().createGame(name, numPlayer);
            }
        });
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
                /*try{
                    System.out.println("%%%%%% " + getLocalModel());
                    //changeToNextScene(SceneStatesEnum.SETUP_SCENE);

                    FXMLLoader loader = new FXMLLoader();
                    SetupController controller = new SetupController();
                    loader.setLocation(getClass().getResource("/fxml/SetupScene.fxml"));
                    loader.setController(controller);
                    controller.setLocalModel(this.getLocalModel());
                    controller.setClientController(this.getClientController());
                    controller.setCommandParser(this.getCommandParser());

                    System.out.println(((SetupController) loader.getController()).getLocalModel());

                    controller.stage = this.stage;
                    controller.attachToListener(SceneStatesEnum.SETUP_SCENE);
                    controller.setToView();
                    controller.setSceneStatesEnum(SceneStatesEnum.SETUP_SCENE);
                    //this.getClientController.getListenersManager().removeListener(this);
                    this.removeListener();

                    System.out.println("oooooooooooooooo");

                    Parent root = loader.load();

                    assert root != null;

                    this.stage.close();

                    //if(controller instanceof SetupController) ((SetupController) controller).init();
                    this.stage.setScene(new Scene(root));
                    //this.stage.setMaximized(true);
                    this.stage.show();
                }
                catch (IOException ignored) {
                    System.out.println(ignored.getMessage());
                }*/
                changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            }
            case GameHandlingEvents.JOINED_GAMES -> System.out.println("You have been registered to game named '" + varArgs.getFirst() + "'.");
            case GameHandlingEvents.AVAILABLE_GAMES -> updateAvalaibleGames(new ArrayList<>(varArgs));
        }
        System.out.println();
    }

}
