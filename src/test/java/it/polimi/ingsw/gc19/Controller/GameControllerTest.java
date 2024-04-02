package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    private GameController gameController;
    private HandleClient clientSkeleton;

    @BeforeEach
    public void setUp() {
        setUp(1);
    }
    public void setUp(long randomSeed) {
        try {
            Game game = new Game(4, randomSeed);
            gameController = new GameController(game, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        clientSkeleton = new HandleClient();
    }

    @Test
    public void testAddClient() {
        gameController.addClient("Player 1", clientSkeleton);
        assertEquals(1,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(1,gameController.getConnectedClients().size());
        assertTrue(gameController.getConnectedClients().contains("Player 1"));
        gameController.addClient("Player 2", clientSkeleton);
        assertEquals(2,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(2,gameController.getConnectedClients().size());
        assertTrue(gameController.getConnectedClients().contains("Player 2"));
        gameController.addClient("Player 2", clientSkeleton);
        assertEquals(2,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(2,gameController.getConnectedClients().size());
        assertTrue(gameController.getConnectedClients().contains("Player 2"));
        gameController.addClient("Player 3", clientSkeleton);
        assertEquals(3,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(3,gameController.getConnectedClients().size());
        assertTrue(gameController.getConnectedClients().contains("Player 3"));
        gameController.addClient("Player 4", clientSkeleton);
        assertEquals(4,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(4,gameController.getConnectedClients().size());
        assertTrue(gameController.getConnectedClients().contains("Player 4"));
        gameController.addClient("Player 5", clientSkeleton);
        assertEquals(4,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(4,gameController.getConnectedClients().size());
        assertFalse(gameController.getConnectedClients().contains("Player 5"));
    }

    @Test
    public void testRemoveClient() {
        gameController.removeClient("Player 1");
        assertEquals(0,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(0,gameController.getConnectedClients().size());
        assertFalse(gameController.getConnectedClients().contains("Player 1"));
        gameController.addClient("Player 1", clientSkeleton);
        gameController.removeClient("Player 1");
        assertEquals(1,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(0,gameController.getConnectedClients().size());
        assertFalse(gameController.getConnectedClients().contains("Player 1"));
        allClientsAdded();
        gameController.removeClient("Player 2");
        assertEquals(4,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(3,gameController.getConnectedClients().size());
        assertFalse(gameController.getConnectedClients().contains("Player 2"));
        gameController.removeClient("Player 3");
        assertEquals(4,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(2,gameController.getConnectedClients().size());
        assertFalse(gameController.getConnectedClients().contains("Player 3"));
        gameController.removeClient("Player 1");
        assertEquals(4,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(1,gameController.getConnectedClients().size());
        assertFalse(gameController.getConnectedClients().contains("Player 1"));
        gameController.removeClient("Player 4");
        assertEquals(4,gameController.getGameAssociated().getNumJoinedPlayer());
        assertEquals(0,gameController.getConnectedClients().size());
        assertFalse(gameController.getConnectedClients().contains("Player 4"));
    }

    @Test
    public void testChooseColor() {
        allClientsAdded();

        for (String nick : gameController.getConnectedClients()) {
            assertNull(gameController.getGameAssociated().getPlayerByName(nick).getColor());
        }

        gameController.chooseColor("Player 1", Color.RED);
        assertEquals(Color.RED, gameController.getGameAssociated().getPlayerByName("Player 1").getColor());

        gameController.chooseColor("Player 1", Color.BLUE);
        assertEquals(Color.RED, gameController.getGameAssociated().getPlayerByName("Player 1").getColor());

        gameController.chooseColor("Player 2", Color.RED);
        assertNull(gameController.getGameAssociated().getPlayerByName("Player 2").getColor());

        gameController.chooseColor("Player 2", Color.BLUE);
        assertEquals(Color.BLUE, gameController.getGameAssociated().getPlayerByName("Player 2").getColor());

        gameController.chooseColor("Player 3", Color.YELLOW);
        assertEquals(Color.YELLOW, gameController.getGameAssociated().getPlayerByName("Player 3").getColor());

        gameController.chooseColor("Player 4", Color.GREEN);
        assertEquals(Color.GREEN, gameController.getGameAssociated().getPlayerByName("Player 4").getColor());

    }

    @Test
    public void testChoosePrivateGoal() {
        gameController.addClient("Player 1", clientSkeleton);

        assertNull(gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getPrivateGoalCard());

        gameController.choosePrivateGoal("Player 1", 0);

        GoalCard goal0 = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getPrivateGoalCard();

        assertNotNull(goal0);

        gameController.choosePrivateGoal("Player 1", 1);

        assertEquals(goal0, gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getPrivateGoalCard());

        gameController.choosePrivateGoal("Player 2", 1);
    }

    @Test
    public void testPlaceInitialCard() {
        gameController.addClient("Player 1", clientSkeleton);

        assertFalse(gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getInitialCardIsPlaced());

        gameController.placeInitialCard("Player 1", CardOrientation.UP);

        assertTrue(gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getInitialCardIsPlaced());

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);

        gameController.placeInitialCard("Player 2", CardOrientation.UP);
    }

    @Test
    public void testStartGameAfterInitialCard() {
        setUp(1); // fixed seed to have first player to Player 1

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allClientsAdded();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersChooseColor();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersChoosePrivateGoal();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersPlacedInitialCard();

        assertTrue(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals("Player 1", gameController.getGameAssociated().getFirstPlayer().getName());
    }

    @Test
    public void testStartGameAfterPrivateGoal() {
        setUp(1); // fixed seed to have first player to Player 1

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allClientsAdded();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersPlacedInitialCard();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersChooseColor();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersChoosePrivateGoal();

        assertTrue(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals("Player 1", gameController.getGameAssociated().getFirstPlayer().getName());
    }

    @Test
    public void testStartGameAfterColor() {
        setUp(1); // fixed seed to have first player to Player 1

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allClientsAdded();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersChoosePrivateGoal();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersPlacedInitialCard();

        assertFalse(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.getGameAssociated().getGameState());

        allPlayersChooseColor();

        assertTrue(gameController.getGameAssociated().allPlayersChooseInitialGoalColor());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals("Player 1", gameController.getGameAssociated().getFirstPlayer().getName());
    }

    private void allPlayersPlacedInitialCard() {
        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal() {
        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);
    }

    private void allClientsAdded() {
        gameController.addClient("Player 1", clientSkeleton);
        gameController.addClient("Player 2", clientSkeleton);
        gameController.addClient("Player 3", clientSkeleton);
        gameController.addClient("Player 4", clientSkeleton);
    }

    private void allPlayersChooseColor() {
        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);
    }

    @Test
    public void testPlaceCard() {
        setUp(1); //fixed seed to have first player to Player 1 and its initial card to initial_05 and resource_23 in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);

        // try to place a card while in setup phase
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        assertFalse(isCardPlacedInStation("Player 1", "resource_23"));

        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        //other player actions are not allowed
        gameController.placeCard("Player 2", "resource_15", "initial_01", Direction.UP_LEFT, CardOrientation.UP);

        assertFalse(isCardPlacedInStation("Player 2", "resource_15"));

        //wrong card in hand
        gameController.placeCard("Player 1", "resource_30", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        assertFalse(isCardPlacedInStation("Player 1", "resource_01"));

        //wrong anchor
        gameController.placeCard("Player 1", "resource_23", "initial_01", Direction.UP_RIGHT, CardOrientation.UP);

        assertFalse(isCardPlacedInStation("Player 1", "resource_23"));

        //wrong direction
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.DOWN_RIGHT, CardOrientation.UP);

        assertFalse(isCardPlacedInStation("Player 1", "resource_23"));

        //correct placing
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        assertTrue(isCardPlacedInStation("Player 1", "resource_23"));

        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.DRAW, gameController.getGameAssociated().getTurnState());
        assertEquals("Player 1", gameController.getGameAssociated().getActivePlayer().getName());
    }

    @Test
    public void testDrawCardFromDeck() {
        setUp(1); //fixed seed to have first player to Player 1 and its initial card to initial_05 and resource_23 in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);

        // try to draw a card while in setup phase
        List<PlayableCard> cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());

        gameController.drawCardFromDeck("Player 1", PlayableCardType.RESOURCE);

        List<PlayableCard> cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        // wrong player
        cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 2").getStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());
        gameController.drawCardFromDeck("Player 2", PlayableCardType.RESOURCE);
        cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 2").getStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // wrong card type (INITIAL)
        cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        assertThrows(MalformedParametersException.class, () -> gameController.drawCardFromDeck("Player 1", PlayableCardType.INITIAL));
        cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // correct placing
        cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        gameController.drawCardFromDeck("Player 1", PlayableCardType.RESOURCE);
        cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertTrue(cardsAfter.containsAll(cardsBefore));
        assertEquals(PlayableCardType.RESOURCE, cardsAfter.getLast().getCardType());

        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());
        assertEquals("Player 2", gameController.getGameAssociated().getActivePlayer().getName());
    }

    @Test
    public void testDrawCardFromTable() {
        setUp(1); //fixed seed to have first player to Player 1 and its initial card to initial_05 and resource_23 in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);

        // try to draw a card while in setup phase
        List<PlayableCard> cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());

        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        List<PlayableCard> cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        // wrong player
        cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 2").getStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());
        gameController.drawCardFromTable("Player 2", PlayableCardType.RESOURCE,0);
        cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 2").getStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // wrong card type (INITIAL)
        cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        assertThrows(MalformedParametersException.class, () -> gameController.drawCardFromTable("Player 1", PlayableCardType.INITIAL, 0));
        cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // malformed index
        cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        assertThrows(IndexOutOfBoundsException.class, () -> gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 2));
        cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // correct placing
        cardsBefore = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        PlayableCard cardToDraw = gameController.getGameAssociated().getResourceCardsOnTable()[0];
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);
        cardsAfter = gameController.getGameAssociated().getPlayerByName("Player 1").getStation().getCardsInStation();
        assertTrue(cardsAfter.containsAll(cardsBefore));
        assertTrue(cardsAfter.contains(cardToDraw));
        assertNotEquals(cardToDraw, gameController.getGameAssociated().getResourceCardsOnTable()[0]);

        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());
        assertEquals("Player 2", gameController.getGameAssociated().getActivePlayer().getName());
    }

    @Test
    public void testTurnSkipOfDisconnectedPlayers() {
        setUp(1); // fixed seed to know cards that players have in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        allPlayersPlacedInitialCard();

        // Player 1 turn
        // test that at Player 1 disconnection turn will go to the next player
        assertEquals("Player 1", gameController.getGameAssociated().getActivePlayer().getName());
        gameController.removeClient("Player 1");
        assertEquals("Player 2", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        // Player 2 turn
        gameController.placeCard("Player 2", "resource_37", "initial_01", Direction.UP_RIGHT, CardOrientation.UP);
        // disconnection of Player 3, so turn will go to Player 4 directly
        gameController.removeClient("Player 3");
        gameController.drawCardFromDeck("Player 2", PlayableCardType.RESOURCE);
        assertEquals("Player 4", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        // Player 4 turn
        gameController.placeCard("Player 4", "resource_20", "initial_03", Direction.UP_RIGHT, CardOrientation.UP);
        gameController.drawCardFromDeck("Player 4", PlayableCardType.RESOURCE);
        // turn should go to Player 2
        assertEquals("Player 2", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());
    }

    @Test
    public void testGamePauseAndUnpause() {
        setUp(1); // fixed seed to know cards that players have in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        allPlayersPlacedInitialCard();

        // Player 1 turn
        gameController.removeClient("Player 1");
        // Player 2 turn
        gameController.removeClient("Player 2");
        // Player 3 turn
        gameController.placeCard("Player 3", "resource_09", "initial_06", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameController.removeClient("Player 4");
        //test game is in pause state and active player is player 3
        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.DRAW, gameController.getGameAssociated().getTurnState());

        // test that at disconnection of all players game is in pause
        gameController.removeClient("Player 3");
        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.DRAW, gameController.getGameAssociated().getTurnState());

        // Player 3 and then Player 4 connect to the game, gameState should be PLAYING and turnState should stay DRAW
        gameController.addClient("Player 3", clientSkeleton);
        gameController.addClient("Player 4",clientSkeleton);
        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.DRAW, gameController.getGameAssociated().getTurnState());

        // disconnection of Player 4 and then Player 3: game in pause again
        gameController.removeClient("Player 4");
        gameController.removeClient("Player 3");
        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.DRAW, gameController.getGameAssociated().getTurnState());

        // Connection of Player 1, active player should change to him and turnState should become PLACE
        gameController.addClient("Player 1",clientSkeleton);
        assertEquals("Player 1", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        // Connection of Player 2, game should return to PLAYING state
        gameController.addClient("Player 2", clientSkeleton);
        assertEquals("Player 1", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());
    }

    @Test
    public void testGamePauseAndEndOneClient() {
        setUp(1); // fixed seed to know cards that players have in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        allPlayersPlacedInitialCard();

        // Player 1 turn
        gameController.removeClient("Player 1");
        // Player 2 turn
        gameController.removeClient("Player 2");
        // Player 3 turn
        gameController.removeClient("Player 4");

        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        sleep(500);

        gameController.addClient("Player 4", clientSkeleton);

        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        sleep(1000);

        // check that game has not been stopped
        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        gameController.removeClient("Player 4");

        sleep(1500);
        // check that game should have been stopped, and Player 3 is the winner
        assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals("Player 3", gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());
    }

    @Test
    public void testGamePauseAndEndZeroClients() {
        setUp(1); // fixed seed to know cards that players have in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        allPlayersPlacedInitialCard();

        // Player 1 turn
        gameController.removeClient("Player 1");
        // Player 2 turn
        gameController.removeClient("Player 2");
        // Player 3 turn
        gameController.removeClient("Player 4");

        gameController.removeClient("Player 3");

        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());
        assertTrue(gameController.getConnectedClients().isEmpty());

        sleep(500);

        gameController.addClient("Player 3", clientSkeleton);
        gameController.addClient("Player 4", clientSkeleton);

        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        sleep(1000);

        // check that game has not been stopped
        assertEquals("Player 3", gameController.getGameAssociated().getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.getGameAssociated().getGameState());
        assertEquals(TurnState.PLACE, gameController.getGameAssociated().getTurnState());

        gameController.removeClient("Player 3");
        gameController.removeClient("Player 4");

        sleep(1500);

        // check that game should have been stopped, and there is no winner
        assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertTrue(gameController.getGameAssociated().getWinnerPlayers().isEmpty());
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWinByPoints() {
        setUp(1); // fixed seed to know cards that players have in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        allPlayersPlacedInitialCard();

        gameController.removeClient("Player 3");
        gameController.removeClient("Player 4");

        // Player 1 turn
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 1);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 0);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        gameController.placeCard("Player 1", "resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        assertFalse(gameController.getGameAssociated().getFinalCondition());
        gameController.placeCard("Player 1", "gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP);
        // Player 1 reached 20 points: final condition should be true, but not in final round
        assertTrue(gameController.getGameAssociated().getFinalCondition());
        assertFalse(gameController.getGameAssociated().isFinalRound());
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);

        assertTrue(gameController.getGameAssociated().getFinalCondition());
        assertFalse(gameController.getGameAssociated().isFinalRound());
        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        // now it should be the final round:
        assertTrue(gameController.getGameAssociated().getFinalCondition());
        assertTrue(gameController.getGameAssociated().isFinalRound());

        dummyTurn("Player 1", PlayableCardType.RESOURCE);

        dummyTurn("Player 2", PlayableCardType.RESOURCE);

        // game should end and declare Player 1 the winner
        assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals("Player 1", gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());
    }

    @Test
    public void testWinByEmptyTable() {
        setUp(1); // fixed seed to know cards that players have in hand

        allClientsAdded();

        allPlayersChooseColor();

        allPlayersChoosePrivateGoal();

        allPlayersPlacedInitialCard();

        // draw all cards from resource deck
        for(int i=0; i<30; i++) {
            dummyTurn(gameController.getGameAssociated().getActivePlayer().getName(), PlayableCardType.RESOURCE);
        }

        // draw all cards from gold deck
        for(int i=0; i<34; i++) {
            dummyTurn(gameController.getGameAssociated().getActivePlayer().getName(), PlayableCardType.GOLD);
        }

        // remove client Player 4 to test final condition and start of final round separately
        gameController.removeClient("Player 4");

        assertFalse(gameController.getGameAssociated().getFinalCondition());
        // draw the four cards on table: when last card is picked, we reach a final condition

        dummyPlace("Player 1");
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);
        assertNull(gameController.getGameAssociated().getResourceCardsOnTable()[0]);

        dummyPlace("Player 2");
        gameController.drawCardFromTable("Player 2", PlayableCardType.RESOURCE, 1);
        assertNull(gameController.getGameAssociated().getResourceCardsOnTable()[1]);

        dummyPlace("Player 3");
        gameController.drawCardFromTable("Player 3", PlayableCardType.GOLD, 0);
        assertNull(gameController.getGameAssociated().getGoldCardsOnTable()[0]);

        // last turn where there is a card to draw
        assertFalse(gameController.getGameAssociated().getFinalCondition());

        dummyPlace("Player 1");
        gameController.drawCardFromTable("Player 1", PlayableCardType.GOLD, 1);
        assertNull(gameController.getGameAssociated().getGoldCardsOnTable()[1]);

        assertTrue(gameController.getGameAssociated().getFinalCondition());
        assertFalse(gameController.getGameAssociated().isFinalRound());

        // now turns will not have a draw phase, only the place phase

        dummyPlace("Player 2");

        assertTrue(gameController.getGameAssociated().getFinalCondition());
        assertFalse(gameController.getGameAssociated().isFinalRound());

        dummyPlace("Player 3");

        // final round should start

        assertTrue(gameController.getGameAssociated().getFinalCondition());
        assertTrue(gameController.getGameAssociated().isFinalRound());

        dummyPlace("Player 1");
        dummyPlace("Player 2");
        dummyPlace("Player 3");

        // game should end and declare Player 1 the winner
        assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals("Player 1", gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());

    }

    public void dummyTurn(String nickname, PlayableCardType cardType) {
        Player p = gameController.getGameAssociated().getPlayerByName(nickname);
        dummyPlace(nickname);
        gameController.drawCardFromDeck(p.getName(), cardType);
    }

    public void dummyPlace(String nickname) {
        Player p = gameController.getGameAssociated().getPlayerByName(nickname);
        gameController.placeCard(
                p.getName(),
                p.getStation().getCardsInStation().getFirst().getCardCode(),
                p.getStation().getLastPlaced().get().getCardCode(),
                Direction.UP_RIGHT,
                CardOrientation.DOWN
        );
    }

    public boolean isCardPlacedInStation(String nickname, String card) {
        Player p = gameController.getGameAssociated().getPlayerByName(nickname);
        List<String> cardCodesInStation = Arrays.stream(
                p.getStation().getCardCodeSchema())
                .flatMap(Stream::of).map(o -> o.orElse(""))
                .toList();

        return cardCodesInStation.contains(card);

    }

    public void gameStatus() {
        System.out.println("Table:");
        System.out.println("Resource cards:");
        Arrays.stream(gameController.getGameAssociated().getResourceCardsOnTable())
                .forEach(System.out::println);

        System.out.println("Gold cards:");
        Arrays.stream(gameController.getGameAssociated().getGoldCardsOnTable())
                .forEach(System.out::println);

        System.out.println("Public Goal cards:");
        Arrays.stream(gameController.getGameAssociated().getPublicGoalCardsOnTable())
                .forEach(System.out::println);

        System.out.println();
        for(Player p : gameController.getConnectedClients().stream().map(n -> gameController.getGameAssociated().getPlayerByName(n)).toList()) {
            System.out.println(p.getName()+":");

            System.out.println("Points:");
            System.out.println(p.getStation().getNumPoints());

            System.out.println("Visible symbols:");
            System.out.println(p.getStation().getVisibleSymbolsInStation());

            System.out.println("Initial card:");
            System.out.println(p.getStation().getInitialCard());

            System.out.println("Possible Goal cards:");
            System.out.println(p.getStation().getPrivateGoalCardInStation(0));
            System.out.println(p.getStation().getPrivateGoalCardInStation(1));

            System.out.println("Private Goal card:");
            System.out.println(p.getStation().getPrivateGoalCard());

            System.out.println("Hand:");
            p.getStation().getCardsInStation()
                    .forEach(System.out::println);

            System.out.println();
        }
    }
}
