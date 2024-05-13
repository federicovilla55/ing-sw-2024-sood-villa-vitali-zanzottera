package it.polimi.ingsw.gc19.Networking.Client;

/**
 * This interface must be implemented by all clients that
 * need to be configurable (e.g. need to store in a file some
 * configuration infos such as nickname, token...)
 */
public interface ConfigurableClient {

    /**
     * This method is used to configure a client with nickname and token
     * @param nick the nickname of the player
     * @param token the token associated to client
     */
    void configure(String nick, String token);

}