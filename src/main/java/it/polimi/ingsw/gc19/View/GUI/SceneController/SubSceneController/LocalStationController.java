package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.GUISettings;
import it.polimi.ingsw.gc19.View.GUI.SceneController.GUIController;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardImageLoader;
import it.polimi.ingsw.gc19.View.GUI.Utils.GoalCardButton;
import it.polimi.ingsw.gc19.View.GUI.Utils.PlayableCardButton;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;

import java.util.*;
import java.util.stream.Collectors;

import static it.polimi.ingsw.gc19.View.GUI.GUISettings.*;

/**
 * A sub-scene controller. Manges how GUI show
 * the station of a player: card picking, card placing and errors.
 */
public class LocalStationController extends GUIController implements StationListener, SetupListener {

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

    /**
     * Nickname of the player who owns the {@link LocalStationPlayer}
     */
    private final String nickOwner;

    /**
     * To translate {@link #renderedCards} inside {@link #centerPane}
     */
    private Translate translate;

    /**
     * To scale up or down {@link #renderedCards} inside {@link #centerPane}
     */
    private Scale centerPaneScale;

    /**
     * Colors pawns
     */
    private final ImageView bluePawnImageView, redPawnImageView, greenPawnImageView, yellowPawnImageView, blackPawnImageView;

    /**
     * Errors rectangle. It is displayed when {@link #nickOwner}
     * is the in turn player, and it tries to place a card that could
     * not be placed because {@link ClientController#placeCard(String, String, Direction, CardOrientation)}
     * returns <code>false</code>
     */
    private Rectangle error;

    /**
     * Starting coords of mouse inside scene.
     * Used to implement drag and drop of cards in hand
     */
    private double startX, startY;

    /**
     * Starting coords of mouse inside {@link #centerPane}. Used
     * to implements {@link #cardGrid}'s mouse dragging
     */
    private double anchorX, anchorY;

    /**
     * Final coords of mouse inside {@link #centerPane}. Used
     * to implements {@link #cardGrid}'s mouse dragging
     */
    private double translateAnchorX, translateAnchorY;

    /**
     * Number of rows and columns of {@link #cardGrid}
     */
    private int absGridRow, absGridCol;

    /**
     * Current scale of {@link #renderedCards} inside
     * {@link #centerPane}
     */
    private double scale = 1.0;

    /**
     * Scale factor of {@link #renderedCards} inside
     * {@link #centerPane}
     */
    private final double delta = 1.1;

    /**
     * List of cards {@link ImageView} contained in {@link #cardGrid}
     */
    private final ArrayList<ImageView> renderedCards;

    /**
     * Card that is currently dragged by mouse
     */
    private PlayableCardButton draggedCard = null;

    public LocalStationController(GUIController controller, String nickOwner) {
        super(controller);

        bluePawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/blue_pawn.png"))));
        redPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/red_pawn.png"))));
        greenPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/green_pawn.png"))));
        yellowPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/yellow_pawn.png"))));
        blackPawnImageView = new ImageView(new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/pawns/black_pawn.png"))));

        this.nickOwner = nickOwner;

        this.renderedCards = new ArrayList<>();

        super.getClientController().getListenersManager().attachListener(ListenerType.SETUP_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
    }

    /**
     * Initializes the sub-scene for local station
     * of the owner player.
     */
    @FXML
    private void initialize(){
        this.translate = new Translate();
        this.centerPaneScale = new Scale(scale, scale);

        this.centerPane.getTransforms().clear();

        this.rightVBox.getChildren().clear();

        initializePawns();
        initializeGameArea();
        initializeCards();

        this.leftVBox.spacingProperty().bind(this.borderPane.heightProperty().divide(10));
        this.rightVBox.spacingProperty().bind(this.borderPane.heightProperty().divide(10));

        Image backgroundImage = new Image(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/background_light.png")));
        BackgroundSize backgroundSize = new BackgroundSize(360, 360, false, false, false, false);
        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);

        borderPane.setBackground(new Background(background));

        this.leftVBox.prefWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO).add(30));
        this.leftVBox.maxWidthProperty().bind(this.leftVBox.prefWidthProperty());
        this.leftVBox.minWidthProperty().bind(this.leftVBox.prefWidthProperty());

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

        this.leftVBox.maxHeightProperty().bind(super.getStage().widthProperty().divide(HEIGHT_RATIO).add(this.leftVBox.spacingProperty()).multiply(3));
    }

