package it.polimi.ingsw.gc19.View.LocalModel;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OtherStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GameLocalView.*;
import it.polimi.ingsw.gc19.View.TUI.TUIView;
import it.polimi.ingsw.gc19.View.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class LocalModelTest {

    private ClientController clientController;
    private MessageHandler messageHandler;
    private ClientTCP clientTCP;
    private Map<String, PlayableCard> playableCards;
    private Map<String, GoalCard> goalCards;

    @BeforeEach
    public void setUpTest() throws IOException {
        try {
            this.playableCards = JSONParser.readPlayableCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
            this.goalCards = JSONParser.readGoalCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
        }
        catch(IOException e){
            e.printStackTrace();
        }

        ServerApp.startTCP();
        clientController = new ClientController();
        messageHandler = new MessageHandler(clientController);
        clientTCP = new ClientTCP(messageHandler);
        clientController.setClientInterface(clientTCP);
        messageHandler.start();

        clientController.setView(new TUIView(new CommandParser(clientController)));

    }

    @AfterEach
    public void resetTest(){
        clientTCP.disconnect();
        clientTCP.stopClient();
        ServerApp.stopTCP();
    }

    @Test
    public void createLocalModel() throws InterruptedException {
        assertNull(clientController.getLocalModel());
        clientController.createPlayer("player1");
        assertNull(clientController.getLocalModel());
        Thread.sleep(500);
        assertNull(clientController.getLocalModel());
        clientController.createGame("game1", 2);
        Thread.sleep(500);
        assertNotNull(clientController.getLocalModel());
        assertEquals(0, clientController.getLocalModel().getOtherStations().size());
        assertNotNull(clientController.getLocalModel().getPersonalStation());
    }

    @Test
    public void setupLocalModel() throws InterruptedException{
        assertNull(clientController.getLocalModel());
        clientController.createPlayer("player1");
        assertNull(clientController.getLocalModel());
        Thread.sleep(500);
        assertNull(clientController.getLocalModel());
        clientController.createGame("game1", 2);
        Thread.sleep(500);
        assertNotNull(clientController.getLocalModel());
        messageHandler.update(new CreatedGameMessage("game1"));

        LocalModel localModel = clientController.getLocalModel();

        assertEquals(1, localModel.getNumActivePlayers());
        assertEquals(0, localModel.getOtherStations().size());
        assertEquals(0, localModel.getMessages().size());
        assertEquals("player1", localModel.getNickname());
        assertEquals("game1", localModel.getGameName());

        // ------------------
        // Available colors
        assertEquals(localModel.getAvailableColors(), List.of(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED));
        messageHandler.update(new AvailableColorsMessage(List.of(Color.RED, Color.GREEN, Color.BLUE)));
        Thread.sleep(500);
        assertEquals(localModel.getAvailableColors(), List.of(Color.RED, Color.GREEN, Color.BLUE));

        // ------------------
        // Game configuration
        messageHandler.update(new GameConfigurationMessage(GameState.PLAYING, TurnState.PLACE, "player1", "player1",
                                                            false, 2));
        Thread.sleep(500);
        assertEquals("player1", localModel.getFirstPlayer());

        // ------------------
        // Table configuration
        messageHandler.update(new TableConfigurationMessage(
                playableCards.get("resource_01").setCardState(CardOrientation.UP),
                playableCards.get("resource_02").setCardState(CardOrientation.UP),
                playableCards.get("gold_01").setCardState(CardOrientation.UP),
                playableCards.get("gold_02").setCardState(CardOrientation.UP),
                goalCards.get("goal_01"),
                goalCards.get("goal_02"),
                Symbol.MUSHROOM,
                Symbol.ANIMAL
        ));
        Thread.sleep(500);
        assertTableEquals(localModel.getTable(), new LocalTable(playableCards.get("resource_01"), playableCards.get("resource_02"), playableCards.get("gold_01"),
                                            playableCards.get("gold_02"), goalCards.get("goal_01"), goalCards.get("goal_02"), Symbol.MUSHROOM, Symbol.ANIMAL));

        // ------------------
        // Own station configuration
        messageHandler.update(new OwnStationConfigurationMessage(
                "player1", null, List.of(playableCards.get("resource_03"), playableCards.get("resource_04")),
                Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                null, 0, playableCards.get("initial_01"),
                goalCards.get("goal_03"), goalCards.get("goal_04"),
                List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2)))
        ));
        Thread.sleep(500);
        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", null,
                        Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2))),
                        null, goalCards.get("goal_03"), goalCards.get("goal_04"),
                        List.of(playableCards.get("resource_03"), playableCards.get("resource_04")), playableCards.get("initial_01")));

        // ------------------
        // Selecting color
        messageHandler.update(new AcceptedColorMessage("player1", Color.RED));
        Thread.sleep(500);
        assertEquals(localModel.getPersonalStation().getChosenColor(), Color.RED);

        // ------------------
        // Other player's station
        messageHandler.update(new OtherStationConfigurationMessage(
                "player2", Color.BLUE,
                List.of(
                        new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                        new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE)
                ),
                Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                0, List.of(new Tuple<>(playableCards.get("initial_02"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2)))
        ));
        Thread.sleep(500);
        assertStationEquals(localModel.getOtherStations().get("player2"),
                new OtherStation(
                        "player2", Color.BLUE,
                        Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_02"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2))),
                        List.of(new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                                new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE))));

        // ------------------
        // Picking a card from the deck
        assertEquals(localModel.getPersonalStation().getCardsInHand(), List.of(playableCards.get("resource_03"), playableCards.get("resource_04")));
        messageHandler.update(new OwnAcceptedPickCardFromDeckMessage("player1", playableCards.get("resource_10"), PlayableCardType.RESOURCE, Symbol.MUSHROOM));
        Thread.sleep(500);
        assertEquals(localModel.getPersonalStation().getCardsInHand(), List.of(playableCards.get("resource_03"), playableCards.get("resource_04"), playableCards.get("resource_10")));

        // ------------------
        // Placing a card
        assertTrue(localModel.isCardPlaceablePersonalStation(playableCards.get("resource_03"), playableCards.get("initial_01"), Direction.DOWN_RIGHT));
        messageHandler.update(new AcceptedPlacePlayableCardMessage("player1", "initial_01", playableCards.get("resource_03"),
                Direction.DOWN_RIGHT, Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 2, Symbol.VEGETABLE, 0, Symbol.INSECT, 1),
                0));
        Thread.sleep(500);
        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", Color.RED,
                        Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 2, Symbol.VEGETABLE, 0, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2)),
                        new Tuple<>(playableCards.get("resource_03"), new Tuple<>(ImportantConstants.gridDimension/2+1, ImportantConstants.gridDimension/2+1))),
                        null, goalCards.get("goal_03"), goalCards.get("goal_04"),
                        List.of(playableCards.get("resource_04"), playableCards.get("resource_10")), playableCards.get("initial_01")));

        // ------------------
        // Other player's picking up cards
        messageHandler.update(new OtherAcceptedPickCardFromDeckMessage("player2", new Tuple<>(Symbol.INSECT,PlayableCardType.RESOURCE), PlayableCardType.GOLD, Symbol.INSECT));
        Thread.sleep(500);
        assertEquals(localModel.getOtherStations().get("player2").getBackCardHand(),
                List.of(
                        new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                        new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                        new Tuple<>(Symbol.INSECT, PlayableCardType.RESOURCE)
                ));

        // ------------------
        // Sending messages
        assertEquals(0, localModel.getMessages().size());
        messageHandler.update(new NotifyChatMessage("player2", "This is a sample message"));
        Thread.sleep(500);
        assertEquals(1, localModel.getMessages().size());
        assertChatMessageEquals(localModel.getMessages(), new ArrayList<>(List.of(
                new Message("This is a sample message", "player2"))));


        // ------------------
        // Picking a card from the table
        assertTableEquals(localModel.getTable(), new LocalTable(playableCards.get("resource_01"), playableCards.get("resource_02"), playableCards.get("gold_01"),
                playableCards.get("gold_02"), goalCards.get("goal_01"), goalCards.get("goal_02"), Symbol.MUSHROOM, Symbol.INSECT));
        assertEquals(localModel.getPersonalStation().getCardsInHand(), List.of(playableCards.get("resource_04"), playableCards.get("resource_10")));

        messageHandler.update(new AcceptedPickCardFromTable("player1", playableCards.get("resource_01"),
                Symbol.MUSHROOM, 0, PlayableCardType.RESOURCE, playableCards.get("resource_05")));
        Thread.sleep(500);
        assertTableEquals(localModel.getTable(), new LocalTable(playableCards.get("resource_05"), playableCards.get("resource_02"), playableCards.get("gold_01"),
                playableCards.get("gold_02"), goalCards.get("goal_01"), goalCards.get("goal_02"), Symbol.MUSHROOM, Symbol.INSECT));
        assertEquals(localModel.getPersonalStation().getCardsInHand(), List.of(playableCards.get("resource_04"), playableCards.get("resource_10"), playableCards.get("resource_01")));

        messageHandler.update(new AcceptedPlacePlayableCardMessage("player1", "initial_01", playableCards.get("resource_01"),
                Direction.UP_RIGHT, Map.of(Symbol.ANIMAL, 0, Symbol.MUSHROOM, 3, Symbol.VEGETABLE, 0, Symbol.INSECT, 1),
                0));
        Thread.sleep(500);
        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", Color.RED,
                        Map.of(Symbol.ANIMAL, 0, Symbol.MUSHROOM, 3, Symbol.VEGETABLE, 0, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2)),
                        new Tuple<>(playableCards.get("resource_03"), new Tuple<>(ImportantConstants.gridDimension/2+1, ImportantConstants.gridDimension/2+1)),
                        new Tuple<>(playableCards.get("resource_01"), new Tuple<>(ImportantConstants.gridDimension/2-1, ImportantConstants.gridDimension/2+1))),
                        null, goalCards.get("goal_03"), goalCards.get("goal_04"),
                        List.of(playableCards.get("resource_04"), playableCards.get("resource_10")), playableCards.get("initial_01")));

        // ------------------
        // Choosing goal card
        messageHandler.update(new AcceptedChooseGoalCardMessage(goalCards.get("goal_03")));
        Thread.sleep(500);
        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", Color.RED,
                        Map.of(Symbol.ANIMAL, 0, Symbol.MUSHROOM, 3, Symbol.VEGETABLE, 0, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2)),
                        new Tuple<>(playableCards.get("resource_03"), new Tuple<>(ImportantConstants.gridDimension/2+1, ImportantConstants.gridDimension/2+1)),
                        new Tuple<>(playableCards.get("resource_01"), new Tuple<>(ImportantConstants.gridDimension/2-1, ImportantConstants.gridDimension/2+1))),
                        goalCards.get("goal_03"), goalCards.get("goal_03"), goalCards.get("goal_04"),
                        List.of(playableCards.get("resource_04"), playableCards.get("resource_10")), playableCards.get("initial_01")));

        // ---------------------------
        // Other player placing a card
        assertFalse(localModel.getOtherStations().get("player2").cardIsPlaceable(playableCards.get("gold_04"), playableCards.get("initial_02"), Direction.DOWN_RIGHT));
        assertEquals(localModel.getOtherStations().get("player2").getBackCardHand(),
                List.of(
                        new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                        new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                        new Tuple<>(Symbol.INSECT, PlayableCardType.RESOURCE)
                ));
        messageHandler.update(new AcceptedPlacePlayableCardMessage("player2", "initial_02", playableCards.get("gold_04"), Direction.DOWN_RIGHT,
                Map.of(Symbol.ANIMAL, 2, Symbol.MUSHROOM, 4, Symbol.VEGETABLE, 1, Symbol.INSECT, 0), 0));
        Thread.sleep(500);
        assertStationEquals(localModel.getOtherStations().get("player2"),
                new OtherStation(
                        "player2", Color.BLUE,
                        Map.of(Symbol.ANIMAL, 2, Symbol.MUSHROOM, 4, Symbol.VEGETABLE, 1, Symbol.INSECT, 0),
                        0, List.of(new Tuple<>(playableCards.get("initial_02"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2)),
                                            new Tuple<>(playableCards.get("gold_04"), new Tuple<>(ImportantConstants.gridDimension/2+1, ImportantConstants.gridDimension/2+1))),
                        List.of(
                                new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                                new Tuple<>(Symbol.VEGETABLE, PlayableCardType.RESOURCE),
                                new Tuple<>(Symbol.INSECT, PlayableCardType.RESOURCE)
                        )));


        // --------------------------------------------------------
        // Simulating disconnection: other player
        assertTrue(localModel.getOtherStations().containsKey("player2"));
        assertEquals(State.ACTIVE, localModel.getPlayerState("player2"));
        assertEquals(State.ACTIVE, localModel.getPlayerState("player1"));
        assertEquals(2, localModel.getNumPlayers());
        assertEquals(2, localModel.getNumActivePlayers());

        messageHandler.update(new DisconnectedPlayerMessage("player2"));
        Thread.sleep(500);

        assertEquals(2, localModel.getNumPlayers());
        assertEquals(1, localModel.getNumActivePlayers());
        assertEquals(State.INACTIVE, localModel.getPlayerState("player2"));

    }


    @Test
    public void initialPlaceTest() throws InterruptedException {
        assertNull(clientController.getLocalModel());
        clientController.createPlayer("player1");
        assertNull(clientController.getLocalModel());
        Thread.sleep(500);
        assertNull(clientController.getLocalModel());
        clientController.createGame("game1", 2);
        Thread.sleep(500);
        assertNotNull(clientController.getLocalModel());
        messageHandler.update(new CreatedGameMessage("game1"));

        LocalModel localModel = clientController.getLocalModel();

        messageHandler.update(new OwnStationConfigurationMessage(
                "player1", null, List.of(playableCards.get("resource_03"), playableCards.get("resource_04")),
                Map.of(),
                null, 0, playableCards.get("initial_01"),
                goalCards.get("goal_03"), goalCards.get("goal_04"),
                List.of()
        ));
        Thread.sleep(500);
        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", null,
                        Map.of(), 0, List.of(),
                        null, goalCards.get("goal_03"), goalCards.get("goal_04"),
                        List.of(playableCards.get("resource_03"), playableCards.get("resource_04")), playableCards.get("initial_01")));


        messageHandler.update(new OtherStationConfigurationMessage(
                "player2", null, List.of(),
                Map.of(), 0, List.of()
        ));
        Thread.sleep(500);
        assertStationEquals(localModel.getOtherStations().get("player2"),
                new OtherStation("player2", null,
                        Map.of(), 0, List.of()));



        messageHandler.update(new AcceptedPlaceInitialCard("player1", playableCards.get("initial_01"),
                Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1)));
        Thread.sleep(500);
        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", null,
                        Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2))),
                        null, goalCards.get("goal_03"), goalCards.get("goal_04"),
                        List.of(playableCards.get("resource_03"), playableCards.get("resource_04")), playableCards.get("initial_01")));


        messageHandler.update(new AcceptedPlaceInitialCard("player2", playableCards.get("initial_02"),
                Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1)));
        Thread.sleep(500);
        assertStationEquals(localModel.getOtherStations().get("player2"),
                new OtherStation("player2", null,
                        Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_02"), new Tuple<>(ImportantConstants.gridDimension/2, ImportantConstants.gridDimension/2)))));
    }

    public void assertTableEquals(LocalTable table1, LocalTable table2){
        assertEquals(table1.getPublicGoal1(), table2.getPublicGoal1());
        assertEquals(table1.getPublicGoal2(), table2.getPublicGoal2());
        assertEquals(table1.getGold1(), table2.getGold1());
        assertEquals(table1.getGold2(), table2.getGold2());
        assertEquals(table1.getResource1(), table2.getResource1());
        assertEquals(table1.getResource2(), table2.getResource2());
        assertEquals(table1.getNextSeedOfGoldDeck(), table2.getNextSeedOfGoldDeck());
        assertEquals(table1.getNextSeedOfResourceDeck(), table2.getNextSeedOfResourceDeck());
    }

    public void assertStationEquals(OtherStation station1, OtherStation station2){
        assertEquals(station1.getOwnerPlayer(), station2.getOwnerPlayer());
        assertEquals(station1.getPlacedCardSequence(), station2.getPlacedCardSequence());
        assertEquals(station1.getChosenColor(), station2.getChosenColor());
        assertEquals(station1.getNumPoints(), station2.getNumPoints());
        assertEquals(station1.getBackCardHand(), station2.getBackCardHand());
        assertEquals(station1.getVisibleSymbols(), station2.getVisibleSymbols());
    }

    public void assertStationEquals(PersonalStation station1, PersonalStation station2){
        assertEquals(station1.getOwnerPlayer(), station2.getOwnerPlayer());
        assertEquals(station1.getVisibleSymbols(), station2.getVisibleSymbols());
        assertEquals(station1.getPlacedCardSequence(), station2.getPlacedCardSequence());
        assertEquals(station1.getChosenColor(), station2.getChosenColor());
        assertEquals(station1.getNumPoints(), station2.getNumPoints());
        assertEquals(station1.getCardsInHand(), station2.getCardsInHand());
        if(station1.getPrivateGoalCardInStation() == null && station2.getPrivateGoalCardInStation() == null){
            assertEquals(Arrays.stream(station1.getPrivateGoalCardsInStation()).toList(),
                    Arrays.stream(station2.getPrivateGoalCardsInStation()).toList());
        } else {
            assertEquals(station1.getPrivateGoalCardInStation(), station2.getPrivateGoalCardInStation());
        }
    }

    public void assertChatMessageEquals(List<Message> messages1, List<Message> messages2){
        // Because receivers should not be compared a new comparing method should be created.
        assertEquals(messages1.size(), messages2.size());

        for(int i = 0; i<messages1.size(); i++){
            assertEquals(messages1.get(i).getSenderPlayer(), messages2.get(i).getSenderPlayer());
            assertEquals(messages1.get(i).getMessage(), messages2.get(i).getMessage());
        }
    }
}
