package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class GameHandlingError extends MessageToClient{

    private final Error errorType;
    private final String description;

    public GameHandlingError(Error errorType, String description) {
        this.errorType = errorType;
        this.description = description;
        this.setPriorityLevel(MessagePriorityLevel.HIGH);
    }

    public Error getErrorType() {
        return errorType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
