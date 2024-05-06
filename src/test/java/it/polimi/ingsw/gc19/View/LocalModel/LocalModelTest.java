package it.polimi.ingsw.gc19.View.LocalModel;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.PickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPlacePlayableCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OtherAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OwnAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OtherStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.GameLocalView.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        ServerApp.startTCP(ServerSettings.DEFAULT_TCP_SERVER_PORT);
        clientController = new ClientController();
        messageHandler = new MessageHandler(clientController);
        clientTCP = new ClientTCP(messageHandler, clientController);
        clientController.setClientInterface(clientTCP);
        messageHandler.setClient(clientTCP);
        messageHandler.start();
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
        assertEquals(0, localModel.getNumActivePlayers());
        assertEquals(0, localModel.getOtherStations().size());
        assertEquals(0, localModel.getMessages().size());
        assertEquals("player1", localModel.getNickname());

        messageHandler.update(new AvailableColorsMessage(List.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)));
        Thread.sleep(500);
        assertEquals(localModel.getAvailableColors(), List.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW));

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

        messageHandler.update(new OwnStationConfigurationMessage(
                "player1", Color.RED, List.of(playableCards.get("resource_03"), playableCards.get("resource_04")),
                Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                null, 0, playableCards.get("initial_01"),
                goalCards.get("goal_03"), goalCards.get("goal_04"),
                List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(25, 25)))
        ));

        Thread.sleep(500);

        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", Color.RED,
                        Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(25, 25))),
                        null, goalCards.get("goal_03"), goalCards.get("goal_04")));

        messageHandler.update(new OtherStationConfigurationMessage(
                "player2", Color.BLUE, List.of(Symbol.VEGETABLE, Symbol.VEGETABLE),
                Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                0, List.of(new Tuple<>(playableCards.get("initial_02"), new Tuple<>(25, 25)))
        ));

        Thread.sleep(500);

        assertStationEquals(localModel.getOtherStations().get("player2"),
                new OtherStation(
                        "player2", Color.BLUE,
                        Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_02"), new Tuple<>(25, 25)))));

        assertEquals(localModel.getPersonalStation().getCardsInHand(), List.of(playableCards.get("resource_03"), playableCards.get("resource_04")));
        messageHandler.update(new OwnAcceptedPickCardFromDeckMessage("player1", playableCards.get("resource_10"), PlayableCardType.RESOURCE, Symbol.MUSHROOM));
        Thread.sleep(500);
        assertEquals(localModel.getPersonalStation().getCardsInHand(), List.of(playableCards.get("resource_03"), playableCards.get("resource_04"), playableCards.get("resource_10")));

        messageHandler.update(new AcceptedPlacePlayableCardMessage("player1", "initial_01", playableCards.get("resource_03"),
                Direction.DOWN_RIGHT, Map.of(Symbol.ANIMAL, 1, Symbol.MUSHROOM, 2, Symbol.VEGETABLE, 0, Symbol.INSECT, 1),
                0));
        Thread.sleep(500);
        assertStationEquals(localModel.getPersonalStation(),
                new PersonalStation("player1", Color.RED,
                        Map.of( Symbol.ANIMAL, 1, Symbol.MUSHROOM, 1, Symbol.VEGETABLE, 1, Symbol.INSECT, 1),
                        0, List.of(new Tuple<>(playableCards.get("initial_01"), new Tuple<>(25, 25)),
                        new Tuple<>(playableCards.get("resource_03"), new Tuple<>(26, 26))),
                        null, goalCards.get("goal_03"), goalCards.get("goal_04")));

        messageHandler.update(new OtherAcceptedPickCardFromDeckMessage("player2", PlayableCardType.GOLD, Symbol.INSECT));
        Thread.sleep(500);
        assertEquals(localModel.getOtherStations().get("player2").getBackCardHand(),
                List.of(PlayableCardType.GOLD));
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
        //assertEquals(station1.getCardsInHand(), station2.getCardsInHand());
        if(station1.getPrivateGoalCardInStation() == null && station2.getPrivateGoalCardInStation() == null){
            assertEquals(station1.getPrivateGoalCardsInStation()[0], station2.getPrivateGoalCardsInStation()[0]);
            assertEquals(station1.getPrivateGoalCardsInStation()[1], station2.getPrivateGoalCardsInStation()[1]);
        } else {
            assertEquals(station1.getPrivateGoalCardInStation(), station2.getPrivateGoalCardInStation());
        }
    }
}
