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
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class LocalStationControllerForSetup extends LocalStationController implements SetupListener {

    @FXML
    private VBox vBox;
    @FXML
    private HBox hBox;
    @FXML
    private BorderPane borderPane;

    private final String nickOwner;

    public LocalStationControllerForSetup(AbstractController controller, String nickOwner) {
        super(controller);

        this.nickOwner = nickOwner;

        super.getClientController().getListenersManager().attachListener(ListenerType.SETUP_LISTENER, this);

        initialize();
    }

    public String getNickOwner(){
        return this.nickOwner;
    }

    @FXML
    private void initialize(){

    }

    private void buildTab(){
        Platform.runLater(() -> {
            hBox.getChildren().clear();

            if(super.getLocalModel().getStations().get(nickOwner).getChosenColor() != null){
                hBox.getChildren().add(pawnFactory(super.getLocalModel().getStations().get(nickOwner).getChosenColor().toString()));
            }

            if(nickOwner.equals(this.getLocalModel().getFirstPlayer())){
                hBox.getChildren().add(pawnFactory("black"));
            }

            if(!this.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence().isEmpty()){
                this.borderPane.setCenter(new CardButton(this.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence().getFirst().x()));
            }
        });
    }

    private ImageView pawnFactory(String name){
        ImageView color = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/pawns/" + name.toLowerCase() + "_pawn.png")).toExternalForm()));
        color.setPreserveRatio(true);
        color.setFitHeight(25);

        return color;
    }

    @Override
    public void notify(SetupEvent type) {
        switch (type){
            case SetupEvent.ACCEPTED_COLOR, SetupEvent.ACCEPTED_INITIAL_CARD -> buildTab();
        }
    }

    @Override
    public void notify(SetupEvent type, String error) {

    }
}
