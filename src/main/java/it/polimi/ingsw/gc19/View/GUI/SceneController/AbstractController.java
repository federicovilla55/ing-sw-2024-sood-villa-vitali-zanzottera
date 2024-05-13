package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.Listener;
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

public class AbstractController implements UI , Listener {
    private LocalModel localModel;
    private CommandParser commandParser;
    private ClientController clientController;

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

    public void attachToListener(){
        this.clientController.getListenersManager().attachListener(this);
    }

    public void setToView() {
        this.clientController.setView(this);
    }

    public void changeToNextScene(String nextScenePath) {
        File url = new File(nextScenePath);
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
        controller.attachToListener();
        controller.setToView();
        this.clientController.getListenersManager().removeListener(this);
        this.stage.setScene(new Scene(root));
        this.stage.show();
    }
}
