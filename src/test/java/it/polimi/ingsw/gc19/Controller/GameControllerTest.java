package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Game.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    private GameController gameController;

    @BeforeEach
    public void setUp() throws IOException {
        gameController = new GameController(new Game(4));
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
    }

    @Test
    public void testPlaceCard() {
        gameController.addClient("Player 1");
        gameController.addClient("Player 2");
        gameController.addClient("Player 3");
        gameController.addClient("Player 4");

        gameController.placeCard("Player 1", "resource_01", "initial_01", Direction.UP_LEFT, CardOrientation.UP);

        gameController.chooseColor("Player 1", Color.RED);
        gameController.chooseColor("Player 2", Color.GREEN);
        gameController.chooseColor("Player 3", Color.BLUE);
        gameController.chooseColor("Player 4", Color.YELLOW);

        gameController.choosePrivateGoal("Player 1", 0);
        gameController.choosePrivateGoal("Player 2", 1);
        gameController.choosePrivateGoal("Player 3", 0);
        gameController.choosePrivateGoal("Player 4", 1);

        gameController.placeInitialCard("Player 1", CardOrientation.UP);
        gameController.placeInitialCard("Player 2", CardOrientation.DOWN);
        gameController.placeInitialCard("Player 3", CardOrientation.UP);
        gameController.placeInitialCard("Player 4", CardOrientation.DOWN);

        assertTrue(gameController.gameAssociated.allPlayersChooseInitialGoalColor());
        assertEquals(GameState.PLAYING, gameController.gameAssociated.getGameState());
    }
}
