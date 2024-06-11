package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.UI;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.jetbrains.annotations.Nullable;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * A generic controller for GUI. All scene and sub-scene
 * controllers must extend this class.
 * It implements {@link UI} and {@link Listener} to interact
 * respectively with {@link LocalModel} and {@link ListenersManager}
 */
public class GUIController implements UI, Listener{

    /**
     * Connected {@link LocalModel}
     */
    private LocalModel localModel;

    /**
     * Connected {@link CommandParser}
     */
    private final CommandParser commandParser;

    /**
     * Connect {@link ClientController}
     */
    private ClientController clientController;

    /**
     * The {@link Stage} on which all scenes will be shown
     */
    private Stage stage;

    /**
     * Used to manage close window event handler
     */
    private boolean isCloseEventHandlerAdded = false;

    protected GUIController(ClientController controller, CommandParser parser, Stage stage){
        this.localModel = controller.getLocalModel();
        this.clientController = controller;
        this.commandParser = parser;
        this.stage = stage;
        this.clientController.setView(this);
    }

    protected GUIController(GUIController controller){
        this.localModel = controller.clientController.getLocalModel();

        this.clientController = controller.clientController;
        this.commandParser = controller.commandParser;

        this.stage = controller.stage;

        this.clientController.setView(this);

        this.isCloseEventHandlerAdded = controller.isCloseEventHandlerAdded;

        if(!this.isCloseEventHandlerAdded && this.clientController.getState() != ViewState.NOT_PLAYER){
            stage.getScene().getWindow().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
            this.isCloseEventHandlerAdded = true;
        }
    }

    /**
     * This class is uses to create a dialog when a player is in a game, and he presses the closing "X" in the window menù.
     * The dialog asks the player if he wants to quit the game and return to the lobby but keeping the nickname to join
     * another game or if he wants to quit the game and disconnect from the server too.
     * @param event a WindowEvent that represent the closing event, so when the "X" window button is pressed.
     */
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

            Window window = closeDialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(e -> {
                window.hide();
                e.consume();
                event.consume();
            });

            Optional<ButtonType> response = closeDialog.showAndWait();
            if (response.isPresent()) {
                if (response.get().getText().equals("Return to Lobby")) {
                    this.getClientController().logoutFromGame();
                    event.consume();
                } else if (response.get().getText().equals("Disconnect")) {
                    this.getClientController().disconnect();
                } else {
                    event.consume();
                }

                this.isCloseEventHandlerAdded = false;
                stage.getScene().getWindow().removeEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
            }
        }
    }

    /**
     * Used to notify a generic error. An {@link Alert}
     * containing error description is shown.
     * @param errorDescription the {@link String} description of the error
     */
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

    /**
     * Used to notify a generic message. An {@link Alert}
     * containing message description is shown.
     * @param message the {@link String} description of the message
     */
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

    /**
     * Setter for current {@link Stage}.
     * @param stage the {@link Stage} to be set.
     */
    public void setStage(Stage stage){
        this.stage = stage;
    }

    /**
     * Setter for current {@link LocalModel}
     * @param localModel the {@link LocalModel} to be set.
     */
    public void setLocalModel(LocalModel localModel){
        this.localModel = localModel;
    }

    /**
     * Setter for current {@link ClientController}
     * @param clientController the {@link ClientController} to be set.
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    /**
     * Getter for current {@link ClientController}
     * @return the current {@link ClientController}
     */
    public ClientController getClientController() {
        return clientController;
    }

    /**
     * Getter for current {@link CommandParser}
     * @return the current {@link CommandParser}
     */
    public CommandParser getCommandParser() {
        return commandParser;
    }

    /**
     * Getter for current {@link LocalModel}
     * @return the current {@link LocalModel}
     */
    public LocalModel getLocalModel() {
        if(this.localModel == null){
            this.localModel = this.clientController.getLocalModel();
        }
        return localModel;
    }

    /**
     * Getter for current {@link Stage}
     * @return the current {@link Stage}
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Sets the {@param nextScenePath} to the current {@link #stage}.
     * Handles maximization and visual bounds.
     * @param nextScenePath the {@link SceneStatesEnum} to be set to {@link Stage}
     */
    public void changeToNextScene(SceneStatesEnum nextScenePath) {
        FXMLLoader loader = new FXMLLoader();

        this.getClientController().getListenersManager().removeListener(this);

        GUIController controller = getController(nextScenePath);

        loader.setLocation(getClass().getClassLoader().getResource(nextScenePath.value()));
        loader.setController(controller);

        Platform.runLater(() -> {
            Parent root = null;
            try {
                root = loader.load();

                this.stage.getScene().setRoot(root);

                switch (nextScenePath){
                    case NEW_CONFIGURATION_SCENE,
                         OLD_CONFIGURATION_SCENE,
                         LOGIN_SCENE,
                         GAME_SELECTION_SCENE -> {

                        this.getStage().setResizable(false);
                        this.getStage().setMaximized(false);
                        this.getStage().sizeToScene();
                        this.getStage().getScene().getWindow().centerOnScreen();
                    }
                    case SETUP_SCENE, PLAYING_AREA_SCENE -> {

                        Screen screen = Screen.getPrimary();
                        Rectangle2D bounds = screen.getVisualBounds();
                        this.getStage().getScene().getRoot().prefWidth(bounds.getWidth());
                        this.getStage().getScene().getRoot().prefHeight(bounds.getHeight());
                        this.getStage().setWidth(bounds.getWidth());
                        this.getStage().setHeight(bounds.getHeight());

                        this.getStage().setResizable(true);
                        this.getStage().setMaximized(false);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setBackground(Pane pane, Boolean isDark){
        Image backgroundImage = null;
        String location = "";
        if(isDark) {
            location = "it/polimi/ingsw/gc19/images/background_dark.png";
        }else {
            location = "it/polimi/ingsw/gc19/images/background_light.png";
        }

        backgroundImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(location)));

        BackgroundSize backgroundSize = new BackgroundSize(360, 360, false, false, false, false);
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);

        pane.setBackground(new Background(background));
    }

    /**
     * Factory method for {@link GUIController} based on
     * {@param nextScenePath}
     * @param nextScenePath the {@link SceneStatesEnum} of the scene to load
     * @return the built {@link GUIController}
     */
    @Nullable
    private GUIController getController(SceneStatesEnum nextScenePath) {
        GUIController controller;

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

    /**
     * Instantiates a {@link ReconnectionWaitController}, set opacity
     * to all children of {@param stackPane} and loads a new reconnection scene.
     * @param stackPane the {@link StackPane} on top of which put the scene.
     */
    protected void notifyPossibleDisconnection(StackPane stackPane){
        Platform.runLater(() -> {
            for (Node n : stackPane.getChildrenUnmodifiable()) {
                n.setOpacity(0.15);
            }

            getStage().getScene().setFill(javafx.scene.paint.Color.GREY);

            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getClassLoader().getResource("it/polimi/ingsw/gc19/fxml/ReconnectionWaitScene.fxml"));
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