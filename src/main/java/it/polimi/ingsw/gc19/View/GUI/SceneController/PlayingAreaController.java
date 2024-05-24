package it.polimi.ingsw.gc19.View.GUI.SceneController;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.util.List;
import java.util.Objects;

public class PlayingAreaController extends AbstractController {

    private static final double PIXEL_WIDTH = 807.04;
    private static final double PIXEL_HEIGHT = 328.04;

    @FXML
    private StackPane centerPane;

    @FXML
    private GridPane cardGrid;

    public PlayingAreaController(ClientController controller, CommandParser parser, Stage stage) {
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

        centerPane.setPrefSize(1920,1080);

        //find first and last row with a card placed
        int firstRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).min().orElse(0);
        int firstCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).min().orElse(0);
        int lastRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).max().orElse(0);
        int lastCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).max().orElse(0);

        cardGrid.getChildren().clear();

        DoubleProperty widthProperty = new SimpleDoubleProperty();
        DoubleProperty heightProperty = new SimpleDoubleProperty();
        DoubleProperty cellWidthProperty = new SimpleDoubleProperty();
        DoubleProperty cellHeightProperty = new SimpleDoubleProperty();
        DoubleProperty cardWidthProperty = new SimpleDoubleProperty();
        DoubleProperty cardHeightProperty = new SimpleDoubleProperty();

        double magnification = 1.0;

        widthProperty.bind(Bindings.min(
                centerPane.widthProperty().multiply(magnification), centerPane.heightProperty().multiply(PIXEL_WIDTH).divide(PIXEL_HEIGHT).multiply(magnification)));
        heightProperty.bind(Bindings.min(
                centerPane.heightProperty().multiply(magnification), centerPane.widthProperty().multiply(PIXEL_HEIGHT).divide(PIXEL_WIDTH).multiply(magnification)));

        cellWidthProperty.bind(widthProperty.divide(lastCol - firstCol + 3));
        cellHeightProperty.bind(heightProperty.divide(lastRow - firstRow + 3));

        cardWidthProperty.bind(cellWidthProperty.multiply(1.28));
        cardHeightProperty.bind(cellHeightProperty.multiply(1.694915254));

        for (int i = firstRow - 1; i <= lastRow + 1; i++) {
            RowConstraints row = new RowConstraints();
            cardGrid.getRowConstraints().add(row);
            row.setValignment(VPos.CENTER);
            row.prefHeightProperty().bind(cellHeightProperty);
            row.minHeightProperty().bind(row.prefHeightProperty());
            row.maxHeightProperty().bind(row.prefHeightProperty());
        }

        for (int i = firstCol - 1; i <= lastCol + 1; i++) {
            ColumnConstraints col = new ColumnConstraints();
            cardGrid.getColumnConstraints().add(col);
            col.setHalignment(HPos.CENTER);
            col.prefWidthProperty().bind(cellWidthProperty);
            col.minWidthProperty().bind(col.prefWidthProperty());
            col.maxWidthProperty().bind(col.prefWidthProperty());
        }

        cardGrid.setGridLinesVisible(true);

        for (Tuple<PlayableCard, Tuple<Integer, Integer>> card : placedCardSequence) {

            ImageView cardImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + card.x().getCardCode() + "_" +
                    (card.x().getCardOrientation() == CardOrientation.UP ? "front" : "back")
                    + ".jpg")).toExternalForm()));

            cardImage.fitWidthProperty().bind(cardWidthProperty);
            cardImage.fitHeightProperty().bind(cardHeightProperty);

            cardGrid.add(cardImage, card.y().y() - firstCol + 1, card.y().x() - firstRow + 1);
        }

    }
}
