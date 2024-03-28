package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
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

    @BeforeEach
    public void setUp() {
        setUp(1);
    }
    public void setUp(long randomSeed) {
        try {
            gameController = new GameController(new Game(4, randomSeed), 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAddClient() {
        gameController.addClient("Player 1");
        assertEquals(1,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(1,gameController.connectedClients.size());
        assertTrue(gameController.connectedClients.contains("Player 1"));
        gameController.addClient("Player 2");
        assertEquals(2,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(2,gameController.connectedClients.size());
        assertTrue(gameController.connectedClients.contains("Player 2"));
        gameController.addClient("Player 2");
        assertEquals(2,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(2,gameController.connectedClients.size());
        assertTrue(gameController.connectedClients.contains("Player 2"));
        gameController.addClient("Player 3");
        assertEquals(3,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(3,gameController.connectedClients.size());
        assertTrue(gameController.connectedClients.contains("Player 3"));
        gameController.addClient("Player 4");
        assertEquals(4,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(4,gameController.connectedClients.size());
        assertTrue(gameController.connectedClients.contains("Player 4"));
        gameController.addClient("Player 5");
        assertEquals(4,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(4,gameController.connectedClients.size());
        assertFalse(gameController.connectedClients.contains("Player 5"));
    }

    @Test
    public void testRemoveClient() {
        gameController.removeClient("Player 1");
        assertEquals(0,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(0,gameController.connectedClients.size());
        assertFalse(gameController.connectedClients.contains("Player 1"));
        gameController.addClient("Player 1");
        gameController.removeClient("Player 1");
        assertEquals(1,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(0,gameController.connectedClients.size());
        assertFalse(gameController.connectedClients.contains("Player 1"));
        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");
        gameController.removeClient("Player 2");
        assertEquals(4,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(3,gameController.connectedClients.size());
        assertFalse(gameController.connectedClients.contains("Player 2"));
        gameController.removeClient("Player 3");
        assertEquals(4,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(2,gameController.connectedClients.size());
        assertFalse(gameController.connectedClients.contains("Player 3"));
        gameController.removeClient("Player 1");
        assertEquals(4,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(1,gameController.connectedClients.size());
        assertFalse(gameController.connectedClients.contains("Player 1"));
        gameController.removeClient("Player 4");
        assertEquals(4,gameController.gameAssociated.getNumJoinedPlayer());
        assertEquals(0,gameController.connectedClients.size());
        assertFalse(gameController.connectedClients.contains("Player 4"));
    }

    @Test
    public void testChooseColor() {
        gameController.addClient("Player 1");

        gameController.addClient("Player 2");

        gameController.addClient("Player 3");

        gameController.addClient("Player 4");

        for (String nick : gameController.connectedClients) {
            assertNull(gameController.gameAssociated.getPlayerByName(nick).getColor());
        }

        gameController.chooseColor("Player 1", Color.RED);
        assertEquals(Color.RED, gameController.gameAssociated.getPlayerByName("Player 1").getColor());

        gameController.chooseColor("Player 1", Color.BLUE);
        assertEquals(Color.RED, gameController.gameAssociated.getPlayerByName("Player 1").getColor());

        gameController.chooseColor("Player 2", Color.RED);
        assertNull(gameController.gameAssociated.getPlayerByName("Player 2").getColor());

        gameController.chooseColor("Player 2", Color.BLUE);
        assertEquals(Color.BLUE, gameController.gameAssociated.getPlayerByName("Player 2").getColor());

        gameController.chooseColor("Player 3", Color.YELLOW);
        assertEquals(Color.YELLOW, gameController.gameAssociated.getPlayerByName("Player 3").getColor());

        gameController.chooseColor("Player 4", Color.GREEN);
        assertEquals(Color.GREEN, gameController.gameAssociated.getPlayerByName("Player 4").getColor());

    }

    @Test
    public void testChoosePrivateGoal() {
        gameController.addClient("Player 1");

        assertNull(gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getPrivateGoalCard());

        gameController.choosePrivateGoal("Player 1", 0);

        GoalCard goal0 = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getPrivateGoalCard();

        assertNotNull(goal0);

        gameController.choosePrivateGoal("Player 1", 1);

        assertEquals(goal0, gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getPrivateGoalCard());

        gameController.choosePrivateGoal("Player 2", 1);
    }

    @Test
    public void testPlaceInitialCard() {
        gameController.addClient("Player 1");

        assertFalse(gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getInitialCardIsPlaced());

        gameController.placeInitialCard("Player 1", CardOrientation.UP);

        assertTrue(gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getInitialCardIsPlaced());

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);

        gameController.placeInitialCard("Player 2", CardOrientation.UP);
    }

    @Test
    public void testStartGame() {
        setUp(1); // fixed seed to have first player to Player 1

        assertFalse(gameController.gameAssociated.allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.gameAssociated.getGameState());

        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        assertFalse(gameController.gameAssociated.allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.gameAssociated.getGameState());

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        assertFalse(gameController.gameAssociated.allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.gameAssociated.getGameState());

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

        assertFalse(gameController.gameAssociated.allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.gameAssociated.getGameState());

        gameController.placeInitialCard("Player 1", CardOrientation.UP);
        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);

        assertFalse(gameController.gameAssociated.allPlayersChooseInitialGoalColor());
        assertEquals(GameState.SETUP, gameController.gameAssociated.getGameState());

        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        assertTrue(gameController.gameAssociated.allPlayersChooseInitialGoalColor());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals("Player 1", gameController.gameAssociated.getFirstPlayer().getName());
    }

    @Test
    public void testPlaceCard() {
        setUp(1); //fixed seed to have first player to Player 1 and its initial card to initial_05 and resource_23 in hand

        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

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

        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.DRAW, gameController.gameAssociated.getTurnState());
        assertEquals("Player 1", gameController.gameAssociated.getActivePlayer().getName());
    }

    @Test
    public void testDrawCardFromDeck() {
        setUp(1); //fixed seed to have first player to Player 1 and its initial card to initial_05 and resource_23 in hand

        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);

        // try to draw a card while in setup phase
        List<PlayableCard> cardsBefore = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());

        gameController.drawCardFromDeck("Player 1", PlayableCardType.RESOURCE);

        List<PlayableCard> cardsAfter = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        // wrong player
        cardsBefore = gameController.gameAssociated.getPlayerByName("Player 2").getPlayerStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());
        gameController.drawCardFromDeck("Player 2", PlayableCardType.RESOURCE);
        cardsAfter = gameController.gameAssociated.getPlayerByName("Player 2").getPlayerStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // wrong card type (INITIAL)
        cardsBefore = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        assertThrows(MalformedParametersException.class, () -> gameController.drawCardFromDeck("Player 1", PlayableCardType.INITIAL));
        cardsAfter = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // correct placing
        cardsBefore = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        gameController.drawCardFromDeck("Player 1", PlayableCardType.RESOURCE);
        cardsAfter = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertTrue(cardsAfter.containsAll(cardsBefore));
        assertEquals(PlayableCardType.RESOURCE, cardsAfter.getLast().getCardType());

        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());
        assertEquals("Player 2", gameController.gameAssociated.getActivePlayer().getName());
    }

    @Test
    public void testDrawCardFromTable() {
        setUp(1); //fixed seed to have first player to Player 1 and its initial card to initial_05 and resource_23 in hand

        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);

        // try to draw a card while in setup phase
        List<PlayableCard> cardsBefore = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());

        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);

        List<PlayableCard> cardsAfter = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        gameController.placeCard("Player 1", "resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.UP);

        // wrong player
        cardsBefore = gameController.gameAssociated.getPlayerByName("Player 2").getPlayerStation().getCardsInStation();
        assertEquals(3, cardsBefore.size());
        gameController.drawCardFromTable("Player 2", PlayableCardType.RESOURCE,0);
        cardsAfter = gameController.gameAssociated.getPlayerByName("Player 2").getPlayerStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // wrong card type (INITIAL)
        cardsBefore = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        assertThrows(MalformedParametersException.class, () -> gameController.drawCardFromTable("Player 1", PlayableCardType.INITIAL, 0));
        cardsAfter = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // malformed index
        cardsBefore = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        assertThrows(IndexOutOfBoundsException.class, () -> gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 2));
        cardsAfter = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertTrue(cardsBefore.containsAll(cardsAfter) && cardsAfter.containsAll(cardsBefore));

        // correct placing
        cardsBefore = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertEquals(2, cardsBefore.size());
        PlayableCard cardToDraw = gameController.gameAssociated.getResourceCardsOnTable()[0];
        gameController.drawCardFromTable("Player 1", PlayableCardType.RESOURCE, 0);
        cardsAfter = gameController.gameAssociated.getPlayerByName("Player 1").getPlayerStation().getCardsInStation();
        assertTrue(cardsAfter.containsAll(cardsBefore));
        assertTrue(cardsAfter.contains(cardToDraw));
        assertNotEquals(cardToDraw, gameController.gameAssociated.getResourceCardsOnTable()[0]);

        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());
        assertEquals("Player 2", gameController.gameAssociated.getActivePlayer().getName());
    }

    @Test
    public void testTurnSkipOfDisconnectedPlayers() {
        setUp(1); // fixed seed to know cards that players have in hand

        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        // test that at Player 1 disconnection turn will go to the next player
        assertEquals("Player 1", gameController.gameAssociated.getActivePlayer().getName());
        gameController.removeClient("Player 1");
        assertEquals("Player 2", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());

        // Player 2 turn
        gameController.placeCard("Player 2", "resource_37", "initial_01", Direction.UP_RIGHT, CardOrientation.UP);
        // disconnection of Player 3, so turn will go to Player 4 directly
        gameController.removeClient("Player 3");
        gameController.drawCardFromDeck("Player 2", PlayableCardType.RESOURCE);
        assertEquals("Player 4", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());

        // Player 4 turn
        gameController.placeCard("Player 4", "resource_20", "initial_03", Direction.UP_RIGHT, CardOrientation.UP);
        gameController.drawCardFromDeck("Player 4", PlayableCardType.RESOURCE);
        // turn should go to Player 2
        assertEquals("Player 2", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());
    }

    @Test
    public void testGamePauseAndUnpause() {
        setUp(1); // fixed seed to know cards that players have in hand

        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        gameController.removeClient("Player 1");
        // Player 2 turn
        gameController.removeClient("Player 2");
        // Player 3 turn
        gameController.placeCard("Player 3", "resource_09", "initial_06", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameController.removeClient("Player 4");
        //test game is in pause state and active player is player 3
        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.DRAW, gameController.gameAssociated.getTurnState());

        // test that at disconnection of all players game is in pause
        gameController.removeClient("Player 3");
        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.DRAW, gameController.gameAssociated.getTurnState());

        // Player 3 and then Player 4 connect to the game, gameState should be PLAYING and turnState should stay DRAW
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");
        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.DRAW, gameController.gameAssociated.getTurnState());

        // disconnection of Player 4 and then Player 3: game in pause again
        gameController.removeClient("Player 4");
        gameController.removeClient("Player 3");
        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.DRAW, gameController.gameAssociated.getTurnState());

        // Connection of Player 1, active player should change to him and turnState should become PLACE
        gameController.addClient("Player 1");
        assertEquals("Player 1", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());

        // Connection of Player 2, game should return to PLAYING state
        gameController.addClient("Player 2");
        assertEquals("Player 1", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());
    }

    @Test
    public void testGamePauseAndEndOneClient() {
        setUp(1); // fixed seed to know cards that players have in hand

        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

        gameController.placeInitialCard("Player 1", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        // Player 1 turn
        gameController.removeClient("Player 1");
        // Player 2 turn
        gameController.removeClient("Player 2");
        // Player 3 turn
        gameController.removeClient("Player 4");

        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PAUSE, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        gameController.addClient("Player 4");

        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // check that game has not been stopped
        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
        assertEquals(TurnState.PLACE, gameController.gameAssociated.getTurnState());

        gameController.removeClient("Player 4");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // check that game should have been stopped, and Player 3 is the winner
        assertEquals("Player 3", gameController.gameAssociated.getActivePlayer().getName());
        assertEquals(GameState.END, gameController.gameAssociated.getGameState());
        assertEquals("Player 3", gameController.gameAssociated.getWinnerPlayers().getFirst().getName());
        assertEquals(1, gameController.gameAssociated.getWinnerPlayers().size());
    }

    @Test
    public void testGamePauseAndEndZeroClients() {

    }

    public boolean isCardPlacedInStation(String nickname, String card) {
        Player p = gameController.gameAssociated.getPlayerByName(nickname);
        List<String> cardCodesInStation = Arrays.stream(
                p.getPlayerStation().getCardCodeSchema())
                .flatMap(Stream::of).map(o -> o.orElse(""))
                .toList();

        return cardCodesInStation.contains(card);

    }

    public void gameStatus() {
        System.out.println("Table:");
        System.out.println("Resource cards:");
        Arrays.stream(gameController.gameAssociated.getResourceCardsOnTable())
                .forEach(System.out::println);

        System.out.println("Gold cards:");
        Arrays.stream(gameController.gameAssociated.getGoldCardsOnTable())
                .forEach(System.out::println);

        System.out.println("Public Goal cards:");
        Arrays.stream(gameController.gameAssociated.getPublicGoalCardsOnTable())
                .forEach(System.out::println);

        System.out.println();
        for(Player p : gameController.connectedClients.stream().map(n -> gameController.gameAssociated.getPlayerByName(n)).toList()) {
            System.out.println(p.getName()+":");

            System.out.println("Visible symbols:");
            System.out.println(p.getPlayerStation().getVisibleSymbolsInStation());

            System.out.println("Initial card:");
            System.out.println(p.getPlayerStation().getInitialCard());

            System.out.println("Possible Goal cards:");
            System.out.println(p.getPlayerStation().getPrivateGoalCardInStation(0));
            System.out.println(p.getPlayerStation().getPrivateGoalCardInStation(1));

            System.out.println("Private Goal card:");
            System.out.println(p.getPlayerStation().getPrivateGoalCard());

            System.out.println("Hand:");
            p.getPlayerStation().getCardsInStation()
                    .forEach(System.out::println);

            System.out.println();
        }
    }

    @Deprecated
    public void testFinder() {
        long seed = -1;
        Game game;
        do {
            seed++;
            try {
                game = new Game(4,seed);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            game.createNewPlayer("Player 1");
            game.createNewPlayer("Player 2");
            game.createNewPlayer("Player 3");
            game.createNewPlayer("Player 4");
            game.startGame();

        }while(
                !game.getPlayerByName("Player 1")
                        .getPlayerStation()
                        .getInitialCard()
                        .getCardCode().equals("initial_05")
                        ||
                        !game.getFirstPlayer().getName().equals("Player 1") ||
                        !Arrays.stream(game.getPublicGoalCardsOnTable()).map(GoalCard::getCardCode).toList().contains("goal_01") ||
                        !Arrays.stream(game.getPublicGoalCardsOnTable()).map(GoalCard::getCardCode).toList().contains("goal_13"));

        System.out.println(seed);
    }
}
