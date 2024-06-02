package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.View.GUI.GUISettings;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.Utils.GoalCardButton;
import it.polimi.ingsw.gc19.View.GUI.Utils.PlayableCardButton;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardImageLoader;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.*;

import static it.polimi.ingsw.gc19.View.GUI.GUISettings.*;

public class LocalStationController extends AbstractController implements StationListener, SetupListener {

    @FXML
    private StackPane centerPane;
    @FXML
    private GridPane cardGrid;
    @FXML
    private VBox leftVBox, rightVBox;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button center, rescale;

    private final String nickOwner;

    private Translate translate;
    private Scale centerPaneScale;

    private final ImageView bluePawnImageView, redPawnImageView, greenPawnImageView, yellowPawnImageView, blackPawnImageView;

    private double startX, startY;
    private double anchorX, anchorY;
    private double translateAnchorX, translateAnchorY;
    private int absGridRow, absGridCol;

    private double scale = 1.0;
    private final double delta = 1.1;

    private final ArrayList<ImageView> renderedCards;

    private PlayableCardButton draggedCard = null;

    public LocalStationController(AbstractController controller,String nickOwner) {
        super(controller);

        bluePawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/blue_pawn.png"))));
        redPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/red_pawn.png"))));
        greenPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/green_pawn.png"))));
        yellowPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/yellow_pawn.png"))));
        blackPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pawns/black_pawn.png"))));

        this.nickOwner = nickOwner;

        this.renderedCards = new ArrayList<>();

        super.getClientController().getListenersManager().attachListener(ListenerType.SETUP_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
    }

    @FXML
    private void initialize(){
        this.translate = new Translate();
        this.centerPaneScale = new Scale(scale, scale);

        this.centerPane.getTransforms().clear();

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
        this.centerPane.prefWidthProperty().bind(this.borderPane.prefWidthProperty().subtract(leftVBox.widthProperty()).subtract(rightVBox.widthProperty()));
        this.centerPane.minHeightProperty().bind(this.centerPane.prefHeightProperty());
        this.centerPane.minWidthProperty().bind(this.centerPane.prefWidthProperty());
        this.centerPane.prefHeightProperty().addListener((observable, oldValue, newValue) -> {
            this.centerPane.setMaxSize(this.centerPane.prefWidthProperty().get(), this.centerPane.prefHeightProperty().get());
        });
        this.centerPane.prefWidthProperty().addListener((observable, oldValue, newValue) -> {
            this.centerPane.setMaxSize(this.centerPane.prefWidthProperty().get(), this.centerPane.prefHeightProperty().get());
        });

        ((VBox) this.borderPane.getLeft()).prefWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO).add(30));
        ((VBox) this.borderPane.getLeft()).maxWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO).add(30));
        ((VBox) this.borderPane.getLeft()).minWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO).add(30));
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
                        new GoalCardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardInStation(), super.getStage(), (double) 1 / GUISettings.WIDTH_RATIO, (double) 1 / GUISettings.HEIGHT_RATIO));
            }

            this.leftVBox.getChildren().clear();

            for(PlayableCard p : List.copyOf(this.getLocalModel().getPersonalStation().getCardsInHand())){
                PlayableCardButton button = new PlayableCardButton(p, super.getStage(), (double) 1 / GUISettings.WIDTH_RATIO, (double) 1 / GUISettings.HEIGHT_RATIO);

                button.setOnMouseClicked(button.getDefaultMouseClickedHandler());

                this.leftVBox.getChildren().add(button);

                this.leftVBox.getChildren().forEach(this::makeCardDraggable);
            }
        }
        else{
            this.leftVBox.getChildren().clear();

            for(var v : List.copyOf(((OtherStation) this.getLocalModel().getStations().get(this.nickOwner)).getBackCardHand())){
                this.leftVBox.getChildren().add(factoryUnswappableCard(v.x(), v.y()));
            }
        }
    }

    private void checkIStackPaneContainsImage(MouseEvent mouse){
        Point2D mousePosition = new Point2D(mouse.getSceneX(), mouse.getSceneY());

        Point2D upperLeftCornerStack = new Point2D(this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMinX(),
                                                   this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMinY());
        Point2D lowerRightCornerStack = new Point2D(this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMaxX(),
                                                    this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMaxY());

        System.out.println((mousePosition.getX() < lowerRightCornerStack.getX() && mousePosition.getX() > upperLeftCornerStack.getX()));
        System.out.println(mousePosition.getY() < lowerRightCornerStack.getY() && mousePosition.getY() > upperLeftCornerStack.getY());

        if((mousePosition.getX() < lowerRightCornerStack.getX() && mousePosition.getX() > upperLeftCornerStack.getX()) &&
                (mousePosition.getY() < lowerRightCornerStack.getY() && mousePosition.getY() > upperLeftCornerStack.getY())){
            this.draggedCard.getSide().fitWidthProperty().unbind();
            this.draggedCard.getSide().fitWidthProperty().bind(super.getStage().widthProperty()
                                                                     .divide(GUISettings.WIDTH_RATIO)
                                                                     .multiply(scale));
        }
        else{
            this.draggedCard.getSide().fitWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO));
        }
    }

    private void makeCardDraggable(Node node){
        node.setOnMousePressed(e -> {
            //if(super.getClientController().getState() == ViewState.SETUP || super.getClientController().getState() == ViewState.END) return;

            //always center card with respect to mouse
            node.setTranslateX(node.getTranslateX() + e.getSceneX() - node.localToScene(0,0).getX() - node.getBoundsInLocal().getWidth()/2);
            node.setTranslateY(node.getTranslateY() + e.getSceneY() - node.localToScene(0,0).getY() - node.getBoundsInLocal().getHeight()/2);
            startX = e.getSceneX() - node.getTranslateX();
            startY = e.getSceneY() - node.getTranslateY();
        });

        node.setOnMouseDragged(e -> {
            //if(super.getClientController().getState() == ViewState.SETUP || super.getClientController().getState() == ViewState.END) return;

            node.setTranslateX(e.getSceneX() - startX);
            node.setTranslateY(e.getSceneY() - startY);

            this.draggedCard = (PlayableCardButton) node;

            this.checkIStackPaneContainsImage(e);
        });

        node.setOnMouseReleased(e -> {
            //if(super.getClientController().getState() == ViewState.SETUP || super.getClientController().getState() == ViewState.END) return;

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
                PlayableCard toPlace = ((PlayableCardButton) node).getCard();
                int localModelX = absGridRow + row;
                int localModelY = absGridCol + column;
                System.out.println("row: " + localModelX + ", column: " + localModelY);

                //check if a card is already present at given position
                PlayableCard card = super.getLocalModel().getPersonalStation().getPlacedCardAtPosition(localModelX,localModelY);
                if(card != null) {
                    super.notifyGenericError("A card is already present at this position!");
                }
                else {
                    //try to find an anchor to place
                    PlayableCard anchor = null;
                    Iterator<Direction> directionIterator = Arrays.stream(Direction.values()).iterator();
                    Direction direction = null;
                    while (directionIterator.hasNext() && anchor == null ) {
                        direction = directionIterator.next();
                        anchor = super.getLocalModel().getPersonalStation().getPlacedCardAtPosition(localModelX - direction.getX(), localModelY - direction.getY());
                    }

                    if(anchor == null) {
                        super.notifyGenericError("There is no available anchor card!");
                    }
                    else {
                        if(getClientController().getState().equals(ViewState.PLACE) && super.getLocalModel().getPersonalStation().cardIsPlaceable(anchor,toPlace,direction)) {
                            super.getClientController().placeCard(toPlace.getCardCode(),anchor.getCardCode(),direction,toPlace.getCardOrientation());
                            return;
                        }
                        else {
                            super.notifyGenericError("This card is not placeable here!");
                        }
                    }

                }
            } else {
                // Handle the case where the drop is outside the gridPane
                System.out.println("Dropped outside of grid");
            }

            node.setTranslateX(0);
            node.setTranslateY(0);
        });
    }

    private ImageView factoryUnswappableCard(Symbol symbol, PlayableCardType type){
        ImageView imageView = new ImageView(CardImageLoader.getBackImage(symbol,type));
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO));
        imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(GUISettings.HEIGHT_RATIO));
        clipCardImage(imageView);

        return imageView;
    }

    protected void initializeGameArea(){
        if(!this.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence().isEmpty()){
            StackPane.setAlignment(this.center, Pos.TOP_RIGHT);
            StackPane.setAlignment(this.rescale, Pos.TOP_RIGHT);

            this.center.setVisible(true);
            this.rescale.setVisible(true);

            this.center.setOnMouseClicked((event) -> {
                this.translate.setX((-1) * translate.getX() / 2);
                this.translate.setY((-1) * translate.getY() / 2);
            });

            this.rescale.setOnMouseClicked(event -> {
                this.scale = 1.0;

                this.centerPaneScale.setX(1.0);
                this.centerPaneScale.setY(1.0);
            });

            this.buildCardGrid(super.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence());
        }
        else{
            this.center.setVisible(false);
            this.rescale.setVisible(false);
        }
    }

    private ImageView pawnFactory(String name){
        ImageView color = switch (name.toLowerCase()) {
            case "red" -> redPawnImageView;
            case "green" -> greenPawnImageView;
            case  "blue" -> bluePawnImageView;
            case "yellow" -> yellowPawnImageView;
            default -> blackPawnImageView;
        };
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

        //save current position of cell in upper left corner of view,
        absGridRow = firstRow - 1;
        absGridCol = firstCol - 1;

        //remove all cards from the grid
        cardGrid.getColumnConstraints().clear();
        cardGrid.getRowConstraints().clear();
        cardGrid.getChildren().clear();
        renderedCards.clear();

        //create resize dimension properties
        DoubleProperty cellWidthProperty = new SimpleDoubleProperty();
        DoubleProperty cellHeightProperty = new SimpleDoubleProperty();

        for(var t : List.copyOf(placedCardSequence)){
            ImageView cardImage = new ImageView(CardImageLoader.getImage(t.x(),t.x().getCardOrientation()));

            //keep card aspect ratio
            cardImage.setPreserveRatio(true);

            cardImage.fitWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO));

            clipCardImage(cardImage);

            renderedCards.add(cardImage);
        }

        cellWidthProperty.bind(renderedCards.getFirst()
                                           .fitWidthProperty()
                                           .multiply(1 - CORNER_PIXEL_WIDTH / CARD_PIXEL_WIDTH));

        cellHeightProperty.bind(renderedCards.getFirst()
                                            .fitWidthProperty()
                                            .multiply(CARD_PIXEL_HEIGHT / CARD_PIXEL_WIDTH)
                                            .multiply(1 - CORNER_PIXEL_HEIGHT / CARD_PIXEL_HEIGHT));

        cardGrid.prefWidthProperty().bind(cellWidthProperty.multiply(lastCol - firstCol + 3));
        cardGrid.prefHeightProperty().bind(cellHeightProperty.multiply(lastRow - firstRow + 3));
        cardGrid.maxWidthProperty().bind(cardGrid.prefWidthProperty());
        cardGrid.maxHeightProperty().bind(cardGrid.prefHeightProperty());

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

        cardGrid.setGridLinesVisible(false);
        cardGrid.setGridLinesVisible(true);

        for (int i = 0; i < renderedCards.size(); i++) {
            cardGrid.add(renderedCards.get(i), placedCardSequence.get(i).y().y() - firstCol + 1, placedCardSequence.get(i).y().x() - firstRow + 1);
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
        final double MAX_SCALE = 2;
        final double MIN_SCALE = 0.25;

        this.centerPane.setOnScroll(event -> {
            double factor;

            if(!this.cardGrid.getTransforms().contains(centerPaneScale)) this.cardGrid.getTransforms().add(centerPaneScale);

            if(event.getDeltaY() != 0) {
                if (event.getDeltaY() > 0) {
                    factor = delta;
                    if(scale * delta < MAX_SCALE) scale = scale * delta;
                }
                else {
                    factor = 1 / delta;
                    if(scale * factor > MIN_SCALE) scale = scale / delta;
                }

                translate.setX((translate.getX() - (factor - 1) * event.getSceneX()) / 2);
                translate.setY((translate.getY() - (factor - 1) * event.getSceneY()) / 2);

                centerPaneScale.setX(scale);
                centerPaneScale.setY(scale);
            }
        });
    }

    @Override
    public void notify(SetupEvent type) {
        Platform.runLater(() -> {
            if(type == SetupEvent.ACCEPTED_COLOR || type == SetupEvent.ACCEPTED_INITIAL_CARD || type == SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD){
                this.initialize();
            }
        });

    }

    @Override
    public void notify(SetupEvent type, String error) { }

    @Override
    public void notify(PersonalStation localStationPlayer) {
        Platform.runLater(() -> {
            if (localStationPlayer.getOwnerPlayer().equals(this.nickOwner)) initialize();
        });
    }

    @Override
    public void notify(OtherStation otherStation) {
        Platform.runLater(() -> {
            if (otherStation.getOwnerPlayer().equals(this.nickOwner)) initialize();
        });
    }

    @Override
    public void notifyErrorStation(String... varArgs){ }

}