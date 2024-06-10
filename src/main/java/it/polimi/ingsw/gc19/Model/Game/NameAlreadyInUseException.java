package it.polimi.ingsw.gc19.Model.Game;

/**
 * This exception is thrown when the name of a player has been already chosen
 */
public class NameAlreadyInUseException extends RuntimeException{

    public NameAlreadyInUseException(String message){

        super(message);

    }

    public NameAlreadyInUseException(Exception ex){

        super(ex);

    }

}