package it.polimi.ingsw.gc19.Model.Deck;

/**
 * This exception is raised when there area no more
 * cards inside the deck.
 */
public class EmptyDeckException extends RuntimeException{

    public EmptyDeckException(String message){
        super(message);
    }

}
