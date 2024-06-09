package it.polimi.ingsw.gc19.Networking.Client.Configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.gc19.Networking.Client.ClientFactory.ClientFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientFactory.ClientRMIFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientFactory.ClientTCPFactory;

import java.util.Date;

/**
 * This class represents the last valid configuration of
 * the client interface. It contains infos about:
 * <ul>
 *     <li>connection type <code>RMI</code> or <code>TCP</code></li>
 *     <li>nickname</li>
 *     <li>token</li>
 *     <li>a {@link String} representing the timestamp</li>
 * </ul>
 */
public class Configuration {

    /**
     * Enum of connection type (RMI or TCP only)
     */
    public enum ConnectionType{
        RMI(new ClientRMIFactory()),
        TCP(new ClientTCPFactory());

        /**
         * Client factory associated to connection type
         */
        private final ClientFactory clientFactory;

        ConnectionType(ClientFactory clientFactory) {
            this.clientFactory = clientFactory;
        }

        /**
         * Getter for {@link ClientFactory} of the enum value
         * @return the relative {@link ClientFactory} for the enum value
         */
        public ClientFactory getClientFactory() {
            return clientFactory;
        }
    }

    /**
     * Nickname chosen by the user when he registered to server
     */
    private final String nick;

    /**
     * Token assigned by server to user when he registered
     */
    private final String token;

    /**
     * Timestamp of client registration
     */
    private final String timestamp;

    /**
     * Connection type chosen by user when he registered to server
     */
    private final  ConnectionType connectionType;

    @JsonCreator
    public Configuration(
            @JsonProperty("nickname")
            String nick,
            @JsonProperty("token")
            String token,
            @JsonProperty("timestamp")
            String timestamp,
            @JsonProperty("connection_type")
            ConnectionType connectionType) {
        this.nick = nick;
        this.token = token;
        this.timestamp = timestamp;
        this.connectionType = connectionType;
    }

    public Configuration(String nick, String token, ConnectionType connectionType){
        this.nick = nick;
        this.token = token;
        this.timestamp = new Date().toString();
        this.connectionType = connectionType;
    }

    /**
     * Getter for nickname stored in configuration
     * @return the nickname stored in the configuration
     */
    public String getNick() {
        return nick;
    }

    /**
     * Getter for token stored in configuration
     * @return the nickname stored in the configuration
     */
    public String getToken() {
        return token;
    }

    /**
     * Getter for timestamp stored in configuration
     * @return the timestamp stored in configuration
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for connection type stored in configuration
     * @return the connection type stored in configuration
     */
    public ConnectionType getConnectionType() {
        return connectionType;
    }

}