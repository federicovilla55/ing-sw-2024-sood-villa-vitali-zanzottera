package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TableListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class TableController extends AbstractController implements TableListener {

    @FXML
    private GridPane gridPane;

    @FXML
    private BorderPane tableBorderPane;

    private final CardButton[][] drawableTableCards;

    private final CardButton[] publicGoals;

    private final ImageView[] decks;

    public TableController(AbstractController controller){
        super(controller);

        this.drawableTableCards = new CardButton[2][2];
        this.publicGoals = new CardButton[2];
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
        if(this.getLocalModel().getTable() != null){
            drawableCardFactory(this.getLocalModel().getTable().getResource1(), 0,0);

            drawableCardFactory(this.getLocalModel().getTable().getResource2(), 0,1);

            drawableCardFactory(this.getLocalModel().getTable().getGold1(), 1, 0);

            drawableCardFactory(this.getLocalModel().getTable().getGold2(), 1, 1);

            this.publicGoals[0] = new CardButton(this.getLocalModel().getTable().getPublicGoal1(), super.getStage(), (double) 1 / 12.8, (double) 1 / 7.2); //(Region) this.gridPane.getParent()
            this.publicGoals[0].setOnMouseClicked(this.publicGoals[0].getDefaultMouseClickedHandler());

            this.publicGoals[1] = new CardButton(this.getLocalModel().getTable().getPublicGoal2(), super.getStage(), (double) 1 / 12.8, (double) 1 / 7.2);
            this.publicGoals[1].setOnMouseClicked(this.publicGoals[1].getDefaultMouseClickedHandler());

            this.decks[0] = factoryUpperDeckCard(this.getLocalModel().getTable().getNextSeedOfResourceDeck(), PlayableCardType.RESOURCE);
            this.decks[1] = factoryUpperDeckCard(this.getLocalModel().getTable().getNextSeedOfGoldDeck(), PlayableCardType.GOLD);
            this.decks[2] = factoryUpperDeckCard();
        }

        for(int i = 0; i < 2; i++){
            for(int k = 0; k < 2; k++){
                this.gridPane.add(this.drawableTableCards[i][k], k, i);
            }
        }

        for(int i = 0; i < 2; i++){
            this.gridPane.add(this.publicGoals[i], i, 2);
        }

        for(int i = 0; i < 3; i++){
            this.gridPane.add(this.decks[i], 2, i);
        }
    }

    private void handleClickDrawableTableCard(MouseEvent event, PlayableCard card, int position){
        CardButton source = ((CardButton) event.getSource());

        if(event.getClickCount() == 1){
            source.swap();
        }
        else{
            if(this.getClientController().getState() == ViewState.PICK) {
                getClientController().pickCardFromTable(card.getCardType(), position);

                this.gridPane.getChildren().removeIf(c -> ((CardButton) c).getCard().equals(card));
                Arrays.stream(this.drawableTableCards).flatMap(Arrays::stream).forEach(b -> b.setOnMouseClicked(b.getDefaultMouseClickedHandler()));
            }
        }
    }

    private void drawableCardFactory(PlayableCard card, int x, int y){
        if(card != null){
            this.drawableTableCards[x][y] = new CardButton(card, super.getStage(), (double) 1 / 12.8, (double) 1 / 7.2);
            this.drawableTableCards[x][y].setOnMouseClicked((event) -> handleClickDrawableTableCard(event, card, y));
        }
        else{
            this.drawableTableCards[x][y] = null;
        }
    }

    private ImageView factoryUpperDeckCard(){
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/back/goal.jpg")).toExternalForm()));
        imageView.setPreserveRatio(true);
        //imageView.setFitWidth(200);
        imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(12.8));
        imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(7.2));

        clipCardImage(imageView);

        return imageView;
    }

    private ImageView factoryUpperDeckCard(Symbol symbol, PlayableCardType type){
        if(symbol != null) {
            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/back/" + type.toString().toLowerCase() + "_" + symbol.toString().toLowerCase() + ".jpg")).toExternalForm()));
            imageView.setPreserveRatio(true);
            //imageView.setFitWidth(200);
            imageView.fitWidthProperty().bind(super.getStage().widthProperty().divide(12.8));
            imageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(7.2));

            clipCardImage(imageView);

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