package it.polimi.ingsw.gc19.Model.Deck;

import it.polimi.ingsw.gc19.Model.Card.Card;

import java.util.*;
import java.util.stream.Stream;

public class Deck<cardType extends Card>{

    private final ArrayList<cardType> cardsInDeck;
    private final int initialLenOfDeck;

    /**
     * This constructor creates a deck of cardType cards
     * @param cardsInDeck stream of cards to put in deck
     */
    public Deck(Stream<cardType> cardsInDeck) {
        this.cardsInDeck = cardsInDeck.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        this.initialLenOfDeck = this.cardsInDeck.size();
    }

    /**
     * This method shuffles deck
     */
    public void shuffleDeck(Random random){
        Collections.shuffle(this.cardsInDeck, random);
    }

    /**
     * This method return the initial len of deck
     * @return the initial length of deck
     */
    public int getInitialLenOfDeck(){
        return this.initialLenOfDeck;
    }

    /**
     * This method pick a random card from deck
     * @return cardType card randomly chosen from the deck
     */
    public cardType pickACard() throws EmptyDeckException {
        if(this.isEmpty()){
            throw new EmptyDeckException("You can't pick a card. Deck is empty!");
        }
        return this.cardsInDeck.removeFirst();
    }

    /**
     * This method insert a card in the deck
     * @param card card to insert in deck
     */
    public void insertCard(cardType card){
        this.cardsInDeck.addFirst(card);
    }

    /**
     * This method tells whether this deck is empty
     * @return true if and only if the deck is empty
     */
    public boolean isEmpty(){
        return this.cardsInDeck.isEmpty();
    }

    /**
     * This method checks if this deck contains a specific card
     * @param cardToSearch the card to search for in deck
     * @return true if the card is inside this deck
     */
    public boolean cardIsInDeck(cardType cardToSearch){
        return this.cardsInDeck.contains(cardToSearch);
    }

    /**
     * This method return the current length of deck
     * @return the current length of this deck
     */
    public int numberOfCardInDeck(){
        return this.cardsInDeck.size();
    }

}
