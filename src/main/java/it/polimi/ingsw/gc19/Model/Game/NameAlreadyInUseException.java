package it.polimi.ingsw.gc19.Model.Game;

/**
 * This exception is thrown if and only if there is another
 * player with the same name
 */
public class NameAlreadyInUseException extends RuntimeException{

    public NameAlreadyInUseException(String message){
        super(message);
    }

}