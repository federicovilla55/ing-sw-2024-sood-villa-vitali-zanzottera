package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.List;

public class LoginController extends AbstractController implements PlayerCreationListener, StateListener, GameHandlingListener {

    @FXML
    TextField loginTextField;
    @FXML
    public void LoginPress(ActionEvent e) {
        String username = loginTextField.getText();
        System.out.println(username);
        super.getCommandParser().createPlayer(username);
    }

    @Override
    public void notifyPlayerCreation(String name) {
        super.changeToNextScene(SceneStatesEnum.GameSelectionScene);

    }

    @Override
    public void notifyPlayerCreationError(String error) {
        System.out.println("Not Success");
    }

    @Override
    public void notify(ViewState viewState) {
    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {
    }
}
