package it.polimi.ingsw.gc19.Networking.Server.Message;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;

/**
 *  This is an empty interface representing serializable messages
 *  sent from Server to Client
 */
public abstract class MessageToClient implements Remote, Serializable{

    private List<String> header;

    public MessageToClient setHeader(List<String> header){
        this.header = header;
        return this;
    }

    public MessageToClient setHeader(String header){
        this.header = new ArrayList<>(List.of(header));
        return this;
    }

    public List<String> getHeader(){
        return this.header;
    }

}
