package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class LoginController extends AbstractController implements PlayerCreationListener, StateListener, GameHandlingListener {

    @FXML
    private TextField loginTextField;
    @FXML
    private Button loginButton;

    protected LoginController(AbstractController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.PLAYER_CREATION_LISTENER, this);
    }

    public void initialize(){
        loginButton.setOnAction((event) -> {
            String username = loginTextField.getText();
            System.out.println(username);
            super.getCommandParser().createPlayer(username);
        });
    }

    @Override
    public void notifyPlayerCreation(String name) {
        super.getClientController().getListenersManager().removeListener(ListenerType.PLAYER_CREATION_LISTENER, this);

        super.changeToNextScene(SceneStatesEnum.GameSelectionScene);
    }

    @Override
    public void notifyPlayerCreationError(String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText(error);
            alert.showAndWait();
        });
    }

    @Override
    public void notify(ViewState viewState) {
    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {
    }
}
