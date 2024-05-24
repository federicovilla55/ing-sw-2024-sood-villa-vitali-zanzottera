package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import java.util.List;
import java.util.Objects;

public class PlayingAreaController extends AbstractController {

    private static final double CARD_PIXEL_WIDTH = 832.0;
    private static final double CARD_PIXEL_HEIGHT = 558.0;
    private static final double CORNER_PIXEL_WIDTH = 184.0;
    private static final double CORNER_PIXEL_HEIGHT = 227.0;


    @FXML
    private StackPane centerPane;

    @FXML
    private GridPane cardGrid;

    public PlayingAreaController(ClientController controller, CommandParser parser, Stage stage) {
        super(controller, parser, stage);
    }

    public PlayingAreaController(AbstractController controller) {
        super(controller);
    }

    public void setCardGrid(List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        //find first and last row with a card placed
        int firstRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).min().orElse(0);
        int firstCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).min().orElse(0);
        int lastRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).max().orElse(0);
        int lastCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).max().orElse(0);

        int numOfRow = lastRow - firstRow + 3;
        int numOfCol = lastCol - firstCol + 3;

        // calculate aspect ratio of the grid
        final double ASPECT_RATIO = ((CARD_PIXEL_WIDTH - CORNER_PIXEL_WIDTH) / (CARD_PIXEL_HEIGHT - CORNER_PIXEL_HEIGHT)) * ((double) numOfCol / numOfRow);

        //remove all cards from the grid
        cardGrid.getChildren().clear();

        //create resize dimention properties
        DoubleProperty widthProperty = new SimpleDoubleProperty();
        DoubleProperty heightProperty = new SimpleDoubleProperty();
        DoubleProperty cellWidthProperty = new SimpleDoubleProperty();
        DoubleProperty cellHeightProperty = new SimpleDoubleProperty();
        DoubleProperty cardWidthProperty = new SimpleDoubleProperty();
        DoubleProperty cardHeightProperty = new SimpleDoubleProperty();

        heightProperty.bind(Bindings.min(
                centerPane.heightProperty(), centerPane.widthProperty().divide(ASPECT_RATIO)));
        widthProperty.bind(heightProperty.multiply(ASPECT_RATIO));

        cellHeightProperty.bind(heightProperty.divide(numOfRow));
        cellWidthProperty.bind(widthProperty.divide(numOfCol));

        cardWidthProperty.bind(cellWidthProperty.multiply(CARD_PIXEL_WIDTH / (CARD_PIXEL_WIDTH - CORNER_PIXEL_WIDTH)));
        cardHeightProperty.bind(cellHeightProperty.multiply(CARD_PIXEL_HEIGHT / (CARD_PIXEL_HEIGHT - CORNER_PIXEL_HEIGHT)));


        //set dimensions of rows and columns
        for (int i = firstRow - 1; i <= lastRow + 1; i++) {
            RowConstraints row = new RowConstraints();
            row.setValignment(VPos.CENTER);
            row.prefHeightProperty().bind(cellHeightProperty);
            row.minHeightProperty().bind(row.prefHeightProperty());
            row.maxHeightProperty().bind(row.prefHeightProperty());
            cardGrid.getRowConstraints().add(row);
        }

        for (int i = firstCol - 1; i <= lastCol + 1; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHalignment(HPos.CENTER);
            col.prefWidthProperty().bind(cellWidthProperty);
            col.minWidthProperty().bind(col.prefWidthProperty());
            col.maxWidthProperty().bind(col.prefWidthProperty());
            cardGrid.getColumnConstraints().add(col);
        }

        cardGrid.setGridLinesVisible(true);

        for (Tuple<PlayableCard, Tuple<Integer, Integer>> card : placedCardSequence) {

            ImageView cardImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/" + card.x().getCardCode() + "_" +
                    (card.x().getCardOrientation() == CardOrientation.UP ? "front" : "back")
                    + ".jpg")).toExternalForm()));

            //keep card aspect ratio
            cardImage.setPreserveRatio(true);

            cardImage.fitWidthProperty().bind(cardWidthProperty);
            cardImage.fitHeightProperty().bind(cardHeightProperty);

            cardGrid.add(cardImage, card.y().y() - firstCol + 1, card.y().x() - firstRow + 1);
        }

    }
}
