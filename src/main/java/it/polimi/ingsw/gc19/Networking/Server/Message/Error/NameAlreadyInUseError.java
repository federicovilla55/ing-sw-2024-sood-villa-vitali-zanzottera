package it.polimi.ingsw.gc19.Networking.Server.Message.Error;

public class NameAlreadyInUseError extends ErrorMessage{

    public NameAlreadyInUseError(String errorDescription){
        super(errorDescription);
    }

}
