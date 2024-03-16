package it.polimi.ingsw.gc19.Model.Deck;

import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Player.NameAlreadyInUseException;
import it.polimi.ingsw.gc19.Model.Player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.MalformedParametersException;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {
    private Game game;

    @BeforeEach
    public void setUp() throws IOException {
        game = new Game(4);
    }

    @Test
    public void testInitialization() {
        // Test game initialization
        assertNotNull(game);
        assertEquals(4, game.getNumPlayers());
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

        // 40 Gold Cards, 40 Resource Cards, 16 Goal Cards, 6 Initial Cards
        assertEquals(102, game.getInfoAllCards().size());

        try {
            game.pickCardFromDeck(PlayableCardType.INITIAL);
            assert(false);
        } catch (MalformedParametersException ignored) {

        }catch (EmptyDeckException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testPlayerCreation() throws NameAlreadyInUseException {
        assertEquals(0, game.getNumJoinedPlayer(), "Initial player count should be 0");
        game.createNewPlayer("Player1", null);
        assertEquals(1, game.getNumJoinedPlayer(), "Player count should be 1 after creating a player");
        assertThrows(NameAlreadyInUseException.class, () -> game.createNewPlayer("Player1", null), "Should throw NameAlreadyInUseException");
    }

}