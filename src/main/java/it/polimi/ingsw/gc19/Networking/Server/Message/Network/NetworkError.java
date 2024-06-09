package it.polimi.ingsw.gc19.Networking.Server.Message.Network;

/**
 * This enum represents all possible error concerning
 * network (such as reconnection not possible, or reconnection not necessary).
 */
public enum NetworkError {

    CLIENT_NOT_REGISTERED_TO_SERVER,
    CLIENT_ALREADY_CONNECTED_TO_SERVER,
    COULD_NOT_RECONNECT

}