package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GameSelectionController extends AbstractController implements StateListener, GameHandlingListener, Initializable {

    @FXML
    private ChoiceBox<Integer> numPlayerBox;
    private Integer[] possibleNumPlayer = {2,3,4};
    @Override
    public void notify(ViewState viewState) {

    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        numPlayerBox.getItems().addAll(possibleNumPlayer);
        numPlayerBox.setValue(2);
    }
}
