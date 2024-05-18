package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.UI;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class AbstractController implements UI , Listener {
    private LocalModel localModel;
    private CommandParser commandParser;
    private ClientController clientController;
    private Stage stage;

    private SceneStatesEnum sceneStatesEnum;

    @Override
    public void notifyGenericError(String errorDescription) {
        Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(errorDescription);
        alert.showAndWait();
        });
    }

    @Override
    public void notify(String message) {
        Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
        });
    }
    public void setLocalModel(LocalModel localModel){
        this.localModel = localModel;
    }
    public void setCommandParser(CommandParser commandParser) {
        this.commandParser = commandParser;
    }
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public CommandParser getCommandParser() {
        return commandParser;
    }

    public LocalModel getLocalModel() {
        return localModel;
    }
    public Stage getStage() {
        return stage;
    }

    public void attachToListener(SceneStatesEnum sceneStatesEnum){
        this.clientController.getListenersManager().attachListener(this);
        /*List<ListenerType> listToAttach = sceneStatesEnum.getListeners();
        for(ListenerType listenerType : listToAttach)
        {
            this.clientController.getListenersManager().attachListener(listenerType,this);
        }*/
    }

    public void removeListener() {
        this.clientController.getListenersManager().removeListener(this);
        /*List<ListenerType> listToAttach = this.sceneStatesEnum.getListeners();
        for(ListenerType listenerType : listToAttach)
        {
            this.clientController.getListenersManager().removeListener(listenerType,this);
        }*/
    }

    public void setToView() {
        this.clientController.setView(this);
    }

    public void setSceneStatesEnum (SceneStatesEnum ScenePath){
        this.sceneStatesEnum = ScenePath;
    }

    public void changeToNextScene(SceneStatesEnum nextScenePath) {
        File url = new File(nextScenePath.value());
        FXMLLoader loader = null;
        Parent root;
        try {
            loader = new FXMLLoader(url.toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AbstractController controller = loader.getController();
        controller.setCommandParser(this.getCommandParser());
        controller.setClientController(this.getClientController());
        controller.setStage(getStage());
        controller.attachToListener(nextScenePath);
        controller.setToView();
        controller.setSceneStatesEnum(nextScenePath);
        //this.clientController.getListenersManager().removeListener(this);
        this.removeListener();
        Platform.runLater(() -> {
        this.stage.setScene(new Scene(root));
        //this.stage.setMaximized(true);
        this.stage.show();
        });
    }
}
