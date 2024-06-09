package it.polimi.ingsw.gc19.Model.Card;

/**
 * This exception is raised when no card has been found
 * given the code
 */
public class CardNotFoundException extends Exception{

    public CardNotFoundException(String message){
        super(message);
    }

}