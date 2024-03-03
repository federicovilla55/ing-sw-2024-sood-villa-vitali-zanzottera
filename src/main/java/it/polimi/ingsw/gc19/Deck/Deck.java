package it.polimi.ingsw.gc19.Deck;

import it.polimi.ingsw.gc19.Card.*;

import java.util.ArrayList;
import java.util.Random;

public class Deck<cardType extends Card>{

    private ArrayList<cardType> cardsInDeck;
    private  int initialLenOfDeck;

    public Deck(String filename){

        /*
            TODO: parse JSON file in order to retrieve Deck information. "filename" is usefully because it allows constructor to build deck at the beginning of the game or when the server crash.
                  Note that we can use cardType.class in .readValue(..., cardType.class)
         */

    }

    public int getInitialLenOfDeck(){

        return this.initialLenOfDeck;

    }

    public cardType pickACard() throws EmptyDeckException{

        if(this.cardsInDeck.isEmpty()){

            throw new EmptyDeckException("You can't pick a card. Deck is empty!");

        }

        return this.cardsInDeck.remove(new Random().nextInt(cardsInDeck.size()));

    }

    public boolean isEmpty(){

        return this.cardsInDeck.isEmpty();

    }

    public boolean cardIsInDeck(cardType cardToSearch){

        return this.cardsInDeck.contains(cardToSearch);

    }

    public int numberOfCardInDeck(){

        return this.cardsInDeck.size();

    }

}
