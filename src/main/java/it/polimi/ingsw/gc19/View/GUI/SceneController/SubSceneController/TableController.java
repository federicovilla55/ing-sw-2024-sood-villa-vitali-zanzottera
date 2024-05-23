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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
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
    }

    @FXML
    public void resize(){

        /*((Region) this.tableBorderPane.getParent()).widthProperty().addListener((observable, oldValue, newValue) -> {
            this.gridPane.getColumnConstraints().get(1).setPrefWidth(400 + 2 * (newValue.doubleValue() - oldValue.doubleValue()));
            this.gridPane.getColumnConstraints().get(3).setPrefWidth(400 + 2 * (newValue.doubleValue() - oldValue.doubleValue()));

            System.out.println(this.gridPane.getColumnConstraints().get(3).getPrefWidth());
        });*/
    }

    private void buildTable(){
        if(this.getLocalModel().getTable() != null){
            drawableCardFactory(this.getLocalModel().getTable().getResource1(), 0,0);

            drawableCardFactory(this.getLocalModel().getTable().getResource2(), 0,1);

            drawableCardFactory(this.getLocalModel().getTable().getGold1(), 1, 0);

            drawableCardFactory(this.getLocalModel().getTable().getGold2(), 1, 1);

            this.publicGoals[0] = new CardButton(this.getLocalModel().getTable().getPublicGoal1());
            this.publicGoals[0].setOnMouseClicked(this.publicGoals[0].getDefaultMouseClickedHandler());

            this.publicGoals[1] = new CardButton(this.getLocalModel().getTable().getPublicGoal2());
            this.publicGoals[1].setOnMouseClicked(this.publicGoals[1].getDefaultMouseClickedHandler());

            this.decks[0] = factoryUpperDeckCard(this.getLocalModel().getTable().getNextSeedOfResourceDeck(), PlayableCardType.RESOURCE);
            this.decks[1] = factoryUpperDeckCard(this.getLocalModel().getTable().getNextSeedOfGoldDeck(), PlayableCardType.GOLD);
            this.decks[2] = factoryUpperDeckCard();
        }

        for(int i = 0; i < 2; i++){
            for(int k = 0; k < 2; k++){
                this.gridPane.add(this.drawableTableCards[i][k], 2 * k, 2 * i);
            }
        }

        for(int i = 0; i < 2; i++){
            this.gridPane.add(this.publicGoals[i], 2 * i, 4);
        }

        for(int i = 0; i < 3; i++){
            this.gridPane.add(this.decks[i], 4, 2 * i);
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
            this.drawableTableCards[x][y] = new CardButton(card);
            this.drawableTableCards[x][y].setOnMouseClicked((event) -> handleClickDrawableTableCard(event, card, y));
        }
        else{
            this.drawableTableCards[x][y] = null;
        }
    }

    private ImageView factoryUpperDeckCard(){
        ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/back/goal.jpg")).toExternalForm()));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);
        return imageView;
    }

    private ImageView factoryUpperDeckCard(Symbol symbol, PlayableCardType type){
        if(symbol != null) {
            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/images/back/" + type.toString().toLowerCase() + "_" + symbol.toString().toLowerCase() + ".jpg")).toExternalForm()));
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);

            return imageView;
        }

        return null;
    }

    @Override
    public void notify(LocalTable localTable) {
        buildTable();
    }

}