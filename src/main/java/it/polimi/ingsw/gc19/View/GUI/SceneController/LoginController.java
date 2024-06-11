package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * A scene controller. It manages user's login, e.g.
 * it lets user chose his nickname and displays
 * an {@link Alert} in case of errors.
 */
public class LoginController extends GUIController implements PlayerCreationListener, StateListener {

    @FXML
    private TextField loginTextField;
    @FXML
    private Button loginButton;
    @FXML
    private StackPane stackPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ImageView logoImageView;
    @FXML
    private VBox contentVBox;
    @FXML
    private Label titleLabel;

    protected LoginController(GUIController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.PLAYER_CREATION_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    /**
     * Initializes the scene.
     */
    public void initialize(){
        loginButton.setOnAction((event) -> {
            String username = loginTextField.getText();
            super.getCommandParser().createPlayer(username);
        });

        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(4));

        loadLogo();

        super.setBackground(borderPane, false);
    }

    /**
     * Loads Codex Naturalis' logo and places it in the scene
     */
    private void loadLogo() {
        Image logoImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/logo.png")));
        logoImageView.setImage(logoImage);
        logoImageView.setPreserveRatio(true);
    }

    /**
     * Used to notify to {@link LoginController} that the player
     * has been correctly created
     * @param name is the name of the player
     */
    @Override
    public void notifyPlayerCreation(String name) {
        super.getClientController().getListenersManager().removeListener(this);

        super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
    }

    /**
     * Used to notify to {@link LoginController} that an error has occurred
     * while trying to register user's nickname.
     * @param error a {@link String} description of the error
     */
    @Override
    public void notifyPlayerCreationError(String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(super.getStage().getScene().getWindow());
            alert.setTitle("Player creation error");
            alert.setContentText(error);
            alert.showAndWait();
        });
    }

    /**
     * Used to notify {@link LoginController} about events
     * concerning {@link ViewState}.
     * @param viewState the new {@link ViewState}
     */
    @Override
    public void notify(ViewState viewState) {
        switch (viewState){
            case ViewState.NOT_PLAYER -> super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
            case ViewState.NOT_GAME -> super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
            case ViewState.SETUP -> {
                super.setLocalModel(super.getClientController().getLocalModel());
                super.changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            }
            case ViewState.PICK, ViewState.PLACE, ViewState.OTHER_TURN, ViewState.PAUSE, ViewState.END -> {
                super.setLocalModel(super.getClientController().getLocalModel());
                super.changeToNextScene(SceneStatesEnum.PLAYING_AREA_SCENE);
            }
        }
    }

}