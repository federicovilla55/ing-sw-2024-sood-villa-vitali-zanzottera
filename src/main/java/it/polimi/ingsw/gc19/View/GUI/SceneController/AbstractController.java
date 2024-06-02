package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ClientState;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.GUIView;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.UI;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AbstractController implements UI , Listener {
    private LocalModel localModel;
    private CommandParser commandParser;
    private ClientController clientController;
    private Stage stage;

    private boolean isCloseEventHandlerAdded = false;

    protected AbstractController(ClientController controller, CommandParser parser, Stage stage){
        this.localModel = controller.getLocalModel();
        this.clientController = controller;
        this.commandParser = parser;
        this.stage = stage;
        this.clientController.setView(this);
    }

    protected AbstractController(AbstractController controller){
        this.localModel = controller.clientController.getLocalModel();

        this.clientController = controller.clientController;
        this.commandParser = controller.commandParser;

        this.stage = controller.stage;

        this.clientController.setView(this);

        this.isCloseEventHandlerAdded = controller.isCloseEventHandlerAdded;

        if(!this.isCloseEventHandlerAdded && this.clientController.getState() != ViewState.NOT_PLAYER){
            stage.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        }
    }

    public void closeWindowEvent(WindowEvent event) {
        if (this.clientController.getState() != ViewState.NOT_GAME &&
                this.clientController.getState() != ViewState.NOT_PLAYER &&
                this.clientController.getState() != ViewState.DISCONNECT) {
            Dialog<ButtonType> closeDialog = new Dialog<>();
            closeDialog.initOwner(stage.getOwner());
            closeDialog.setTitle("Closing Codex Naturalis");

            ButtonType lobbyButton = new ButtonType("Return to Lobby", ButtonBar.ButtonData.LEFT);
            ButtonType disconnectButton = new ButtonType("Disconnect", ButtonBar.ButtonData.RIGHT);

            closeDialog.getDialogPane().getButtonTypes().addAll(lobbyButton, disconnectButton);

            closeDialog.getDialogPane().setContentText("Do you want to close Codex Naturalis?");

            Optional<ButtonType> response = closeDialog.showAndWait();
            if (response.isPresent()) {
                if (response.get().getText().equals("Return to Lobby")) {
                    this.clientController.logoutFromGame();
                    this.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
                    event.consume(); // Prevent the window from closing
                } else if (response.get().getText().equals("Disconnect")) {
                    this.clientController.disconnect();
                }

                stage.getScene().getWindow().removeEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
            } else {
                event.consume();
            }
        }
    }


    @Override
    public void notifyGenericError(String errorDescription) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(stage.getScene().getWindow());
            alert.setTitle("Generic error");
            alert.setContentText(errorDescription);
            alert.showAndWait();
        });
    }

    @Override
    public void notify(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(stage.getScene().getWindow());
            alert.setTitle("Info");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void setStage(Stage stage){
        this.stage = stage;
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

    public ClientController getClientController() {
        return clientController;
    }

    public CommandParser getCommandParser() {
        return commandParser;
    }

    public LocalModel getLocalModel() {
        if(this.localModel == null){
            this.localModel = this.clientController.getLocalModel();
        }
        return localModel;
    }

    public Stage getStage() {
        return stage;
    }

    public void changeToNextScene(SceneStatesEnum nextScenePath) {
        try{
            FXMLLoader loader = new FXMLLoader();

            this.getClientController().getListenersManager().removeListener(this);

            AbstractController controller = getController(nextScenePath);

            loader.setLocation(new File(nextScenePath.value()).toURL());
            loader.setController(controller);

            Platform.runLater(() -> {
                Parent root = null;
                try {
                    root = loader.load();

                    Scene scene = new Scene(root);

                    this.stage.setScene(scene);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch (IOException ignored) {
            System.out.println(ignored.getMessage());
        }
    }

    public void setBackground(Pane pane, Boolean isDark){
        Image backgroundImage = null;
        String location = "";
        if(isDark) {
            location = "src/main/resources/images/background_dark.png";
        }else {
            location = "src/main/resources/images/background_light.png";
        }
        try {
            backgroundImage = new Image(new FileInputStream(location));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        BackgroundSize backgroundSize = new BackgroundSize(360, 360, false, false, false, false);
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);

        pane.setBackground(new Background(background));
    }

    @Nullable
    private AbstractController getController(SceneStatesEnum nextScenePath) {
        AbstractController controller;

        switch (nextScenePath){
            case LOGIN_SCENE -> controller = new LoginController(this);
            case GAME_SELECTION_SCENE -> controller = new GameSelectionController(this);
            case NEW_CONFIGURATION_SCENE -> controller = new NewConfigurationController(this);
            case SETUP_SCENE -> controller = new SetupController(this);
            case PLAYING_AREA_SCENE -> controller = new PlayingAreaController(this);
            default -> controller = null;
        }

        return controller;
    }

    protected void notifyPossibleDisconnection(StackPane stackPane){
        Platform.runLater(() -> {
            for (Node n : stackPane.getChildrenUnmodifiable()) {
                n.setOpacity(0.15);
            }

            getStage().getScene().setFill(javafx.scene.paint.Color.GREY);

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(new File("src/main/resources/fxml/ReconnectionWaitScene.fxml").toURL());
                ReconnectionWaitController controller = new ReconnectionWaitController(this);
                loader.setController(controller);

                StackPane waitStack = loader.load();

                stackPane.getChildren().add(waitStack);
                stackPane.requestLayout();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
