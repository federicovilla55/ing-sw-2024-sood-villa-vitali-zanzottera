package it.polimi.ingsw.gc19.Model.Deck;

public class EmptyDeckException extends RuntimeException{

    public EmptyDeckException(String message){
        super(message);
    }

    public EmptyDeckException(Exception ex){
        super(ex);
    }

}
