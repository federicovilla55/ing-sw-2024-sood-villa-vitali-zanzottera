package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.Disconnect;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class PlayingAreaController extends AbstractController {

    @FXML
    private GridPane cardGrid;

    public PlayingAreaController(ClientController controller, CommandParser parser, Stage stage){
        super(controller, parser, stage);

        super.getClientController().getListenersManager().attachListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    public PlayingAreaController(AbstractController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    public void setCardGrid(List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        System.out.println("set up cards in grid");

        //find first and last row with a card placed
        int firstRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).min().orElse(0);
        int firstCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).min().orElse(0);

        cardGrid.setGridLinesVisible(false);

        for(Tuple<PlayableCard, Tuple<Integer, Integer>> card : placedCardSequence) {

            StackPane.setMargin(cardGrid, new Insets(44, 36, 44, 36));

            ImageView cardImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + card.x().getCardCode() + "_" +
                    (card.x().getCardOrientation() == CardOrientation.UP ? "front" : "back")
                    + ".jpg")).toExternalForm()));

            GridPane.setMargin(cardImage, new Insets(-40, -32, -40, -32));

            cardImage.setPreserveRatio(true);
            cardImage.setFitWidth(300);

            cardGrid.add(cardImage, card.y().y() - firstCol, card.y().x() - firstRow);
        }
    }
}
