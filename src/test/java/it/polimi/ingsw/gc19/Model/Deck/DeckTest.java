package it.polimi.ingsw.gc19.Model.Deck;

import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Enums.CornerPosition;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeckTest {
    @Test
    void getArray() throws IOException {
        /*decks locations*/
        String initialDeckPath = "src/main/resources/it/polimi/ingsw/gc19/decks/initial_deck.json";
        String resourceDeckPath = "src/main/resources/it/polimi/ingsw/gc19/decks/resource_deck.json";
        String goldDeckPath = "src/main/resources/it/polimi/ingsw/gc19/decks/gold_deck.json";
        String goalDeckPath = "src/main/resources/it/polimi/ingsw/gc19/decks/goal_deck.json";

        /*load initial cards from file*/
        Deck<PlayableCard> initialDeck = new Deck<>(initialDeckPath);

        ArrayList<PlayableCard> initialArray = initialDeck.getArray();

        /*load resource cards from file*/
        Deck<PlayableCard> resourceDeck = new Deck<>(resourceDeckPath);

        ArrayList<PlayableCard> resourceArray = resourceDeck.getArray();

        /*load gold cards from file*/
        Deck<PlayableCard> goldDeck = new Deck<>(goldDeckPath);

        ArrayList<PlayableCard> goldArray = goldDeck.getArray();

        /*load goal cards from file*/
        Deck<GoalCard> goalDeck = new Deck<>(goalDeckPath);

        ArrayList<GoalCard> goalArray = goalDeck.getArray();

        /*initialDeck test*/
        assertEquals(
                6,
                initialDeck.getInitialLenOfDeck(),
                "Initial Deck does not have 6 cards"
        );
        assertEquals(
                CardOrientation.DOWN,
                initialArray.getFirst().getCardOrientation(),
                "Initialized card is not oriented DOWN"
        );
        assertEquals(
                Symbol.INSECT,
                initialArray.getFirst().getCorner(CornerPosition.DOWN_LEFT),
                "Wrong symbol in DOWN_LEFT back corner for card " + initialArray.getFirst().getCardCode()
        );
        initialArray.getFirst().swapCard();
        assertEquals(
                Symbol.VEGETABLE,
                initialArray.getFirst().getCorner(CornerPosition.UP_RIGHT),
                "Wrong symbol in UP_RIGHT front corner back for card " + initialArray.getFirst().getCardCode()
        );
    }
}