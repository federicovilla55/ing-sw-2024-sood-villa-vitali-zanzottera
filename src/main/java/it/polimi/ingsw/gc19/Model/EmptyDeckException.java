package it.polimi.ingsw.gc19.Model;

public class EmptyDeckException extends Exception{

    public EmptyDeckException(String message){

        super(message);

    }

    public EmptyDeckException(Exception ex){

        super(ex);

    }

    public EmptyDeckException() {

    }
}
