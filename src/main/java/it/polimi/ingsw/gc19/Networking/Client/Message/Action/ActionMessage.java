package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This abstract class represents a generic message sent from
 * client to server concerning player's action (e.g.placing a card or
 * picking a card from deck)
 */
public abstract class ActionMessage extends MessageToServer{

    protected ActionMessage(String nickname) {
        super(nickname);
    }

}
