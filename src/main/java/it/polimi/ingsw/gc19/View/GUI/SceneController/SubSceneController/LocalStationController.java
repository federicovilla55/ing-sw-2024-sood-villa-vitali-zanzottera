package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

//questo controllore si occupa solo di stampare il colore della pedina e la stazione di gioco
public class LocalStationController extends AbstractController {

    @FXML
    protected StackPane centerPane;
    @FXML
    protected GridPane cardGrid;
    @FXML
    protected VBox leftVBox;
    @FXML
    protected VBox rightVBox;
    @FXML
    protected BorderPane borderPane;

    protected String nickOwner;

    public LocalStationController(AbstractController controller,String nickOwner) {
        super(controller);

        this.nickOwner = nickOwner;
    }

    @FXML
    protected void initialize(){
        this.leftVBox.getChildren().clear();
        this.rightVBox.getChildren().clear();

        initializePawns();
        initializeGameArea();
        initializeCards();

        this.leftVBox.spacingProperty().bind(this.borderPane.heightProperty().divide(10));
        this.rightVBox.spacingProperty().bind(this.borderPane.heightProperty().divide(10));

        this.centerPane.prefHeightProperty().bind(this.borderPane.prefHeightProperty());
        this.centerPane.prefWidthProperty().bind(this.borderPane.prefWidthProperty().multiply(0.80));
        this.centerPane.minHeightProperty().bind(this.centerPane.prefHeightProperty());
        this.centerPane.minWidthProperty().bind(this.centerPane.prefWidthProperty());
    }

    protected void initializePawns(){
        if(!this.nickOwner.equals(this.getLocalModel().getNickname())){
            this.rightVBox.getChildren().clear();
        }

        if(super.getLocalModel().getStations().get(nickOwner).getChosenColor() != null){
            rightVBox.getChildren().add(pawnFactory(super.getLocalModel().getStations().get(nickOwner).getChosenColor().toString()));
        }

        if(nickOwner.equals(this.getLocalModel().getFirstPlayer())){
            rightVBox.getChildren().add(pawnFactory("black"));
        }
    }

    protected void initializeCards(){
        if(this.nickOwner.equals(this.getLocalModel().getNickname())){
            if(super.getLocalModel().getPersonalStation().getPrivateGoalCardInStation() != null) {
                this.rightVBox.getChildren().add(
                        new CardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardInStation(), super.getStage(), (double) 1 / 12.8, (double) 1 / 7.2));
            }

            this.leftVBox.getChildren().clear();

            for(PlayableCard p : this.getLocalModel().getPersonalStation().getCardsInHand()){
                CardButton button = new CardButton(p, super.getStage(), (double) 1 / 12.8, (double) 1 / 7.2);

                button.setOnMouseClicked(button.getDefaultMouseClickedHandler());

                this.leftVBox.getChildren().add(button);
            }
        }
        else{
            this.leftVBox.getChildren().clear();

            for(var v : ((OtherStation) this.getLocalModel().getStations().get(this.nickOwner)).getBackCardHand()){
                this.leftVBox.getChildren().add(factoryUnswappableCard(v.x(), v.y()));
            }
        }
    }

    private ImageView factoryUnswappableCard(Symbol symbol, PlayableCardType type){
        ImageView imageView = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource("/images/back/" + type.toString().toLowerCase() + "_" + symbol.toString().toLowerCase() + ".jpg"))
                       .toExternalForm()));
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(12.8));
        imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(7.2));
        //imageView.setFitWidth(200);

        return imageView;
    }

    protected void initializeGameArea(){
        if(!this.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence().isEmpty()){
            this.setCardGrid(super.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence());
        }
    }

    private ImageView pawnFactory(String name){
        ImageView color = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/pawns/" + name.toLowerCase() + "_pawn.png")).toExternalForm()));
        color.setPreserveRatio(true);
        color.fitWidthProperty().bind(super.getStage().widthProperty().divide(50));

        return color;
    }

    public void setCardGrid(List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        final double CARD_PIXEL_WIDTH = 832.0;
        final double CARD_PIXEL_HEIGHT = 558.0;
        final double CORNER_PIXEL_WIDTH = 184.0;
        final double CORNER_PIXEL_HEIGHT = 227.0;

        //find first and last row with a card placed
        int firstRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).min().orElse(0);
        int firstCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).min().orElse(0);
        int lastRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).max().orElse(0);
        int lastCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).max().orElse(0);

        int numOfRow = lastRow - firstRow + 10;
        int numOfCol = lastCol - firstCol + 10;

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
