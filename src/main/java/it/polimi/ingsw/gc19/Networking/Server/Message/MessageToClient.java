package it.polimi.ingsw.gc19.Networking.Server.Message;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 *  This is an empty interface representing serializable messages
 *  sent from Server to Client
 */
public abstract class MessageToClient implements Remote, Serializable{

    private ArrayList<String> header;

    public MessageToClient setHeader(ArrayList<String> header){
        this.header = header;
        return this;
    }

    public MessageToClient setHeader(String header){
        this.header = new ArrayList<>(List.of(header));
        return this;
    }

    public ArrayList<String> getHeader(){
        return this.header;
    }

}

/**
 *     protected MessageToClient(ArrayList<String> header){
 *         this.header = header;
 *     }
 *
 *     public MessageToClient(String header){
 *         this.header = new ArrayList<>(List.of(header));
 *     }
 *
 *     protected MessageToClient(){
 *
 *     }
 */
