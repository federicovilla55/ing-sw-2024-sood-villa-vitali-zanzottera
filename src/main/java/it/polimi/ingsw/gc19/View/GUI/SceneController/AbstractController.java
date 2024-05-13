package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesConst;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.UI;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AbstractController implements UI , Listener {
    private LocalModel localModel;
    private CommandParser commandParser;
    private ClientController clientController;
    private SceneStatesConst scenePath;

    private Stage stage;

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
    public  void setScenePath(SceneStatesConst scenePath) {
        this.scenePath = scenePath;
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

    public SceneStatesConst getScenePath() {
        return scenePath;
    }

    public Stage getStage() {
        return stage;
    }

    public void attachToListener(){
        this.clientController.getListenersManager().attachListener(this);
    }

    public void setToView() {
        this.clientController.setView(this);
    }
}
