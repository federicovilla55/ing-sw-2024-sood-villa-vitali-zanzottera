package it.polimi.ingsw.gc19.Model.Player;

public class PlayerNotFoundException extends Exception{

    public PlayerNotFoundException(String message){

        super(message);

    }

    public PlayerNotFoundException(Exception ex){

        super(ex);

    }

}