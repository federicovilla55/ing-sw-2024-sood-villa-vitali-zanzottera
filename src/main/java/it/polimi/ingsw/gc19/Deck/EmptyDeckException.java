package it.polimi.ingsw.gc19.Deck;

import it.polimi.ingsw.gc19.Card.Card;

public class EmptyDeckException extends Exception{

    public EmptyDeckException(String message){

        super(message);

    }

    public EmptyDeckException(Exception ex){

        super(ex);

    }

}
