package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.GUISettings;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardImageLoader;
import it.polimi.ingsw.gc19.View.GUI.Utils.GoalCardButton;
import it.polimi.ingsw.gc19.View.GUI.Utils.PlayableCardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TableListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.List;

public class TableController extends AbstractController implements TableListener {

    @FXML
    private GridPane gridPane;

    @FXML
    private BorderPane tableBorderPane;

    private static final double ASPECT_RATIO = 832.0 / 558.0;

    private final PlayableCardButton[][] drawableTableCards;

    private final GoalCardButton[] publicGoals;

    private final ImageView[] decks;

    public TableController(AbstractController controller){
        super(controller);

        this.drawableTableCards = new PlayableCardButton[2][2];
        this.publicGoals = new GoalCardButton[2];
        this.decks = new ImageView[3];

        getClientController().getListenersManager().attachListener(ListenerType.TABLE_LISTENER, this);
    }

    @FXML
    public void initialize(){
        buildTable();

        this.gridPane.hgapProperty().bind(this.gridPane.widthProperty().multiply(2).divide(7));
        this.gridPane.vgapProperty().bind(this.gridPane.heightProperty().divide(15));
    }

    private void buildTable(){
        gridPane.getChildren().clear();

        for(int i = 0; i <= 2; i++) {
            for(int j = 0; j <= 2; j++) {
                Pane pane = new Pane();
                pane.prefWidthProperty().bind(Bindings.min(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO), super.getStage().heightProperty().divide(GUISettings.HEIGHT_RATIO).multiply(ASPECT_RATIO)));
                pane.prefHeightProperty().bind(Bindings.min(super.getStage().heightProperty().divide(GUISettings.HEIGHT_RATIO), super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO).divide(ASPECT_RATIO)));
                pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                pane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                gridPane.add(pane, i, j);
            }
        }

        if(this.getLocalModel().getTable() != null){
            drawableCardFactory(this.getLocalModel().getTable().getResource1(), 0,0);

            drawableCardFactory(this.getLocalModel().getTable().getResource2(), 0,1);

            drawableCardFactory(this.getLocalModel().getTable().getGold1(), 1, 0);

            drawableCardFactory(this.getLocalModel().getTable().getGold2(), 1, 1);

            this.publicGoals[0] = new GoalCardButton(this.getLocalModel().getTable().getPublicGoal1(), super.getStage(), (double) 1 / GUISettings.WIDTH_RATIO, (double) 1 / GUISettings.HEIGHT_RATIO); //(Region) this.gridPane.getParent()
            this.publicGoals[0].setOnMouseClicked(this.publicGoals[0].getDefaultMouseClickedHandler());

            this.publicGoals[1] = new GoalCardButton(this.getLocalModel().getTable().getPublicGoal2(), super.getStage(), (double) 1 / GUISettings.WIDTH_RATIO, (double) 1 / GUISettings.HEIGHT_RATIO);
            this.publicGoals[1].setOnMouseClicked(this.publicGoals[1].getDefaultMouseClickedHandler());

            if(this.getLocalModel().getTable().getNextSeedOfResourceDeck() != null) {
                this.decks[0] = factoryUpperDeckCard(this.getLocalModel().getTable().getNextSeedOfResourceDeck(), PlayableCardType.RESOURCE);
            }
            else {
                this.decks[0] = null;
            }
            if(this.getLocalModel().getTable().getNextSeedOfGoldDeck() != null) {
                this.decks[1] = factoryUpperDeckCard(this.getLocalModel().getTable().getNextSeedOfGoldDeck(), PlayableCardType.GOLD);
            }
            else {
                this.decks[1] = null;
            }
            this.decks[2] = factoryUpperDeckCard();
        }

        for(int i = 0; i < 2; i++){
            for(int k = 0; k < 2; k++){
                if(this.drawableTableCards[i][k] != null) {
                    this.gridPane.add(this.drawableTableCards[i][k], k, i);
                }
                else {
                    List<Node> gridNodes = List.copyOf(gridPane.getChildren());
                    for(Node n : gridNodes) {
                        if(n instanceof PlayableCardButton && GridPane.getRowIndex(n) == i && GridPane.getColumnIndex(n) == k) {
                            gridPane.getChildren().remove(n);
                        }
                    }
                }
            }
        }

        for(int i = 0; i < 2; i++){
            if(this.publicGoals != null) {
                this.gridPane.add(this.publicGoals[i], i, 2);
            }
            else {
                List<Node> gridNodes = List.copyOf(gridPane.getChildren());
                for(Node n : gridNodes) {
                    if(n instanceof GoalCardButton && GridPane.getRowIndex(n) == 2 && GridPane.getColumnIndex(n) == i) {
                        gridPane.getChildren().remove(n);
                    }
                }
            }
        }

        for(int i = 0; i < 2; i++){
            if (this.decks[i] != null) {
                this.gridPane.add(this.decks[i], 2, i);
            }
            else {
                List<Node> gridNodes = List.copyOf(gridPane.getChildren());
                for(Node n : gridNodes) {
                    if(n instanceof ImageView && GridPane.getRowIndex(n) == i && GridPane.getColumnIndex(n) == 2) {
                        gridPane.getChildren().remove(n);
                    }
                }
            }
        }

        if(!this.gridPane.getChildren().contains(factoryUpperDeckCard())){
            this.gridPane.add(this.decks[2], 2, 2);
        }
    }

    private void handleClickDrawableTableCard(MouseEvent event, PlayableCard card, int position){
        if(event.getClickCount() == 1){
            if(this.getClientController().getState() == ViewState.PICK) {
                getClientController().pickCardFromTable(card.getCardType(), position);
            }
        }
    }

    private void handleClickDrawableDeck(MouseEvent event, PlayableCardType type){
        if(event.getClickCount() == 1){
            if(this.getClientController().getState() == ViewState.PICK) {
                getClientController().pickCardFromDeck(type);
            }
        }
    }

    private void drawableCardFactory(PlayableCard card, int x, int y){
        if(card != null){
            this.drawableTableCards[x][y] = new PlayableCardButton(card, super.getStage(), (double) 1 / GUISettings.WIDTH_RATIO, (double) 1 / GUISettings.HEIGHT_RATIO);
            this.drawableTableCards[x][y].setOnMouseClicked((event) -> handleClickDrawableTableCard(event, card, y));
        }
        else{
            this.drawableTableCards[x][y] = null;
        }
    }

    private ImageView factoryUpperDeckCard(){
        ImageView imageView = CardImageLoader.getBackImageView();
        imageView.setPreserveRatio(true);
        //imageView.setFitWidth(200);
        imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO));
        imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(GUISettings.HEIGHT_RATIO));

        clipCardImage(imageView);

        return imageView;
    }

    private ImageView factoryUpperDeckCard(Symbol symbol, PlayableCardType type){
        if(symbol != null) {
            ImageView imageView = new ImageView(CardImageLoader.getBackImage(symbol,type));
            imageView.setPreserveRatio(true);
            //imageView.setFitWidth(200);
            imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(GUISettings.WIDTH_RATIO));
            imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(GUISettings.HEIGHT_RATIO));

            clipCardImage(imageView);

            imageView.setOnMouseClicked((event) -> handleClickDrawableDeck(event, type));

            return imageView;
        }

        return null;
    }

    @Override
    public void notify(LocalTable localTable) {
        Platform.runLater(this::buildTable);
    }

    private void clipCardImage(ImageView cardImage){
        double CARD_PIXEL_HEIGHT = 558.0;
        double CARD_PIXEL_WIDTH = 832.0;

        javafx.scene.shape.Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(cardImage.fitWidthProperty());
        rectangle.heightProperty().bind(cardImage.fitWidthProperty().multiply(CARD_PIXEL_HEIGHT / CARD_PIXEL_WIDTH));

        double CORNER_RADIUS = 27.0;
        rectangle.arcWidthProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));
        rectangle.arcHeightProperty().bind(cardImage.fitWidthProperty().multiply(2 * CORNER_RADIUS / CARD_PIXEL_WIDTH));

        cardImage.setClip(rectangle);
    }

}