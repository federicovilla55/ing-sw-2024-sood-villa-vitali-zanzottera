package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Deck.Deck;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Player.Player;
import it.polimi.ingsw.gc19.Model.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Model.Station.InvalidCardException;
import it.polimi.ingsw.gc19.Model.Station.InvalidPositionException;
import it.polimi.ingsw.gc19.Model.Station.Station;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class GameTest{
    Game gameToTest = new Game(4);

    GameTest() throws IOException {
    }

    @Test
    void deckTests() throws IOException, NoSuchFieldException, IllegalAccessException, EmptyDeckException, InvalidPositionException, InvalidAnchorException, InvalidCardException {
        Field fieldInitialDeck = gameToTest.getClass().getDeclaredField("initialDeck");
        fieldInitialDeck.setAccessible(true);
        Deck<PlayableCard> initialDeck = (Deck<PlayableCard>) fieldInitialDeck.get(gameToTest);

        assertEquals(initialDeck.getInitialLenOfDeck(), 6);

        Field fieldResourceDeck = gameToTest.getClass().getDeclaredField("resourceDeck");
        fieldResourceDeck.setAccessible(true);
        Deck<PlayableCard> resourceDeck = (Deck<PlayableCard>) fieldResourceDeck.get(gameToTest);

        assertEquals(resourceDeck.getInitialLenOfDeck(), 40);

        Field fieldGoldDeck = gameToTest.getClass().getDeclaredField("goldDeck");
        fieldGoldDeck.setAccessible(true);
        Deck<PlayableCard> goldDeck = (Deck<PlayableCard>) fieldGoldDeck.get(gameToTest);

        Field fieldGoalDeck = gameToTest.getClass().getDeclaredField("goalDeck");
        fieldGoalDeck.setAccessible(true);
        Deck<GoalCard> goalDeck = (Deck<GoalCard>) fieldGoalDeck.get(gameToTest);

        /*assertEquals(goldDeck.getInitialLenOfDeck(), 40);

        for(int i = 0; i < 6; i++) initialDeck.pickACard();

        assertThrows(EmptyDeckException.class, initialDeck::pickACard);

        for(int i = 0; i < 38; i++) resourceDeck.pickACard();

        assertThrows(EmptyDeckException.class, resourceDeck::pickACard);

        for(int i = 0; i < 38; i++) goldDeck.pickACard();

        assertThrows(EmptyDeckException.class, goldDeck::pickACard);*/

        Station station = new Station();

        PlayableCard i = gameToTest.getPlayableCardFromCode("initial_01").get();

        PlayableCard p1 = gameToTest.getPlayableCardFromCode("resource_01").get();

        PlayableCard p2 = gameToTest.getPlayableCardFromCode("resource_01").get();

        PlayableCard p3 = gameToTest.getPlayableCardFromCode("resource_01").get();

        PlayableCard p4 = gameToTest.getPlayableCardFromCode("resource_01").get();

        PlayableCard p5 = gameToTest.getPlayableCardFromCode("resource_01").get();

        PlayableCard p6 = gameToTest.getPlayableCardFromCode("resource_01").get();

        System.out.println(p1.getCardDescription());

        System.out.println(p2.getCardDescription());

        System.out.println(p3.getCardDescription());

        station.updateCardsInHand(p2);

        station.updateCardsInHand(p3);

        station.placeInitialCard(i);

        //assertTrue(station.cardIsPlaceable(p1, p2, Direction.UP_RIGHT));

        station.placeCard(i, p1, Direction.UP_RIGHT);

        station.placeCard(p1, p2, Direction.UP_RIGHT);

        station.placeCard(p2, p3, Direction.UP_RIGHT);

        station.placeCard(p3, p4, Direction.UP_RIGHT);

        station.placeCard(p4, p5, Direction.UP_RIGHT);

        station.placeCard(p5, p6, Direction.UP_RIGHT);

        //GoalCard goalCard = gameToTest.getGoalCardFromCode("goal_01").get();

        //station.setPrivateGoalCard(goalCard);

        //System.out.println(goalCard.getCardDescription());

        //station.updatePoints(goalCard);

        //System.out.println(station.getNumPoints());

        //assertFalse(station.cardIsPlaceable(p1, p3, Direction.UP_RIGHT));

        //System.out.println(station.getVisibleSymbolsInStation());

        //assertEquals(station.getNumPoints(), 1);

    }

    @Test
    void cardDeckTest(){

    }

}