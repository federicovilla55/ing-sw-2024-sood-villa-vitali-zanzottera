package it.polimi.ingsw.gc19.View.TUI;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void testAvailableColorsTUIView() {
        String[][] availableColorsTUIView;

        availableColorsTUIView = tuiView.availableColorsTUIView(
                List.of(

                )
        );

        tuiView.printTUIView(availableColorsTUIView);

        availableColorsTUIView = tuiView.availableColorsTUIView(
                List.of(
                        Color.RED,
                        Color.GREEN,
                        Color.BLUE,
                        Color.YELLOW
                )
        );

        tuiView.printTUIView(availableColorsTUIView);


    }

    @Test
    public void testAvailableGamesTUIView() {
        String[][] availableGamesTUIView;

        availableGamesTUIView = tuiView.availableGamesTUIView(
                List.of(

                )
        );

        tuiView.printTUIView(availableGamesTUIView);

        availableGamesTUIView = tuiView.availableGamesTUIView(
                List.of(
                        "beautiful_game",
                        "awesome_game",
                        "marvellous_game",
                        "superb_game",
                        "incredible_game"
                )
        );

        tuiView.printTUIView(availableGamesTUIView);


    }

    @Test
    public void testPlayableCardTUIView() {
        String[][] cardTUIView;
        PlayableCard card;
        for (String type : List.of("resource_", "gold_")) {
            for (Integer i = 1; i <= 40; i++) {
                String cardString = type + new DecimalFormat("00").format(i);
                System.out.println("-----------------------");
                System.out.println(cardString + ":");
                card = stringPlayableCardHashMap.get(cardString);
                card.setCardState(CardOrientation.UP);
                cardTUIView = tuiView.cardTUIView(card);
                tuiView.printTUIView(cardTUIView);
                card.swapCard();
                System.out.println();
                cardTUIView = tuiView.cardTUIView(card);
                tuiView.printTUIView(cardTUIView);
                System.out.println();
                System.out.println(card.getCardDescription());
                System.out.println();
                tuiView.printTUIView(tuiView.playableCardEffectTUIView(card));
            }
        }
        for (Integer i = 1; i <= 6; i++) {
            String cardString = "initial_" + new DecimalFormat("00").format(i);
            System.out.println("-----------------------");
            System.out.println(cardString + ":");
            card = stringPlayableCardHashMap.get(cardString);
            card.setCardState(CardOrientation.UP);
            cardTUIView = tuiView.cardTUIView(card);
            tuiView.printTUIView(cardTUIView);
            card.swapCard();
            System.out.println();
            cardTUIView = tuiView.cardTUIView(card);
            tuiView.printTUIView(cardTUIView);
            System.out.println();
            System.out.println(card.getCardDescription());
            System.out.println();
            tuiView.printTUIView(tuiView.playableCardEffectTUIView(card));
        }
    }

    @Test
    public void testInitialCardTUIView() {
        String[][] initialCardTUIView = tuiView.initialCardTUIView(
                stringPlayableCardHashMap.get("initial_05")
        );

        tuiView.printTUIView(initialCardTUIView);
    }

    @Test
    public void testGoalCardTUIView() {
        GoalCard card;
        for (Integer i = 1; i <= 16; i++) {
            String cardString = "goal_" + new DecimalFormat("00").format(i);
            card = stringGoalCardHashMap.get(cardString);
            String[][] goalCardTUIView = tuiView.goalCardEffectTUIView(card);
            System.out.println("-----------------------");
            System.out.println(card.getCardDescription());
            System.out.println();
            tuiView.printTUIView(goalCardTUIView);
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

        tableTUIView = tuiView.tableTUIView(
                stringPlayableCardHashMap.get("resource_01"),
                stringPlayableCardHashMap.get("resource_11"),
                stringPlayableCardHashMap.get("gold_21"),
                stringPlayableCardHashMap.get("gold_31"),
                Symbol.INSECT,
                Symbol.ANIMAL
        );

        tuiView.printTUIView(tableTUIView);

        tableTUIView = tuiView.tableTUIView(
                stringPlayableCardHashMap.get("resource_01"),
                null,
                null,
                stringPlayableCardHashMap.get("gold_31"),
                Symbol.INSECT,
                null
        );

        tuiView.printTUIView(tableTUIView);


    }

    @Test
    public void testHandTUIView() {
        String[][] handTUIView;

        handTUIView = tuiView.handTUIView(
                List.of(

                )
        );

        tuiView.printTUIView(handTUIView);

        handTUIView = tuiView.handTUIView(
                List.of(
                        stringPlayableCardHashMap.get("gold_09"),
                        stringPlayableCardHashMap.get("gold_19"),
                        stringPlayableCardHashMap.get("gold_39")
                )
        );

        tuiView.printTUIView(handTUIView);


    }

    @Test
    public void testScoreboardTUIView() {
        String[][] scoreboardTUIView;

        scoreboardTUIView = tuiView.scoreboardTUIView(
                new PersonalStation(
                        "aldo",
                        null,
                        Map.of(
                                Symbol.INSECT, 0,
                                Symbol.ANIMAL, 1,
                                Symbol.VEGETABLE, 22,
                                Symbol.MUSHROOM, 3,
                                Symbol.INK, 14,
                                Symbol.SCROLL, 5,
                                Symbol.FEATHER, 6
                        ),

                        7,
                        List.of(),

                        null,
                        null,

                        null

                )

        );

        tuiView.printTUIView(scoreboardTUIView);

        scoreboardTUIView = tuiView.scoreboardTUIView(
                new PersonalStation(
                        "aldo",
                        Color.RED,
                        Map.of(
                                Symbol.INSECT, 0,
                                Symbol.ANIMAL, 1,
                                Symbol.VEGETABLE, 22,
                                Symbol.MUSHROOM, 3,
                                Symbol.INK, 14,
                                Symbol.SCROLL, 5,
                                Symbol.FEATHER, 6
                        ),

                        7,
                        List.of(),

                        null,
                        null,

                        null

                ),
                new PersonalStation(
                        "giovanni",
                        Color.GREEN,
                        Map.of(
                                Symbol.INSECT, 9,
                                Symbol.ANIMAL, 21,
                                Symbol.VEGETABLE, 53,
                                Symbol.MUSHROOM, 3,
                                Symbol.INK, 15,
                                Symbol.SCROLL, 12,
                                Symbol.FEATHER, 5
                        ),

                        28,
                        List.of(),

                        null,
                        null,

                        null

                ),
                new PersonalStation(
                        "giacomo",
                        Color.BLUE,
                        Map.of(
                                Symbol.INSECT, 0,
                                Symbol.ANIMAL, 0,
                                Symbol.VEGETABLE, 0,
                                Symbol.MUSHROOM, 1,
                                Symbol.INK, 0,
                                Symbol.SCROLL, 0,
                                Symbol.FEATHER, 1
                        ),

                        0,
                        List.of(),

                        null,
                        null,

                        null

                ),

                new PersonalStation(
                        "marco",
                        Color.YELLOW,
                        Map.of(
                                Symbol.INSECT, 99,
                                Symbol.ANIMAL, 99,
                                Symbol.VEGETABLE, 99,
                                Symbol.MUSHROOM, 99,
                                Symbol.INK, 99,
                                Symbol.SCROLL, 99,
                                Symbol.FEATHER, 99
                        ),

                        99,
                        List.of(),

                        null,
                        null,

                        null

                )

        );

        tuiView.printTUIView(scoreboardTUIView);
    }

}
