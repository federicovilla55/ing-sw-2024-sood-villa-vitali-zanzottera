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

    protected AbstractController(ClientController controller, CommandParser parser, Stage stage){
        this.clientController = controller;
        this.commandParser = parser;
        this.localModel = new LocalModel();
        this.stage = stage;
    }

    protected AbstractController(AbstractController controller){
        this.localModel = controller.clientController.getLocalModel();

        this.clientController = controller.clientController;
        this.commandParser = controller.commandParser;

        this.stage = controller.stage;

        this.clientController.setView(this);
    }

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

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void setLocalModel(LocalModel localModel){this.localModel = localModel; }

    public void setCommandParser(CommandParser commandParser) {
        this.commandParser = commandParser;
    }
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
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

    public void changeToNextScene(SceneStatesEnum nextScenePath) {

        try{
            FXMLLoader loader = new FXMLLoader();
            AbstractController controller;
            switch (nextScenePath){
                case LoginScene -> controller = new LoginController(this);
                case GameSelectionScene -> controller = new GameSelectionController(this);
                case NewConfigurationScene -> controller = new NewConfigurationController(this);
                case SETUP_SCENE -> controller = new SetupController(this);
                default -> controller = null;
            }

            loader.setLocation(new File(nextScenePath.value()).toURL());
            loader.setController(controller);

            Platform.runLater(() -> {
                Parent root = null;
                try {
                    root = loader.load();

                    this.stage.setScene(new Scene(root));
                    //this.stage.setMaximized(true);
                    this.stage.show();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch (IOException ignored) {
            System.out.println(ignored.getMessage());
        }
    }
}
