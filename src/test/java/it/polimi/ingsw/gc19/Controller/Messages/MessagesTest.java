package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OtherStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.RemoteException;
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
        this.mainController = MainController.getMainController();

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

    @AfterEach
    void tearDown() {
        mainController.resetMainController();
        this.player1.setGameController(null);
        this.player2.setGameController(null);
        this.player3.setGameController(null);
        this.player4.setGameController(null);
    }

    @Test
    void testGameCreationAndConfiguration(){
        this.mainController.createClient(player1);
        this.mainController.createClient(player2);
        this.mainController.createClient(player3);
        this.mainController.createClient(player4);

        this.mainController.createGame("game1", 4, this.player1, 1);

        assertEquals(new CreatedGameMessage("game1").setHeader(player1.getUsername()), player1.getMessage());

        assertEquals(new JoinedGameMessage("game1").setHeader(player1.getUsername()), player1.getMessage());

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

        assertEquals(new JoinedGameMessage("game1").setHeader(player2.getUsername()), player2.getMessage());


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

        assertEquals(new JoinedGameMessage("game1").setHeader(player3.getUsername()), player3.getMessage());

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
    void testGameSetup() throws RemoteException {
        this.mainController.createClient(player1);
        this.mainController.createClient(player2);
        this.mainController.createClient(player3);
        this.mainController.createClient(player4);

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");
        this.clearQueue(List.of(player1,player2,player3,player4));

        this.player1.chooseColor(Color.GREEN);
        //All players should receive AcceptedColorMessage
        assertMessageEquals(List.of(player1,player2,player3,player4), new AcceptedColorMessage(player1.getUsername(), Color.GREEN));
        //All player except player1 should receive AvailableColorsMessage
        assertMessageEquals(List.of(player2,player3,player4), new AvailableColorsMessage(List.of(Color.BLUE,Color.YELLOW,Color.RED)));

        this.player3.chooseColor(Color.YELLOW);
        //All players should receive AcceptedColorMessage
        assertMessageEquals(List.of(player1,player2,player3,player4), new AcceptedColorMessage(player3.getUsername(), Color.YELLOW));
        //All player except player3 should receive AvailableColorsMessage
        assertMessageEquals(List.of(player1,player2,player4), new AvailableColorsMessage(List.of(Color.BLUE,Color.RED)));

        this.player2.chooseColor(Color.BLUE);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //Invalid action on color already taken
        this.player4.chooseColor(Color.BLUE);

        // @info: before was
        //player4 should not receive messages
        //assertNull(player4.getMessage());
        // @info: now
        assertMessageEquals(player4, new RefusedActionMessage(ErrorType.COLOR_ALREADY_CHOSEN, "The color BLUE was already taken"));

        this.player4.chooseColor(Color.RED);

        this.clearQueue(List.of(player1,player2,player3,player4));

        this.player1.placeInitialCard(CardOrientation.DOWN);

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
        this.player1.placeInitialCard(CardOrientation.UP);
        assertNull(player1.getMessage());

        this.player2.placeInitialCard(CardOrientation.DOWN);

        this.player3.placeInitialCard(CardOrientation.UP);

        this.player4.placeInitialCard(CardOrientation.DOWN);

        this.clearQueue(List.of(player1,player2,player3,player4));

        this.player1.choosePrivateGoalCard(0);
        //player1 only should receive the chosen private goal card
        assertMessageEquals(player1,
                new AcceptedChooseGoalCard(goalCards.get("goal_09")));
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.player2.choosePrivateGoalCard(1);

        this.player3.choosePrivateGoalCard(0);

        this.clearQueue(List.of(player1,player2,player3,player4));

        this.player4.choosePrivateGoalCard(1);

        assertMessageEquals(player4,
                new AcceptedChooseGoalCard(goalCards.get("goal_04")));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new StartPlayingGameMessage("player1"));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new TurnStateMessage("player1", TurnState.PLACE));


    }

    @Test
    void testPlaceCardMessage() throws RemoteException {
        this.mainController.createClient(player1);
        this.mainController.createClient(player2);
        this.mainController.createClient(player3);
        this.mainController.createClient(player4);

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.player1.chooseColor(Color.GREEN);
        this.player3.chooseColor(Color.YELLOW);
        this.player2.chooseColor(Color.BLUE);
        this.player4.chooseColor(Color.RED);

        this.player1.placeInitialCard(CardOrientation.DOWN);
        this.player2.placeInitialCard(CardOrientation.DOWN);
        this.player3.placeInitialCard(CardOrientation.UP);
        this.player4.placeInitialCard(CardOrientation.DOWN);

        this.player1.choosePrivateGoalCard(0);
        this.player2.choosePrivateGoalCard(1);
        this.player3.choosePrivateGoalCard(0);
        this.player4.choosePrivateGoalCard(1);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //player1 turn
        //player2 action should not work and no message should be sent
        this.player2.placeCard("resource_15", "initial_01", Direction.UP_RIGHT, CardOrientation.UP);
        assertNull(player1.getMessage());
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.player1.placeCard("resource_01", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageEquals(List.of(player1,player2,player3,player4),
                new AcceptedPlacePlayableCardMessage("player1",
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
    void testPickCardFromDeckMessage() throws RemoteException {
        this.mainController.createClient(player1);
        this.mainController.createClient(player2);
        this.mainController.createClient(player3);
        this.mainController.createClient(player4);

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.player1.chooseColor(Color.GREEN);
        this.player3.chooseColor(Color.YELLOW);
        this.player2.chooseColor(Color.BLUE);
        this.player4.chooseColor(Color.RED);

        this.player1.placeInitialCard(CardOrientation.DOWN);
        this.player2.placeInitialCard(CardOrientation.DOWN);
        this.player3.placeInitialCard(CardOrientation.UP);
        this.player4.placeInitialCard(CardOrientation.DOWN);

        this.player1.choosePrivateGoalCard(0);
        this.player2.choosePrivateGoalCard(1);
        this.player3.choosePrivateGoalCard(0);
        this.player4.choosePrivateGoalCard(1);

        this.player1.placeCard("resource_01", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //player2 action should not work and no message should be sent
        this.player2.pickCardFromDeck(PlayableCardType.GOLD);
        assertNull(player1.getMessage());
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.player1.pickCardFromDeck(PlayableCardType.RESOURCE);
        assertMessageEquals(player1,
                new OwnAcceptedPickCardFromDeckMessage("player1", playableCards.get("resource_18"), PlayableCardType.RESOURCE, Symbol.INSECT));
        assertMessageEquals(List.of(player2,player3,player4),
                new OtherAcceptedPickCardFromDeckMessage("player1", PlayableCardType.RESOURCE, Symbol.INSECT));
    }

    @Test
    void testPickCardFromTableMessage() throws RemoteException {
        this.mainController.createClient(player1);
        this.mainController.createClient(player2);
        this.mainController.createClient(player3);
        this.mainController.createClient(player4);

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.player1.chooseColor(Color.GREEN);
        this.player3.chooseColor(Color.YELLOW);
        this.player2.chooseColor(Color.BLUE);
        this.player4.chooseColor(Color.RED);

        this.player1.placeInitialCard(CardOrientation.DOWN);
        this.player2.placeInitialCard(CardOrientation.DOWN);
        this.player3.placeInitialCard(CardOrientation.UP);
        this.player4.placeInitialCard(CardOrientation.DOWN);

        this.player1.choosePrivateGoalCard(0);
        this.player2.choosePrivateGoalCard(1);
        this.player3.choosePrivateGoalCard(0);
        this.player4.choosePrivateGoalCard(1);

        this.player1.placeCard("resource_01", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        this.clearQueue(List.of(player1,player2,player3,player4));

        //player2 action should not work and no message should be sent
        this.player2.pickCardFromTable(PlayableCardType.GOLD, 0);
        assertNull(player1.getMessage());
        assertNull(player2.getMessage());
        assertNull(player3.getMessage());
        assertNull(player4.getMessage());

        this.player1.pickCardFromTable(PlayableCardType.RESOURCE, 0);
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
    void testGameEventsMessages() throws RemoteException {
        this.mainController.createClient(player1);
        this.mainController.createClient(player2);
        this.mainController.createClient(player3);
        this.mainController.createClient(player4);

        this.mainController.createGame("game1", 4, this.player1, 1);
        this.mainController.registerToGame(player2, "game1");
        this.mainController.registerToGame(player3, "game1");
        this.mainController.registerToGame(player4, "game1");


        this.player1.chooseColor(Color.GREEN);
        this.player3.chooseColor(Color.YELLOW);
        this.player2.chooseColor(Color.BLUE);
        this.player4.chooseColor(Color.RED);

        this.player1.placeInitialCard(CardOrientation.DOWN);
        this.player2.placeInitialCard(CardOrientation.DOWN);
        this.player3.placeInitialCard(CardOrientation.UP);
        this.player4.placeInitialCard(CardOrientation.DOWN);

        this.player1.choosePrivateGoalCard(0);
        this.player2.choosePrivateGoalCard(1);
        this.player3.choosePrivateGoalCard(0);
        this.clearQueue(List.of(player1,player2,player3,player4));


        this.player4.choosePrivateGoalCard(1);

        assertMessageEquals(player4,
                new AcceptedChooseGoalCard(goalCards.get("goal_04")));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new StartPlayingGameMessage("player1"));

        assertMessageEquals(List.of(player1,player2,player3,player4),
                new TurnStateMessage("player1", TurnState.PLACE));

        this.clearQueue(List.of(player1,player2,player3,player4));

        mainController.setPlayerInactive(player2.getUsername());

        assertMessageEquals(List.of(player1,player3,player4),
                new DisconnectedPlayerMessage("player2"));

        mainController.setPlayerInactive(player3.getUsername());

        assertMessageEquals(List.of(player1,player4),
                new DisconnectedPlayerMessage("player3"));
        assertNull(player2.getMessage());

        mainController.setPlayerInactive(player4.getUsername());

        assertMessageEquals(player1,
                new DisconnectedPlayerMessage("player4"));
        assertNull(player2.getMessage());
        assertNull(player4.getMessage());

        assertMessageEquals(player1,
                new GamePausedMessage());

        mainController.reconnect(player2);

        assertMessageEquals(player1,
                new PlayerReconnectedToGameMessage("player2"));

        assertEquals(new JoinedGameMessage("game1").setHeader(player2.getUsername()),
                player2.getMessage());

        assertMessageEquals(player2,
                new TableConfigurationMessage(
                        playableCards.get("resource_05").setCardState(CardOrientation.UP),
                        playableCards.get("resource_21").setCardState(CardOrientation.UP),
                        playableCards.get("gold_19").setCardState(CardOrientation.UP),
                        playableCards.get("gold_23").setCardState(CardOrientation.UP),
                        goalCards.get("goal_11"),
                        goalCards.get("goal_15"),
                        Symbol.VEGETABLE,
                        Symbol.INSECT
                ));

        assertMessageEquals(player2,
                new OwnStationConfigurationMessage(
                        "player2",
                        Color.BLUE,
                        List.of(
                                playableCards.get("resource_15"),
                                playableCards.get("resource_37"),
                                playableCards.get("gold_21")
                        ),
                        Map.of(
                                Symbol.ANIMAL, 0,
                                Symbol.MUSHROOM, 0,
                                Symbol.VEGETABLE, 1,
                                Symbol.INSECT, 2,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        goalCards.get("goal_01"),
                        0,
                        playableCards.get("initial_01").setCardState(CardOrientation.DOWN),
                        goalCards.get("goal_16"),
                        goalCards.get("goal_01"),
                        List.of(
                                new Tuple<>(playableCards.get("initial_01"), new Tuple<>(25,25))
                        )
                ));

        assertMessageEquals(player2,
                new OtherStationConfigurationMessage(
                        "player1",
                        Color.GREEN,
                        Map.of(
                                Symbol.ANIMAL, 1,
                                Symbol.MUSHROOM, 0,
                                Symbol.VEGETABLE, 1,
                                Symbol.INSECT, 1,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        0,
                        List.of(
                                new Tuple<>(playableCards.get("initial_05").setCardState(CardOrientation.DOWN), new Tuple<>(25,25))
                        )
                ));
        assertMessageEquals(player2,
                new OtherStationConfigurationMessage(
                        "player3",
                        Color.YELLOW,
                        Map.of(
                                Symbol.ANIMAL, 1,
                                Symbol.MUSHROOM, 1,
                                Symbol.VEGETABLE, 1,
                                Symbol.INSECT, 1,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        0,
                        List.of(
                                new Tuple<>(playableCards.get("initial_06").setCardState(CardOrientation.UP), new Tuple<>(25,25))
                        )
                ));

        assertMessageEquals(player2,
                new OtherStationConfigurationMessage(
                        "player4",
                        Color.RED,
                        Map.of(
                                Symbol.ANIMAL, 0,
                                Symbol.MUSHROOM, 1,
                                Symbol.VEGETABLE, 1,
                                Symbol.INSECT, 0,
                                Symbol.INK, 0,
                                Symbol.FEATHER, 0,
                                Symbol.SCROLL, 0
                        ),
                        0,
                        List.of(
                                new Tuple<>(playableCards.get("initial_03").setCardState(CardOrientation.DOWN), new Tuple<>(25,25))
                        )
                ));

        assertMessageEquals(player2,
                new GameConfigurationMessage(
                        GameState.PAUSE,
                        TurnState.PLACE,
                        "player1",
                        "player1",
                        false,
                        4
                ));

        assertMessageEquals(List.of(player1,player2),
                new GameResumedMessage());
    }


    private void assertMessageEquals(ClientStub receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }
    private void assertMessageEquals(List<ClientStub> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(ClientStub::getUsername).toList();
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
