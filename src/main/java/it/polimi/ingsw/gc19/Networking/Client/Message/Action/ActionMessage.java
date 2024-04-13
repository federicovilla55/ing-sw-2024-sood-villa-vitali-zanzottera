package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public abstract class ActionMessage extends MessageToServer{

    protected ActionMessage(String nickname) {
        super(nickname);
    }

}
