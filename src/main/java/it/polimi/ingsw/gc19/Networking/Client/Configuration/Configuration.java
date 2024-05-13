package it.polimi.ingsw.gc19.Networking.Client.Configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.gc19.Networking.Client.ClientFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMIFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCPFactory;

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

    public enum ConnectionType{
        RMI(new ClientRMIFactory()),
        TCP(new ClientTCPFactory());

        private final ClientFactory clientFactory;

        ConnectionType(ClientFactory clientFactory) {
            this.clientFactory = clientFactory;
        }

        public ClientFactory getClientFactory() {
            return clientFactory;
        }
    }

    private final String nick;
    private final String token;
    private final String timestamp;
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