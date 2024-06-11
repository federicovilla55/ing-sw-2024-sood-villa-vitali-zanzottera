package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Model.MessageFactory;
import it.polimi.ingsw.gc19.Model.Card.CardNotFoundException;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private Game game;
    private GameController gameController;

    @BeforeEach
    public void setUp() throws IOException {
        game = new Game(4, "game");
        game.setMessageFactory(new MessageFactory());
    }

    /**
     * Tests the initialization of the game and the decks. Tests the deck sizes too.
     */
    @Test
    public void testDecks() {
        // Test Station - 1 game initialization
        assertNotNull(game);
        assertEquals(4, game.getNumPlayers());

        //try to select non-valid deck
        assertThrows(IllegalArgumentException.class, () -> {game.getDeckFromType(PlayableCardType.INITIAL);});

        int deckSize = 0;
        try {
            while (true) {
                game.pickCardFromDeck(PlayableCardType.GOLD);
                deckSize++;
            }
        } catch (EmptyDeckException e) {
            assertEquals(38, deckSize, "Gold deck size should be 38");
        }
        deckSize = 0;
        try {
            while (true) {
                game.pickCardFromDeck(PlayableCardType.RESOURCE);
                deckSize++;
            }
        } catch (EmptyDeckException e) {
            assertEquals(38, deckSize, "Resource deck size should be 38");
        }

        assertThrows(IllegalArgumentException.class, () -> game.pickCardFromDeck(PlayableCardType.INITIAL));

        // 40 Gold Cards, 40 Resource Cards, 16 Goal Cards, 6 Initial Cards
        assertEquals(102, game.getInfoAllCards().size());
    }

    /**
     * Tests the behavior of the cards positioned on the table and what happens when they are picked.
     * @throws CardNotFoundException if a card is not found.
     */
    @Test
    public void testCardsTable() throws CardNotFoundException {
        assertThrows(IllegalArgumentException.class, () -> game.pickCardFromTable(PlayableCardType.INITIAL, 0));
        assertThrows(IllegalArgumentException.class, () -> game.pickCardFromTable(PlayableCardType.INITIAL, 1));
        assertThrows(IllegalArgumentException.class, () -> game.pickCardFromTable(PlayableCardType.GOLD, 2));
        assertThrows(IllegalArgumentException.class, () -> game.pickCardFromTable(PlayableCardType.RESOURCE, 2));

        for(int i = 0; i < 39; i++){
            System.out.println(i);
            game.pickCardFromTable(PlayableCardType.GOLD, 1);
            game.pickCardFromTable(PlayableCardType.RESOURCE, 1);
        }
        assertThrows(CardNotFoundException.class, () -> game.pickCardFromTable(PlayableCardType.GOLD, 1));
        assertThrows(CardNotFoundException.class, () -> game.pickCardFromTable(PlayableCardType.RESOURCE, 1));
        game.pickCardFromTable(PlayableCardType.GOLD, 0);
        game.pickCardFromTable(PlayableCardType.RESOURCE, 0);
        assertThrows(CardNotFoundException.class, () -> game.pickCardFromTable(PlayableCardType.GOLD, 0));
        assertThrows(CardNotFoundException.class, () -> game.pickCardFromTable(PlayableCardType.RESOURCE, 0));
    }

    /**
     * Tests the creation of players.
     * @throws NameAlreadyInUseException if a player with the same name already exists.
     */
    @Test
    public void testPlayerCreation() throws NameAlreadyInUseException {
        assertEquals(0, game.getNumJoinedPlayer(), "Initial player count should be 0");
        game.createNewPlayer("Player1");
        assertEquals(1, game.getNumJoinedPlayer(), "Player count should be 1 after creating a player");
        assertThrows(NameAlreadyInUseException.class, () -> game.createNewPlayer("Player1"), "Should throw NameAlreadyInUseException");
    }

    /**
     * Tests the removal of players from the game.
     *
     * @throws NameAlreadyInUseException if a player with the same name already exists.
     * @throws PlayerNotFoundException   if the player to be removed is not found.
     */
    @Test
    public void testPlayerRemoval() throws NameAlreadyInUseException, PlayerNotFoundException {
        assertEquals(0, game.getNumJoinedPlayer(), "Initial player count should be 0");
        game.createNewPlayer("Player1");
        assertEquals(1, game.getNumJoinedPlayer(), "Player count should be 1 after creating a player");
        game.createNewPlayer("Player2");
        game.createNewPlayer("Player3");
        assertThrows(NameAlreadyInUseException.class, () -> game.createNewPlayer("Player1"), "Should throw NameAlreadyInUseException");
        assertEquals(3, game.getNumJoinedPlayer(), "Player count should be 3");
        game.removePlayer(game.getPlayerByName("Player3"));
        assertEquals(2, game.getNumJoinedPlayer());
        game.removePlayer(game.getPlayerByName("Player2"));
        assertEquals(1, game.getNumJoinedPlayer());
        game.removePlayer(game.getPlayerByName("Player1"));
        assertEquals(0, game.getNumJoinedPlayer());
        assertThrows(PlayerNotFoundException.class, () -> game.getPlayerByName("Player1"));
    }

    /**
     * Tests the retrieval of information from all cards and checks for exception in case
     * a card is not found.
     * All the card codes follow the pattern "cardType_cardNumber", such as: "gold_01".
     * @throws NullPointerException if the specified card code is not found.
     */
    @Test
    public void testGetCardFromCode() throws NullPointerException{
        ArrayList<String> allCardInfos = game.getInfoAllCards();
        for(int i = 1; i <= 40; i++){
            String goldCode = "gold_" + String.format("%02d", i);
            assertTrue(game.getPlayableCardFromCode(goldCode).isPresent(), "Gold card " + goldCode + " should be present");
            String description = game.getInfoCard(goldCode);
            assertEquals(description, game.getPlayableCardFromCode(goldCode).get().getCardDescription(), "Description of " + goldCode + " differs.");
        }

        for(int i = 1; i <= 40; i++){
            String resourceCode = "resource_" + String.format("%02d", i);
            assertTrue(game.getPlayableCardFromCode(resourceCode).isPresent(), "Resource card " + resourceCode + " should be present");
            String description = game.getInfoCard(resourceCode);
            assertEquals(description, game.getPlayableCardFromCode(resourceCode).get().getCardDescription(), "Description of " + resourceCode + " differs.");
        }

        for(int i = 1; i <= 6; i++){
            String initialCode = "initial_" + String.format("%02d", i);
            assertTrue(game.getPlayableCardFromCode(initialCode).isPresent(), "Initial card " + initialCode + " should be present");
            String description = game.getInfoCard(initialCode);
            assertEquals(description, game.getPlayableCardFromCode(initialCode).get().getCardDescription(), "Description of " + initialCode + " differs.");
        }

        for(int i = 1; i <= 16; i++){
            String goalCode = "goal_" + String.format("%02d", i);
            assertTrue(game.getGoalCardFromCode(goalCode).isPresent(), "Gold card " + goalCode + " should be present");
            String description = game.getInfoCard(goalCode);
            assertEquals(description, game.getGoalCardFromCode(goalCode).get().getCardDescription(), "Description of " + goalCode + " differs.");
        }

        assertThrows(NullPointerException.class, () -> game.getPlayableCardFromCode("invalid_code").isPresent());
    }

    /**
     * Tests the determination of the first and the active player after the game starts.
     * If someone tries to start the game (and therefore set the first and the active player)
     * before the previously specified number of players have joined the game, nothing
     * will happen.
     * @throws NameAlreadyInUseException if a player with the same name already exists.
     */
    @Test
    public void testFirstActivePlayer() throws NameAlreadyInUseException {
        assertNull(game.getFirstPlayer());
        assertNull(game.getActivePlayer());
        game.startGame();
        // Nothing assigned yet as number of players
        // is less than the numPlayers specified
        assertNull(game.getFirstPlayer());
        assertNull(game.getActivePlayer());
        game.createNewPlayer("Player1");
        game.createNewPlayer("Player2");
        game.createNewPlayer("Player3");
        game.startGame();
        assertNull(game.getFirstPlayer());
        assertNull(game.getActivePlayer());
        // Now the number of players is equal to numPlayers
        // so the first and active player will be set
        game.createNewPlayer("Player4");
        game.startGame();
        assertNotNull(game.getFirstPlayer());
        assertNotNull(game.getActivePlayer());
    }

    /**
     * Tests the determination of the next player.
     * @throws NameAlreadyInUseException if a player with the same name already exists.
     */
    @Test
    public void testNextPlayer() throws NameAlreadyInUseException {

        game.createNewPlayer("Player1");
        game.createNewPlayer("Player2");
        game.createNewPlayer("Player3");
        game.createNewPlayer("Player4");

        // @todo: Change this test after a logic to change the active player is created.
        while(true){
            game.startGame();
            Player p = game.getActivePlayer();
            if (p.getName().equals("Player1")){
                assertEquals("Player2", game.getNextPlayer().getName());
                break;
            }
        }
        while(true){
            game.startGame();
            Player p = game.getActivePlayer();
            if (p.getName().equals("Player2")){
                assertEquals("Player3", game.getNextPlayer().getName());
                break;
            }
        }
        while(true){
            game.startGame();
            Player p = game.getActivePlayer();
            if (p.getName().equals("Player3")){
                assertEquals("Player4", game.getNextPlayer().getName());
                break;
            }
        }
        while(true){
            game.startGame();
            Player p = game.getActivePlayer();
            if (p.getName().equals("Player4")){
                assertEquals("Player1", game.getNextPlayer().getName());
                break;
            }
        }
    }
}