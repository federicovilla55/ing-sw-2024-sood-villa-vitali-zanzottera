package it.polimi.ingsw.gc19.View.TUI;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.View.TUIView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class TUICardViewTest {

    private final static HashMap<String, PlayableCard> stringPlayableCardHashMap = new HashMap<>();
    private final static HashMap<String, GoalCard> stringGoalCardHashMap = new HashMap<>();
    private final static TUIView tuiView = new TUIView();


    @BeforeAll
    public static void setUp() throws IOException {
        JSONParser.readPlayableCardFromFile().forEach(c -> stringPlayableCardHashMap.put(c.getCardCode(), c));
        JSONParser.readGoalCardFromFile().forEach(c -> stringGoalCardHashMap.put(c.getCardCode(), c));
    }

    @Test
    public void testPlayableCardTUIView() {
        String[][] cardTUIView;
        PlayableCard card;
        for(String type : List.of("resource_", "gold_")) {
            for(Integer i = 1; i<=40; i++) {
                String cardString = type+new DecimalFormat("00").format(i);
                System.out.println(cardString + ":");
                card = stringPlayableCardHashMap.get(cardString);
                cardTUIView = tuiView.cardTUIView(card);
                tuiView.printTUICardView(cardTUIView);
                card.swapCard();
                System.out.println();
                cardTUIView = tuiView.cardTUIView(card);
                tuiView.printTUICardView(cardTUIView);
                System.out.println();
            }
        }
        for(Integer i = 1; i<=6; i++) {
            String cardString = "initial_"+new DecimalFormat("00").format(i);
            System.out.println(cardString + ":");
            card = stringPlayableCardHashMap.get(cardString);
            cardTUIView = tuiView.cardTUIView(card);
            tuiView.printTUICardView(cardTUIView);
            card.swapCard();
            System.out.println();
            cardTUIView = tuiView.cardTUIView(card);
            tuiView.printTUICardView(cardTUIView);
            System.out.println();
        }
    }

}
