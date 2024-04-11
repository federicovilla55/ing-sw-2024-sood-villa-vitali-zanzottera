package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This abstract class represents a generic configuration message
 * (e.g. cards and decks on table, players station after reconnection)
 */
public abstract class ConfigurationMessage extends MessageToClient{

}
