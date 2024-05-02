package it.polimi.ingsw.gc19.Networking.Client.Configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Configuration {

    public enum ConnectionType{
        RMI, TCP;
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

    public String getNick() {
        return nick;
    }

    public String getToken() {
        return token;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

}
