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
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class LoginController extends AbstractController implements PlayerCreationListener, StateListener {

    @FXML
    private TextField loginTextField;
    @FXML
    private Button loginButton;
    @FXML
    private StackPane stackPane;

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
        super.getClientController().getListenersManager().removeListener(this);

        if(viewState == ViewState.DISCONNECT){
            super.notifyPossibleDisconnection(stackPane);
        }
    }

}
