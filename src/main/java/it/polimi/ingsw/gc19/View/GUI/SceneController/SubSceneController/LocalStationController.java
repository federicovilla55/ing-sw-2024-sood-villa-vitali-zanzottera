package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocalStationController extends AbstractController{

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

    private final Translate translate;

    private double startX, startY;
    private double anchorX, anchorY;
    private double translateAnchorX, translateAnchorY;

    private double scale = 1.0;
    private final double delta = 1.1;

    private final double CARD_PIXEL_WIDTH = 832.0;
    private final double CARD_PIXEL_HEIGHT = 558.0;

    private final ArrayList<ImageView> renderedCars;

    public LocalStationController(AbstractController controller,String nickOwner) {
        super(controller);

        this.nickOwner = nickOwner;

        this.translate = new Translate();

        this.renderedCars = new ArrayList<>();
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

        //super.getStage().setHeight(0.4 * super.getStage().getWidth() + 800);

        //super.getStage().minHeightProperty().bind(super.getStage().widthProperty().multiply(0.4).add(257));

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

                this.leftVBox.getChildren().forEach(this::makeNodeDraggable);
            }
        }
        else{
            this.leftVBox.getChildren().clear();

            for(var v : ((OtherStation) this.getLocalModel().getStations().get(this.nickOwner)).getBackCardHand()){
                this.leftVBox.getChildren().add(factoryUnswappableCard(v.x(), v.y()));
            }
        }
    }

    private void makeNodeDraggable(Node node){
        node.setOnMousePressed(e -> {
            startX = e.getSceneX() - node.getTranslateX();
            startY = e.getSceneY() - node.getTranslateY();

            /*if(!renderedCars.isEmpty()){
                CardButton button = (CardButton) e.getSource();
                System.out.println(scale);
                button.getSide().fitWidthProperty().multiply(scale);
            }*/
        });

        node.setOnMouseDragged(e -> {
            node.setTranslateX(e.getSceneX() - startX);
            node.setTranslateY(e.getSceneY() - startY);
        });

        node.setOnMouseReleased(e -> {
            // Get the scene coordinates where the card is released
            double sceneX = e.getSceneX();
            double sceneY = e.getSceneY();

            // Convert scene coordinates to GridPane local coordinates
            Point2D localCoords = cardGrid.sceneToLocal(sceneX, sceneY);

            // Determine the cell in the GridPane
            int column = (int) (localCoords.getX() / (cardGrid.getWidth() / cardGrid.getColumnCount()));
            int row = (int) (localCoords.getY() / (cardGrid.getHeight() / cardGrid.getRowCount()));

            // Ensure the calculated cell is within bounds
            if (localCoords.getX() >= 0 && column < cardGrid.getColumnCount() && localCoords.getY() >= 0 && row < cardGrid.getRowCount()) {
                System.out.println("column: " + column + ", row: " + row);
            } else {
                // Handle the case where the drop is outside the gridPane
                System.out.println("Dropped outside of grid");
            }

            node.setTranslateX(0);
            node.setTranslateY(0);
        });
    }

    private ImageView factoryUnswappableCard(Symbol symbol, PlayableCardType type){
        ImageView imageView = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource("/images/back/" + type.toString().toLowerCase() + "_" + symbol.toString().toLowerCase() + ".jpg"))
                       .toExternalForm()));
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(12.8));
        imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(7.2));
        clipCardImage(imageView);
        //imageView.setFitWidth(200);

        return imageView;
    }

    protected void initializeGameArea(){
        if(!this.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence().isEmpty()){
            ArrayList<Tuple<PlayableCard, Tuple<Integer, Integer>>> sequence = new ArrayList<>(List.of(new Tuple<>(this.getLocalModel().getPersonalStation().getCardsInHand().get(1), new Tuple<>(26, 26)),
                                                                                                       new Tuple<>(this.getLocalModel().getPersonalStation().getCardsInHand().get(2), new Tuple<>(27, 27))));
            sequence.addAll(super.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence());
            this.buildCardGrid(sequence);
        }
    }

    private ImageView pawnFactory(String name){
        ImageView color = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/pawns/" + name.toLowerCase() + "_pawn.png")).toExternalForm()));
        color.setPreserveRatio(true);
        color.fitWidthProperty().bind(super.getStage().widthProperty().divide(50));

        return color;
    }

    public void buildCardGrid(List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        //find first and last row with a card placed
        int firstRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).min().orElse(0);
        int firstCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).min().orElse(0);
        int lastRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).max().orElse(0);
        int lastCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).max().orElse(0);

        //remove all cards from the grid
        cardGrid.getChildren().clear();

        //create resize dimension properties
        DoubleProperty cellWidthProperty = new SimpleDoubleProperty();
        DoubleProperty cellHeightProperty = new SimpleDoubleProperty();

        for(var t : placedCardSequence){
            ImageView cardImage = new ImageView(
                    new Image(Objects.requireNonNull(
                            getClass().getResource("/images/" + t.x().getCardCode() + "_" + (t.x().getCardOrientation() == CardOrientation.UP ? "front" : "back") + ".jpg"))
                                     .toExternalForm()));

            //keep card aspect ratio
            cardImage.setPreserveRatio(true);

            cardImage.fitWidthProperty().bind(super.getStage().widthProperty().divide(12.8));

            clipCardImage(cardImage);

            renderedCars.add(cardImage);
        }

        double CORNER_PIXEL_WIDTH = 184.0;
        cellWidthProperty.bind(renderedCars.getFirst()
                                           .fitWidthProperty()
                                           .multiply(1 - CORNER_PIXEL_WIDTH / CARD_PIXEL_WIDTH));

        double CORNER_PIXEL_HEIGHT = 227.0;
        cellHeightProperty.bind(renderedCars.getFirst()
                                            .fitWidthProperty()
                                            .multiply(CARD_PIXEL_HEIGHT / CARD_PIXEL_WIDTH)
                                            .multiply(1 - CORNER_PIXEL_HEIGHT / CARD_PIXEL_HEIGHT));

        //set dimensions of rows and columns
        for (int i = firstRow - 1; i <= lastRow + 1; i++) {
            RowConstraints row = new RowConstraints();
            row.setValignment(VPos.CENTER);
            row.prefHeightProperty().bind(cellHeightProperty);
            row.minHeightProperty().bind(cellHeightProperty);
            row.maxHeightProperty().bind(cellHeightProperty);
            cardGrid.getRowConstraints().add(row);
        }

        for (int i = firstCol - 1; i <= lastCol + 1; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHalignment(HPos.CENTER);
            col.prefWidthProperty().bind(cellWidthProperty);
            col.minWidthProperty().bind(cellWidthProperty);
            col.maxWidthProperty().bind(cellWidthProperty);
            cardGrid.getColumnConstraints().add(col);
        }

        cardGrid.setGridLinesVisible(true);

        for (int i = 0; i < renderedCars.size(); i++) {
            cardGrid.add(renderedCars.get(i), placedCardSequence.get(i).y().y() - firstCol + 1, placedCardSequence.get(i).y().x() - firstRow + 1);
        }

        makeGridPaneDraggable();

        makeCenterPaneScrollable();
    }

    private void clipCardImage(ImageView cardImage){
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(cardImage.fitWidthProperty());
        rectangle.heightProperty().bind(cardImage.fitWidthProperty().multiply(CARD_PIXEL_HEIGHT / CARD_PIXEL_WIDTH));

        double CORNER_RADIUS = 27.0;
        rectangle.arcWidthProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));
        rectangle.arcHeightProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));

        cardImage.setClip(rectangle);
    }

    private void makeGridPaneDraggable(){
        centerPane.setStyle("""
                                -fx-padding: 5;
                                -fx-border-color: black;
                                -fx-border-style: solid inside;                                                                
                            """);

        cardGrid.getTransforms().add(translate);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(this.centerPane.widthProperty());
        clip.heightProperty().bind(this.centerPane.heightProperty());

        centerPane.setClip(clip);

        borderPane.getCenter().setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            translateAnchorX = translate.getX();
            translateAnchorY = translate.getY();
        });

        borderPane.getCenter().setOnMouseDragged(event -> {
            translate.setX(translateAnchorX + event.getSceneX() - anchorX);
            translate.setY(translateAnchorY + event.getSceneY() - anchorY);
        });
    }

    private void makeCenterPaneScrollable(){
        Scale centerPaneScale = new Scale(scale, scale);
        this.cardGrid.getTransforms().add(centerPaneScale);

        this.centerPane.setOnScroll(event -> {

            double factor;

            if(event.getDeltaY() != 0) {
                if (event.getDeltaY() > 0) {
                    factor = delta;
                    scale = scale * delta;
                }
                else {
                    factor = 1 / delta;
                    scale = scale / delta;
                }

                translate.setX(translate.getX() - (factor - 1) * event.getSceneX());
                translate.setY(translate.getY() - (factor - 1) * event.getSceneY());

                centerPaneScale.setX(scale);
                centerPaneScale.setY(scale);
            }

        });
    }

}
