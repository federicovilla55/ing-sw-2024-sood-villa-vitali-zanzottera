package it.polimi.ingsw.gc19.Networking.Server.Message.Network;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class NetworkHandlingErrorMessage extends MessageToClient {

    private final NetworkError networkError;
    private final String description;

    public NetworkHandlingErrorMessage(NetworkError networkError, String description){
        this.networkError = networkError;
        this.description = description;
    }

    public NetworkError getError() {
        return this.networkError;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {

    }

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof NetworkHandlingErrorMessage){
            return ((NetworkHandlingErrorMessage) o).networkError == this.networkError;
        }
        return false;
    }

}