    /**
     * Initializes pawn image for the owner player.
     * If player hasn't still chosen its color,
     * then nothing is showed. If player is first player,
     * it shows, also, black pawn.
     */
    private void initializePawns(){
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

    /**
     * For the owner player (<code>{@link LocalStationController#nickOwner}</code> equals
     * {@link ClientController#getNickname()}) displays the chosen {@link GoalCard},
     * otherwise nothing.
     * Then displays cards in hand: for owner player, it makes them draggable and swappable;
     * for others displays only back side.
     */
    private void initializeCards(){
        if(this.nickOwner.equals(this.getLocalModel().getNickname())){
            if(super.getLocalModel().getPersonalStation().getPrivateGoalCardInStation() != null) {
                this.rightVBox.getChildren().add(
                        new GoalCardButton(this.getLocalModel().getPersonalStation().getPrivateGoalCardInStation(), super.getStage(),
                                           (double) 1 / GUISettings.WIDTH_RATIO, (double) 1 / GUISettings.HEIGHT_RATIO));
            }

            HashMap<String, CardOrientation> prevCardsOrientation = new HashMap<>();

            for(Node n : List.copyOf(this.leftVBox.getChildren())){
                PlayableCardButton p = (PlayableCardButton) n;
                prevCardsOrientation.put(p.getCard().getCardCode(), p.getCardOrientation());
            }

            this.leftVBox.getChildren().clear();

            for(PlayableCard p : List.copyOf(this.getLocalModel().getPersonalStation().getCardsInHand())){
                PlayableCardButton button = new PlayableCardButton(p, super.getStage(), (double) 1 / GUISettings.WIDTH_RATIO, (double) 1 / GUISettings.HEIGHT_RATIO);

                if(prevCardsOrientation.containsKey(p.getCardCode()) && prevCardsOrientation.get(p.getCardCode()) == CardOrientation.DOWN){
                    button.swap();
                }

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

    /**
     * Checks whether {@link LocalStationController#centerPane} contains mouse.
     * <br>
     * If mouse is inside, and it is dragging a card (<code>draggedCard != null</code>)
     * then {@link LocalStationController#draggedCard} is resized to the dimension of the cards
     * inside {@link LocalStationController#centerPane}.
     * <br>
     * If mouse is outside {@link LocalStationController#centerPane} and <code>draggedCard != null</code>
     * then {@link LocalStationController#draggedCard} is resized to the dimensions
     * of cards in hand.
     * @param mouse a {@link MouseEvent} containing coords of mouse
     */
    private void checkStackPaneContainsImage(MouseEvent mouse){
        Point2D mousePosition = new Point2D(mouse.getSceneX(), mouse.getSceneY());

        Point2D upperLeftCornerStack = new Point2D(this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMinX(),
                                                   this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMinY());
        Point2D lowerRightCornerStack = new Point2D(this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMaxX(),
                                                    this.centerPane.localToScene(this.centerPane.getBoundsInLocal()).getMaxY());

        if((mousePosition.getX() < lowerRightCornerStack.getX() && mousePosition.getX() > upperLeftCornerStack.getX()) &&
                (mousePosition.getY() < lowerRightCornerStack.getY() && mousePosition.getY() > upperLeftCornerStack.getY())){

            this.draggedCard.getSide().fitWidthProperty().unbind();
            this.draggedCard.getSide().fitWidthProperty().bind(super.getStage().widthProperty()
                                                                    .divide(GUISettings.WIDTH_RATIO)
                                                                    .multiply(scale));
        }
        else{
            this.draggedCard.getSide().fitWidthProperty().unbind();
            this.draggedCard.getSide().fitWidthProperty().bind(super.getStage().widthProperty()
                                                                               .divide(GUISettings.WIDTH_RATIO));
        }
    }

    /**
     * Makes the specified {@link PlayableCardButton} draggable form {@link #leftVBox}
     * to {@link #centerPane}
     * @param node the {@link Node} that must be made draggable
     */
    private void makeCardDraggable(Node node){
        node.setOnMousePressed(e -> {
            if(super.getClientController().getState() == ViewState.SETUP || super.getClientController().getState() == ViewState.END) return;

            //always center card with respect to mouse
            node.setTranslateX(node.getTranslateX() + e.getSceneX() - node.localToScene(0,0).getX() - node.getBoundsInLocal().getWidth()/2);
            node.setTranslateY(node.getTranslateY() + e.getSceneY() - node.localToScene(0,0).getY() - node.getBoundsInLocal().getHeight()/2);
            startX = e.getSceneX() - node.getTranslateX();
            startY = e.getSceneY() - node.getTranslateY();

            if(this.error != null){
                this.error.setVisible(false);
            }
        });

        node.setOnMouseDragged(e -> {
            if(super.getClientController().getState() == ViewState.SETUP || super.getClientController().getState() == ViewState.END) return;

            node.setTranslateX(e.getSceneX() - startX);
            node.setTranslateY(e.getSceneY() - startY);

            this.draggedCard = (PlayableCardButton) node;

            this.checkStackPaneContainsImage(e);
        });

        node.setOnMouseReleased(e -> {
            if(super.getClientController().getState() == ViewState.SETUP || super.getClientController().getState() == ViewState.END) return;

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

                //check if a card is already present at given position
                PlayableCard card = super.getLocalModel().getPersonalStation().getPlacedCardAtPosition(localModelX, localModelY);
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
                        if(super.getClientController().placeCard(toPlace.getCardCode(), anchor.getCardCode(), direction, toPlace.getCardOrientation())) return;
                    }

                }
            }


            if(this.draggedCard != null) {
                this.draggedCard.getSide().fitWidthProperty().unbind();
                this.draggedCard.getSide().fitWidthProperty().bind(super.getStage().widthProperty()
                        .divide(GUISettings.WIDTH_RATIO));
            }

            node.setTranslateX(0);
            node.setTranslateY(0);
        });
    }

    /**
     * Factory method for unswappable cards. They are represented
     * as {@link ImageView} with both height and width properties bounded
     * to stage current width and height
     * @param symbol the {@link Symbol} of the card
     * @param type the {@link PlayableCardType} of the card
     * @return an {@link ImageView} describing the unswappable card
     */
    private ImageView factoryUnswappableCard(Symbol symbol, PlayableCardType type){
        ImageView imageView = new ImageView(CardImageLoader.getBackImage(symbol,type));
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO));
        imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(GUISettings.HEIGHT_RATIO));
        clipCardImage(imageView);

        return imageView;
    }

    /**
     * Initializes game area (e.g. {@link LocalStationController#centerPane})
     * containing current game (placed cards) of the owner player.
     * It builds also buttons for rescaling and centering  images inside
     * {@link LocalStationController#centerPane}.
     */
    private void initializeGameArea(){
        if(!this.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence().isEmpty()){
            this.buildCardGrid(super.getLocalModel().getStations().get(this.nickOwner).getPlacedCardSequence());

            StackPane.setAlignment(this.center, Pos.TOP_RIGHT);
            StackPane.setAlignment(this.rescale, Pos.TOP_RIGHT);

            this.center.setVisible(true);
            this.rescale.setVisible(true);

            this.center.toFront();
            this.rescale.toFront();

            this.rescale.setOnMouseClicked(event -> {
                this.scale = 1.0;

                this.centerPaneScale.setX(1.0);
                this.centerPaneScale.setY(1.0);
            });

            this.center.setOnMouseClicked(event -> {
                this.translate.setX(0.0);
                this.translate.setY(0.0);

                Point2D initialCardCenter = new Point2D(this.renderedCards.getFirst().getBoundsInParent().getCenterX(),
                                                        this.renderedCards.getFirst().getBoundsInParent().getCenterY());

                Point2D gridPaneUpperLeft = new Point2D(this.cardGrid.getBoundsInParent().getMinX(),
                                                        this.cardGrid.getBoundsInParent().getMinY());

                double x = this.centerPane.getBoundsInLocal().getCenterX() - (scale * initialCardCenter.getX() + gridPaneUpperLeft.getX());
                double y = this.centerPane.getBoundsInLocal().getCenterY() - (scale * initialCardCenter.getY() + gridPaneUpperLeft.getY());

                this.translate.setX(x);
                this.translate.setY(y);
            });
        }
        else{
            this.center.setVisible(false);
            this.rescale.setVisible(false);
        }
    }

    /**
     * Factory method for pawns
     * @param name the name of the {@link Color} to get the pawn
     * @return the {@link ImageView} of the corresponding pawn.
     */
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

    /**
     * Builds {@link #cardGrid} and fills it with {@link ImageView} of all {@link #renderedCards}
     * @param placedCardSequence the order sequence of {@link PlayableCard} to be displayed
     */
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

        for (int i = 0; i < renderedCards.size(); i++) {
            cardGrid.add(renderedCards.get(i), placedCardSequence.get(i).y().y() - firstCol + 1, placedCardSequence.get(i).y().x() - firstRow + 1);
        }

        makeGridPaneDraggable();

        makeCenterPaneScrollable();
    }

    /**
     * Clips the {@param cardImage} by a {@link Rectangle} with
     * rounded corners.
     * @param cardImage the {@link ImageView} to be clipped.
     */
    private void clipCardImage(ImageView cardImage){
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(cardImage.fitWidthProperty());
        rectangle.heightProperty().bind(cardImage.fitWidthProperty().multiply(CARD_PIXEL_HEIGHT / CARD_PIXEL_WIDTH));

        rectangle.arcWidthProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS/ CARD_PIXEL_WIDTH));
        rectangle.arcHeightProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));

        cardImage.setClip(rectangle);
    }

    /**
     * Makes {@link LocalStationController#cardGrid} draggable inside
     * {@link LocalStationController#centerPane}. Users with mouse can move
     * {@link LocalStationController#cardGrid} around {@link LocalStationController#centerPane}
     */
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

    /**
     * Makes {@link LocalStationController#cardGrid} and {@link LocalStationController#centerPane}
     * scrollable. Users with mouse's scroll can zoom in and
     * zoom out in {@link LocalStationController#cardGrid}.
     * At max, {@link LocalStationController#scale} can be <code>2</code>; at min, can be <code>0.25</code>
     */
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

    /**
     * Used to notify {@link LocalStationController} about {@link SetupEvent}.
     * Rebuilds the station view.
     * @param type a {@link SetupEvent} describing the type of the event
     */
    @Override
    public void notify(SetupEvent type) {
        Platform.runLater(() -> {
            if(type == SetupEvent.ACCEPTED_COLOR || type == SetupEvent.ACCEPTED_INITIAL_CARD || type == SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD){
                this.initialize();
            }
        });
    }

    /**
     * Used to notify {@link LocalStationController} about errors concerning {@link SetupEvent}.
     * Currently not used.
     * @param type the type of the error
     * @param error a {@link String} description of the error
     */
    @Override
    public void notify(SetupEvent type, String error) { }

    /**
     * Used to notify {@link LocalStationController} about {@link PersonalStation} updates.
     * Rebuilds the station view.
     * @param localStationPlayer is the {@link PersonalStation} that has changed
     */
    @Override
    public void notify(PersonalStation localStationPlayer) {
        Platform.runLater(() -> {
            if (localStationPlayer.getOwnerPlayer().equals(this.nickOwner)) initialize();
        });
    }

    /**
     * Used to notify {@link LocalStationController} about {@link OtherStation} updates.
     * Rebuilds the station view.
     * @param otherStation is the {@link OtherStation} that has changed
     */
    @Override
    public void notify(OtherStation otherStation) {
        Platform.runLater(() -> {
            if (otherStation.getOwnerPlayer().equals(this.nickOwner)) initialize();
        });
    }

    /**
     * Used to notify {@link LocalStationController} about errors concerning {@link LocalStationPlayer}
     * Does not rebuild the station view. Displays a drop shadowed rectangle in the position where user has placed
     * the invalid card.
     * @param varArgs variable arguments describing the error
     */
    @Override
    public void notifyErrorStation(String... varArgs){
        if(varArgs.length == 4 && varArgs[0].equals(nickOwner)) {
            Direction direction;

            try {
                direction = Direction.valueOf(varArgs[3].toUpperCase());
            } catch (IllegalArgumentException illegalArgumentException) {
                return;
            }

            Tuple<Integer, Integer> coords = super.getLocalModel().getPersonalStation().getCoords(varArgs[2]);
            int rowIndex = coords.x() + direction.getX() - (super.getLocalModel().getPersonalStation().getPlacedCardSequence().stream().mapToInt(t -> t.y().x()).min().orElse(0) - 1);
            int columnIndex = coords.y() + direction.getY() - (super.getLocalModel().getPersonalStation().getPlacedCardSequence().stream().mapToInt(t -> t.y().y()).min().orElse(0) - 1);

            Platform.runLater(() -> {
                error = new Rectangle();

                error.widthProperty().bind(super.getStage().widthProperty().divide(WIDTH_RATIO).multiply(scale));
                error.heightProperty().bind(error.widthProperty().multiply(CARD_PIXEL_HEIGHT).divide(CARD_PIXEL_WIDTH));

                error.arcWidthProperty().bind(error.widthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));
                error.arcHeightProperty().bind(error.heightProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));

                error.setFill(Color.TRANSPARENT);
                error.setStroke(Color.RED);
                error.setStrokeWidth(2);

                DropShadow dropShadow = new DropShadow();
                dropShadow.setOffsetX(3);
                dropShadow.setOffsetY(3);
                dropShadow.setColor(Color.RED);

                error.setEffect(dropShadow);

                this.cardGrid.add(error, columnIndex, rowIndex);
            });
        }
    }

}