package it.polimi.ingsw.gc19.Model.Deck;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class represents a generic deck that contains
 * cards of type <code>cardType</code>. It lets other classes picking
 * cards and shuffling.
 * @param <cardType> the type of cards ({@link PlayableCard} or {@link GoalCard})
 *                  that the deck will contain
 */
public class Deck<cardType extends Card>{

    /**
     * Cards currently inside deck
     */
    private final ArrayList<cardType> cardsInDeck;

    /**
     * This constructor creates a deck of cardType cards
     * @param cardsInDeck stream of cards to put in deck
     */
    public Deck(Stream<cardType> cardsInDeck) {
        this.cardsInDeck = cardsInDeck.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * This method shuffles deck
     */
    public void shuffleDeck(Random random){
        Collections.shuffle(this.cardsInDeck, random);
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
     * This method tells whether this deck is empty
     * @return true if and only if the deck is empty
     */
    public boolean isEmpty(){
        return this.cardsInDeck.isEmpty();
    }

    /**
     * Getter for seed of next card on top of deck. If the
     * returned {@code Optional<cardType>} is empty, then there are no more cards
     * inside the deck
     * @return the next seed on top of deck if at least one card is present,
     * otherwise and empty {@link Optional}
     */
    public Optional<cardType> getNextCard(){
        if(!this.cardsInDeck.isEmpty()){
            return Optional.of(this.cardsInDeck.getFirst());
        }
        return Optional.empty();
    }

}