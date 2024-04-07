package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OtherStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MessagesTest{

    private MainController mainController;
    private Map<String, PlayableCard> playableCards;
    private Map<String, GoalCard> goalCards;
    private ClientStub player1, player2, player3, player4;

    @BeforeEach
    void setUp(){
        MainController.destroyMainController();
        this.mainController = MainController.getMainServer();

        try {
            this.playableCards = JSONParser.readPlayableCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
            this.goalCards = JSONParser.readGoalCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
        }
        catch(IOException e){
            e.printStackTrace();
        }

        this.player1 = new ClientStub("player1");
        this.player2 = new ClientStub("player2");
        this.player3 = new ClientStub("player3");
        this.player4 = new ClientStub("player4");
    }

    @Test
    void testGameCreationAndConfiguration(){
        this.mainController.createClient(player1, this.player1.getName());
        assertEquals(new CreatedPlayerMessage("player1"), this.player1.getMessage());

        this.mainController.createClient(player2, this.player2.getName());
        assertEquals(new CreatedPlayerMessage("player2"), this.player2.getMessage());
        //No new messages has been sent to player1
        assertNull(player1.getMessage());

        this.mainController.createClient(player3, this.player3.getName());
        assertEquals(new CreatedPlayerMessage("player3"), this.player3.getMessage());

        this.mainController.createClient(player4, this.player4.getName());
        assertEquals(new CreatedPlayerMessage("player4"), this.player4.getMessage());

        this.mainController.createGame("game1", 4, this.player1, 1);

        assertEquals(new CreatedGameMessage("game1"), player1.getMessage());

        assertEquals(new JoinedGameMessage("game1"), player1.getMessage());

        assertMessageEquals(player1,
                new TableConfigurationMessage(
                        playableCards.get("resource_05").setCardState(CardOrientation.UP),
                        playableCards.get("resource_21").setCardState(CardOrientation.UP),
                        playableCards.get("gold_19").setCardState(CardOrientation.UP),
                        playableCards.get("gold_23").setCardState(CardOrientation.UP),
                        goalCards.get("goal_11"),
                        goalCards.get("goal_15"),
                        Symbol.VEGETABLE,
                        Symbol.ANIMAL
                ));

        assertMessageEquals(player1, new OwnStationConfigurationMessage(
                "player1",
                null,
                List.of(
                        playableCards.get("resource_23"),
                        playableCards.get("resource_01"),
                        playableCards.get("gold_28")
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                null,
                0,
                playableCards.get("initial_05"),
                goalCards.get("goal_09"),
                goalCards.get("goal_14"),
                List.of()
        ));

        assertMessageEquals(player1,
                new AvailableColorsMessage(List.of(Color.values())));

        assertMessageEquals(player1,
                new GameConfigurationMessage(
                        GameState.SETUP,
                        null,
                        null,
                        null,
                        false,
                        4
                ));

        //No new messages has been sent to player2
        assertNull(player2.getMessage());

        this.mainController.registerToGame(player2, "game1");

        assertMessageEquals(player1,
                new NewPlayerConnectedToGameMessage("player2"));

        assertMessageEquals(player1,
                new OtherStationConfigurationMessage(
                        "player2",
                        null,
                        Map.of(
                                Symbol.ANIMAL, 0,
                                Symbol.MUSHROOM, 0,
                                Symbol.VEGETABLE, 0,
                                Symbol.INSECT, 0,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        0,
                        List.of()
                ));

        MessageToClient tableConfigurationMessage = new TableConfigurationMessage(
                playableCards.get("resource_05").setCardState(CardOrientation.UP),
                playableCards.get("resource_21").setCardState(CardOrientation.UP),
                playableCards.get("gold_19").setCardState(CardOrientation.UP),
                playableCards.get("gold_23").setCardState(CardOrientation.UP),
                goalCards.get("goal_11"),
                goalCards.get("goal_15"),
                Symbol.ANIMAL,
                Symbol.VEGETABLE
        );

        assertMessageEquals(player1,
                tableConfigurationMessage);

        assertEquals(new JoinedGameMessage("game1"), player2.getMessage());


        assertMessageEquals(player2,
                tableConfigurationMessage);

        assertMessageEquals(player2,
                new OwnStationConfigurationMessage(
                        "player2",
                        null,
                        List.of(
                                playableCards.get("resource_15"),
                                playableCards.get("resource_37"),
                                playableCards.get("gold_21")
                        ),
                        Map.of(
                                Symbol.ANIMAL, 0,
                                Symbol.MUSHROOM, 0,
                                Symbol.VEGETABLE, 0,
                                Symbol.INSECT, 0,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        null,
                        0,
                        playableCards.get("initial_01"),
                        goalCards.get("goal_16"),
                        goalCards.get("goal_01"),
                        List.of()
                ));

        assertMessageEquals(player2,
                new OtherStationConfigurationMessage(
                "player1",
                null,
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                0,
                List.of()
        ));

        assertMessageEquals(player2,
                new AvailableColorsMessage(List.of(Color.values())));

        assertMessageEquals(player2,
                new GameConfigurationMessage(
                        GameState.SETUP,
                        null,
                        null,
                        null,
                        false,
                        4
                ));

        this.mainController.registerToGame(player3, "game1");
        tableConfigurationMessage = new TableConfigurationMessage(
            playableCards.get("resource_05").setCardState(CardOrientation.UP),
            playableCards.get("resource_21").setCardState(CardOrientation.UP),
            playableCards.get("gold_19").setCardState(CardOrientation.UP),
            playableCards.get("gold_23").setCardState(CardOrientation.UP),
            goalCards.get("goal_11"),
            goalCards.get("goal_15"),
            Symbol.VEGETABLE,
            Symbol.MUSHROOM
        );

        assertMessageEquals(List.of(player1,player2), new NewPlayerConnectedToGameMessage("player3"));
        assertMessageEquals(List.of(player1,player2),
                new OtherStationConfigurationMessage(
                "player3",
                null,
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                0,
                List.of()
        ));
        assertMessageEquals(List.of(player1,player2),tableConfigurationMessage);

        assertEquals(new JoinedGameMessage("game1"), player3.getMessage());

        assertMessageEquals(player3, tableConfigurationMessage);

        assertMessageEquals(player3,
                new OwnStationConfigurationMessage(
                        "player3",
                        null,
                        List.of(
                                playableCards.get("resource_24"),
                                playableCards.get("resource_09"),
                                playableCards.get("gold_14")
                        ),
                        Map.of(
                                Symbol.ANIMAL, 0,
                                Symbol.MUSHROOM, 0,
                                Symbol.VEGETABLE, 0,
                                Symbol.INSECT, 0,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        null,
                        0,
                        playableCards.get("initial_06"),
                        goalCards.get("goal_07"),
                        goalCards.get("goal_06"),
                        List.of()
                ));
        for(String nickname : List.of("player1","player2")) {
            assertMessageEquals(player3,
                    new OtherStationConfigurationMessage(
                            nickname,
                            null,
                            Map.of(
                                    Symbol.ANIMAL, 0,
                                    Symbol.MUSHROOM, 0,
                                    Symbol.VEGETABLE, 0,
                                    Symbol.INSECT, 0,
                                    Symbol.INK, 0,
                                    Symbol.FEATHER, 0,
                                    Symbol.SCROLL, 0
                            ),
                            0,
                            List.of()
                    ));
        }
        assertMessageEquals(player3,
                new AvailableColorsMessage(List.of(Color.values())));
        assertMessageEquals(player3,
                new GameConfigurationMessage(
                        GameState.SETUP,
                        null,
                        null,
                        null,
                        false,
                        4
                ));

        this.mainController.registerToGame(player4, "game1");
        this.clearQueue(List.of(player1,player2,player3,player4));
    }

    @Test
    void testGameSetup() {
        this.mainController.createClient(player1, this.player1.getName());
        this.mainController.createClient(player2, this.player2.getName());
        this.mainController.createClient(player3, this.player3.getName());
        this.mainController.createClient(player4, this.player4.getName());

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");
        this.clearQueue(List.of(player1,player2,player3,player4));

        this.mainController.chooseColor(player1, Color.GREEN);
        //All players should receive AcceptedColorMessage
        assertMessageEquals(List.of(player1,player2,player3,player4), new AcceptedColorMessage(player1.getName(), Color.GREEN));
        //All player except player1 should receive AvailableColorsMessage
        assertMessageEquals(List.of(player2,player3,player4), new AvailableColorsMessage(List.of(Color.BLUE,Color.YELLOW,Color.RED)));

        this.mainController.chooseColor(player3, Color.YELLOW);
        //All players should receive AcceptedColorMessage
        assertMessageEquals(List.of(player1,player2,player3,player4), new AcceptedColorMessage(player3.getName(), Color.YELLOW));
        //All player except player3 should receive AvailableColorsMessage
        assertMessageEquals(List.of(player1,player2,player4), new AvailableColorsMessage(List.of(Color.BLUE,Color.RED)));

        this.mainController.chooseColor(player2, Color.BLUE);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //Invalid action on color already taken
        this.mainController.chooseColor(player4, Color.BLUE);
        //player4 should not receive messages
        assertNull(player4.getMessage());

        this.mainController.chooseColor(player4, Color.RED);

        this.clearQueue(List.of(player1,player2,player3,player4));

        this.mainController.placeInitialCard(player1, CardOrientation.DOWN);

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new AcceptedPlaceInitialCard("player1",
                        playableCards.get("initial_05").setCardState(CardOrientation.DOWN),
                        Map.of(
                                Symbol.ANIMAL, 1,
                                Symbol.MUSHROOM, 0,
                                Symbol.VEGETABLE, 1,
                                Symbol.INSECT, 1,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        )
                ));

        //if the card is already placed by a player and tries to be replaced, no message should be sent
        this.mainController.placeInitialCard(player1, CardOrientation.UP);
        assertNull(player1.getMessage());

        this.mainController.placeInitialCard(player2, CardOrientation.DOWN);

        this.mainController.placeInitialCard(player3, CardOrientation.UP);

        this.mainController.placeInitialCard(player4, CardOrientation.DOWN);

        this.clearQueue(List.of(player1,player2,player3,player4));

        this.mainController.choosePrivateGoalCard(player1, 0);
        //player1 only should receive the chosen private goal card
        assertMessageEquals(player1,
                new AcceptedChooseGoalCard(goalCards.get("goal_09")));
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.mainController.choosePrivateGoalCard(player2, 1);

        this.mainController.choosePrivateGoalCard(player3, 0);

        this.clearQueue(List.of(player1,player2,player3,player4));

        this.mainController.choosePrivateGoalCard(player4, 1);

        assertMessageEquals(player4,
                new AcceptedChooseGoalCard(goalCards.get("goal_04")));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new StartPlayingGameMessage("player1"));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new TurnStateMessage("player1", TurnState.PLACE));


    }

    @Test
    void testPlaceCardMessage() {
        this.mainController.createClient(player1, this.player1.getName());
        this.mainController.createClient(player2, this.player2.getName());
        this.mainController.createClient(player3, this.player3.getName());
        this.mainController.createClient(player4, this.player4.getName());

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.mainController.chooseColor(player1, Color.GREEN);
        this.mainController.chooseColor(player3, Color.YELLOW);
        this.mainController.chooseColor(player2, Color.BLUE);
        this.mainController.chooseColor(player4, Color.RED);

        this.mainController.placeInitialCard(player1, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player2, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player3, CardOrientation.UP);
        this.mainController.placeInitialCard(player4, CardOrientation.DOWN);

        this.mainController.choosePrivateGoalCard(player1, 0);
        this.mainController.choosePrivateGoalCard(player2, 1);
        this.mainController.choosePrivateGoalCard(player3, 0);
        this.mainController.choosePrivateGoalCard(player4, 1);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //player1 turn
        //player2 action should not work and no message should be sent
        this.mainController.placeCard(player2, "resource_15", "initial_01", Direction.UP_RIGHT, CardOrientation.UP);
        assertNull(player1.getMessage());
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.mainController.placeCard(player1, "resource_01", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageEquals(List.of(player1,player2,player3,player4),
                new AcceptedPlaceCardMessage("player1",
                        "initial_05", playableCards.get("resource_01").setCardState(CardOrientation.UP),
                        Direction.UP_RIGHT,
                        Map.of(
                                Symbol.ANIMAL, 1,
                                Symbol.MUSHROOM, 2,
                                Symbol.VEGETABLE, 1,
                                Symbol.INSECT, 1,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        0));
    }

    @Test
    void testPickCardFromDeckMessage() {
        this.mainController.createClient(player1, this.player1.getName());
        this.mainController.createClient(player2, this.player2.getName());
        this.mainController.createClient(player3, this.player3.getName());
        this.mainController.createClient(player4, this.player4.getName());

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.mainController.chooseColor(player1, Color.GREEN);
        this.mainController.chooseColor(player3, Color.YELLOW);
        this.mainController.chooseColor(player2, Color.BLUE);
        this.mainController.chooseColor(player4, Color.RED);

        this.mainController.placeInitialCard(player1, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player2, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player3, CardOrientation.UP);
        this.mainController.placeInitialCard(player4, CardOrientation.DOWN);

        this.mainController.choosePrivateGoalCard(player1, 0);
        this.mainController.choosePrivateGoalCard(player2, 1);
        this.mainController.choosePrivateGoalCard(player3, 0);
        this.mainController.choosePrivateGoalCard(player4, 1);

        this.mainController.placeCard(player1, "resource_01", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //player2 action should not work and no message should be sent
        this.mainController.pickCardFromDeck(player2, PlayableCardType.GOLD);
        assertNull(player1.getMessage());
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.mainController.pickCardFromDeck(player1, PlayableCardType.RESOURCE);
        assertMessageEquals(player1,
                new OwnAcceptedPickCardFromDeckMessage("player1", playableCards.get("resource_18"), PlayableCardType.RESOURCE, Symbol.INSECT));
        assertMessageEquals(List.of(player2,player3,player4),
                new OtherAcceptedPickCardFromDeckMessage("player1", PlayableCardType.RESOURCE, Symbol.INSECT));
    }

    @Test
    void testPickCardFromTableMessage() {
        this.mainController.createClient(player1, this.player1.getName());
        this.mainController.createClient(player2, this.player2.getName());
        this.mainController.createClient(player3, this.player3.getName());
        this.mainController.createClient(player4, this.player4.getName());

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.mainController.chooseColor(player1, Color.GREEN);
        this.mainController.chooseColor(player3, Color.YELLOW);
        this.mainController.chooseColor(player2, Color.BLUE);
        this.mainController.chooseColor(player4, Color.RED);

        this.mainController.placeInitialCard(player1, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player2, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player3, CardOrientation.UP);
        this.mainController.placeInitialCard(player4, CardOrientation.DOWN);

        this.mainController.choosePrivateGoalCard(player1, 0);
        this.mainController.choosePrivateGoalCard(player2, 1);
        this.mainController.choosePrivateGoalCard(player3, 0);
        this.mainController.choosePrivateGoalCard(player4, 1);

        this.mainController.placeCard(player1, "resource_01", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //player2 action should not work and no message should be sent
        this.mainController.pickCardFromTable(player2, PlayableCardType.GOLD, 0);
        assertNull(player1.getMessage());
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.mainController.pickCardFromTable(player1, PlayableCardType.RESOURCE, 0);
        assertMessageEquals(List.of(player1,player2,player3,player4),
                new AcceptedPickCardFromTable(
                        "player1",
                        playableCards.get("resource_05"),
                        Symbol.INSECT,
                        0, PlayableCardType.RESOURCE,
                        playableCards.get("resource_18"))
        );
    }

    @Test
    void testGameEventsMessages() {
        this.mainController.createClient(player1, this.player1.getName());
        this.mainController.createClient(player2, this.player2.getName());
        this.mainController.createClient(player3, this.player3.getName());
        this.mainController.createClient(player4, this.player4.getName());

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.mainController.chooseColor(player1, Color.GREEN);
        this.mainController.chooseColor(player3, Color.YELLOW);
        this.mainController.chooseColor(player2, Color.BLUE);
        this.mainController.chooseColor(player4, Color.RED);

        this.mainController.placeInitialCard(player1, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player2, CardOrientation.DOWN);
        this.mainController.placeInitialCard(player3, CardOrientation.UP);
        this.mainController.placeInitialCard(player4, CardOrientation.DOWN);

        this.mainController.choosePrivateGoalCard(player1, 0);
        this.mainController.choosePrivateGoalCard(player2, 1);
        this.mainController.choosePrivateGoalCard(player3, 0);
        this.clearQueue(List.of(player1,player2,player3,player4));


        this.mainController.choosePrivateGoalCard(player4, 1);

        assertMessageEquals(player4,
                new AcceptedChooseGoalCard(goalCards.get("goal_04")));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new StartPlayingGameMessage("player1"));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new TurnStateMessage("player1", TurnState.PLACE));

    }


    private void assertMessageEquals(ClientStub receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }
    private void assertMessageEquals(List<ClientStub> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(ClientStub::getName).toList();
        message.setHeader(receiversName);
        for(ClientStub receiver : receivers) {
            assertEquals(message, receiver.getMessage());
        }
    }

    private  void clearQueue(List<ClientStub> clients) {
        for(ClientStub player : clients) {
            player.clearQueue();
        }
    }
}
