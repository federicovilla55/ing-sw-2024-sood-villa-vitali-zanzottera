package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.Utils.CardButton;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TableListener;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

public class TableController extends AbstractController implements TableListener {

    @FXML
    private GridPane gridPane;

    public void initialize(){
        /*CardButton card1 = new CardButton("gold_01");
        card1.getFront().setPreserveRatio(true);
        card1.getFront().setFitWidth(300);
        CardButton card2 = new CardButton("gold_01");
        card2.getFront().setPreserveRatio(true);
        card2.getFront().setFitWidth(300);
        CardButton card3 = new CardButton("gold_01");
        card3.getFront().setPreserveRatio(true);
        card3.getFront().setFitWidth(300);
        CardButton card4 = new CardButton("gold_01");
        card4.getFront().setPreserveRatio(true);
        card4.getFront().setFitWidth(300);
        CardButton card5 = new CardButton("goal_01");
        card5.getFront().setPreserveRatio(true);
        card5.getFront().setFitWidth(300);
        CardButton card6 = new CardButton("goal_01");
        card6.getFront().setPreserveRatio(true);
        card6.getFront().setFitWidth(300);
        CardButton card7 = new CardButton("goal_01");
        card7.getFront().setPreserveRatio(true);
        card7.getFront().setFitWidth(300);
        CardButton card8 = new CardButton("goal_01");
        card8.getFront().setPreserveRatio(true);
        card8.getFront().setFitWidth(300);
        CardButton card9 = new CardButton("goal_01");
        card9.getFront().setPreserveRatio(true);
        card9.getFront().setFitWidth(300);

        gridPane.add(card1.getFront(), 0, 0);
        gridPane.add(card2.getFront(), 2, 0);
        gridPane.add(card3.getFront(), 4, 0);
        gridPane.add(card4.getFront(), 0, 2);
        gridPane.add(card5.getFront(), 2, 2);
        gridPane.add(card6.getFront(), 4, 2);
        gridPane.add(card7.getFront(), 0, 4);
        gridPane.add(card8.getFront(), 2, 4);
        gridPane.add(card9.getFront(), 4, 4);*/
    }

    @Override
    public void notify(LocalTable localTable) {

    }
}
