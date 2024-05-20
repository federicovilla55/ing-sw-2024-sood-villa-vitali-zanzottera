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
    protected Stage stage;

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
        System.out.println(localModel);
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
        //this.getClientController.getListenersManager().attachListener(this);
        List<ListenerType> listToAttach = sceneStatesEnum.getListeners();
        for(ListenerType listenerType : listToAttach)
        {
            this.clientController.getListenersManager().attachListener(listenerType,this);
        }
    }

    public void removeListener() {
        //this.getClientController.getListenersManager().removeListener(this);
        System.out.println(sceneStatesEnum);
        List<ListenerType> listToAttach = this.sceneStatesEnum.getListeners();
        for(ListenerType listenerType : listToAttach) {
            this.clientController.getListenersManager().removeListener(listenerType,this);
        }
    }

    public void setToView() {
        this.clientController.setView(this);
    }

    public void setSceneStatesEnum (SceneStatesEnum ScenePath){
        this.sceneStatesEnum = ScenePath;
    }

    public void changeToNextScene(SceneStatesEnum nextScenePath) {
        /*File url = new File(nextScenePath.value());
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
        System.out.println("  -----  " + getLocalModel());
        controller.setLocalModel(this.getLocalModel());
        controller.setStage(getStage());
        controller.attachToListener(nextScenePath);
        controller.setToView();
        System.out.println(nextScenePath);
        controller.setSceneStatesEnum(nextScenePath);
        //this.getClientController.getListenersManager().removeListener(this);
        this.removeListener();

        //if(controller instanceof SetupController) ((SetupController) controller).init();
        Platform.runLater(() -> {
        this.stage.setScene(new Scene(root));
        //this.stage.setMaximized(true);
        this.stage.show();
        });*/

        try{
            System.out.println("%%%%%% " + getLocalModel());
            //changeToNextScene(SceneStatesEnum.SETUP_SCENE);

            FXMLLoader loader = new FXMLLoader();
            AbstractController controller;
            switch (nextScenePath){
                case LoginScene -> controller = new LoginController();
                case GameSelectionScene -> controller = new GameSelectionController();
                case NewConfigurationScene -> controller = new NewConfigurationController();
                case SETUP_SCENE -> controller = new SetupController();
                default -> controller = null;
            }
            System.out.println(controller.getClass());
            loader.setLocation(new File(nextScenePath.value()).toURL());
            loader.setController(controller);
            controller.setLocalModel(this.getLocalModel());
            controller.setClientController(this.getClientController());
            controller.setCommandParser(this.getCommandParser());

            //System.out.println(((SetupController) loader.getController()).getLocalModel());

            controller.stage = this.stage;
            controller.attachToListener(nextScenePath);
            controller.setToView();
            controller.setSceneStatesEnum(nextScenePath);
            //this.getClientController.getListenersManager().removeListener(this);
            this.removeListener();

            System.out.println("oooooooooooooooo");

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
