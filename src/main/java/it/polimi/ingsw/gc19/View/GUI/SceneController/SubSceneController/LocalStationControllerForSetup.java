package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class LocalStationControllerForSetup extends LocalStationController implements SetupListener {

    public LocalStationControllerForSetup(AbstractController controller, String nickOwner) {
        super(controller, nickOwner);

        this.nickOwner = nickOwner;

        super.getClientController().getListenersManager().attachListener(ListenerType.SETUP_LISTENER, this);
    }

    public void initialize(){
        super.initialize();
    }

    @Override
    public void notify(SetupEvent type) {
        Platform.runLater(() -> {
            switch (type){
                case SetupEvent.ACCEPTED_COLOR -> super.initializePawns();
                case SetupEvent.ACCEPTED_INITIAL_CARD -> super.initializeGameArea();
                case SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD -> super.initializeCards();
            }
        });

    }

    @Override
    public void notify(SetupEvent type, String error) {

    }
}
