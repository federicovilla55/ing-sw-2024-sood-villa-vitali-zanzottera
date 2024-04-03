package it.polimi.ingsw.gc19.Model.Chat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Message{
    private final String message;
    private final LocalDate sendTime;
    private final String senderPlayer;
    private final ArrayList<String> receivers;

    public Message(String message, String senderPlayer, ArrayList<String> receiversPlayer){
        this.message = message;
        this.sendTime = LocalDate.now();
        this.senderPlayer = senderPlayer;
        this.receivers = receiversPlayer;
    }

    public Message(String message, String senderPlayer, String ... receiversPlayer){
        this.message = message;
        this.sendTime = LocalDate.now();
        this.senderPlayer = senderPlayer;
        this.receivers = new ArrayList<>(List.of(receiversPlayer));
    }

    public String getMessage(){
        return this.message;
    }

    public String getSenderPlayer(){
        return this.senderPlayer;
    }

    public LocalDate getSendTime() {
        return this.sendTime;
    }

    @Override
    public String toString(){
        return "[" + this.senderPlayer + " - " + this.sendTime + "] -> " + this.getReceivers() + " : " + this.message;
    }

    public ArrayList<String> getReceivers(){
        return this.receivers;
    }

}
