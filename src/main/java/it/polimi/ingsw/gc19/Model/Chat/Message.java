package it.polimi.ingsw.gc19.Model.Chat;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class Message implements Serializable {
    private final String message;
    private final LocalDate sendTime;
    private final String senderPlayer;

    protected Message(String message, String senderPlayer){
        this.message = message;
        this.sendTime = LocalDate.now();
        this.senderPlayer = senderPlayer;
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

    public abstract String getReceivers();

}
