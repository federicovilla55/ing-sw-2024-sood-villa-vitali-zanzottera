package it.polimi.ingsw.gc19.View.TUI;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
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
                tuiView.printTUIView(cardTUIView);
                card.swapCard();
                System.out.println();
                cardTUIView = tuiView.cardTUIView(card);
                tuiView.printTUIView(cardTUIView);
                System.out.println();
            }
        }
        for(Integer i = 1; i<=6; i++) {
            String cardString = "initial_"+new DecimalFormat("00").format(i);
            System.out.println(cardString + ":");
            card = stringPlayableCardHashMap.get(cardString);
            cardTUIView = tuiView.cardTUIView(card);
            tuiView.printTUIView(cardTUIView);
            card.swapCard();
            System.out.println();
            cardTUIView = tuiView.cardTUIView(card);
            tuiView.printTUIView(cardTUIView);
            System.out.println();
        }
    }

    @Test
    public void testPlayerAreaTUIView() {
        String[][] playerAreaTUIView;

        playerAreaTUIView = tuiView.playerAreaTUIView(
                List.of(
                        new Tuple<>(stringPlayableCardHashMap.get("initial_05").setCardState(CardOrientation.DOWN), new Tuple<>(25, 25)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_23").setCardState(CardOrientation.DOWN), new Tuple<>(24, 26)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_01").setCardState(CardOrientation.UP), new Tuple<>(24, 24)),
                        new Tuple<>(stringPlayableCardHashMap.get("gold_39").setCardState(CardOrientation.DOWN), new Tuple<>(23, 23)),
                        new Tuple<>(stringPlayableCardHashMap.get("gold_23").setCardState(CardOrientation.UP), new Tuple<>(23, 27)),
                        new Tuple<>(stringPlayableCardHashMap.get("gold_40").setCardState(CardOrientation.DOWN), new Tuple<>(22, 26)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_05").setCardState(CardOrientation.DOWN), new Tuple<>(22, 24)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_03").setCardState(CardOrientation.DOWN), new Tuple<>(21, 25)),
                        new Tuple<>(stringPlayableCardHashMap.get("gold_06").setCardState(CardOrientation.UP), new Tuple<>(23, 25)),
                        new Tuple<>(stringPlayableCardHashMap.get("gold_20").setCardState(CardOrientation.DOWN), new Tuple<>(24, 28)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_08").setCardState(CardOrientation.DOWN), new Tuple<>(25, 29)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_21").setCardState(CardOrientation.DOWN), new Tuple<>(23, 29)),
                        new Tuple<>(stringPlayableCardHashMap.get("gold_28").setCardState(CardOrientation.UP), new Tuple<>(26, 30)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_30").setCardState(CardOrientation.UP), new Tuple<>(25, 31)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_39").setCardState(CardOrientation.UP), new Tuple<>(22, 30)),
                        new Tuple<>(stringPlayableCardHashMap.get("gold_24").setCardState(CardOrientation.UP), new Tuple<>(24, 30)),
                        new Tuple<>(stringPlayableCardHashMap.get("resource_28").setCardState(CardOrientation.UP), new Tuple<>(21, 31))
                )
        );

        tuiView.printTUIView(playerAreaTUIView);
    }

    @Test
    public void testTableTUIView() {
        String[][] tableTUIView;

        tableTUIView = tuiView.tableTUIView();

        tuiView.printTUIView(tableTUIView);
    }

}
