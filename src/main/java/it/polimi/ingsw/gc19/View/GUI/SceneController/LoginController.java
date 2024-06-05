package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class LoginController extends GUIController implements PlayerCreationListener, StateListener {

    @FXML
    private TextField loginTextField;
    @FXML
    private Button loginButton;
    @FXML
    private StackPane stackPane;
    @FXML
    private AnchorPane anchorPane;
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

    public void initialize(){
        loginButton.setOnAction((event) -> {
            String username = loginTextField.getText();
            System.out.println(username);
            super.getCommandParser().createPlayer(username);
        });

        contentVBox.spacingProperty().bind(super.getStage().heightProperty().divide(10));
        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(4));

        loadLogo();

        double fontSizeFactor = 0.04;
        titleLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() * fontSizeFactor),
                super.getStage().heightProperty()
        ));

        loginButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 40),
                super.getStage().heightProperty()
        ));

        loginTextField.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() * fontSizeFactor),
                super.getStage().heightProperty()
        ));

        super.setBackground(anchorPane, false);
    }

    private void loadLogo() {
        Image logoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("resources/images/logo.png")));
        logoImageView.setImage(logoImage);
        logoImageView.setPreserveRatio(true);
    }


    @Override
    public void notifyPlayerCreation(String name) {
        super.getClientController().getListenersManager().removeListener(this);

        super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
    }

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
