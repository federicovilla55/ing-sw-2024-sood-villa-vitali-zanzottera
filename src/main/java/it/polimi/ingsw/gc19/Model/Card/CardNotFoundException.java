package it.polimi.ingsw.gc19.Model.Card;

public class CardNotFoundException extends Exception{

    public CardNotFoundException(String message){

        super(message);

    }

    public CardNotFoundException(Exception ex){

        super(ex);

    }

}