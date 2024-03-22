package it.polimi.ingsw.gc19.Model.Game;

public class NameAlreadyInUseException extends Exception{

    public NameAlreadyInUseException(String message){

        super(message);

    }

    public NameAlreadyInUseException(Exception ex){

        super(ex);

    }

}