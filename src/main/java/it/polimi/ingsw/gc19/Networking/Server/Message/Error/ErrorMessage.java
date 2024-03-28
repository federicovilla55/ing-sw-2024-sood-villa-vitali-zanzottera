package it.polimi.ingsw.gc19.Networking.Server.Message.Error;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

/**
 * This is an abstract class used to notify errors to clients.
 * reporting errors.
 */
public abstract class ErrorMessage implements MessageToClient{

    private final String errorDescription;

    ErrorMessage(String errorDescription){
        this.errorDescription = errorDescription;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

}
