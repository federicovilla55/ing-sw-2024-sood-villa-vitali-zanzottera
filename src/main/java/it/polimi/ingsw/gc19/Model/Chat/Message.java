package it.polimi.ingsw.gc19.Model.Chat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents a generic chat message
 */
public class Message{

    /**
     * The content of the message
     */
    private final String message;

    /**
     * The timestamp in which message has arrived to server and has been
     * pushed inside chat
     */
    private final String sendTime;

    /**
     * Nickname of the player who sent the message
     */
    private final String senderPlayer;

    /**
     * Users to which this message is headed
     */
    private final ArrayList<String> receivers;

    public Message(String message, String senderPlayer, ArrayList<String> receiversPlayer){
        this.message = message;
        this.sendTime = new Date().toString();
        this.senderPlayer = senderPlayer;
        this.receivers = receiversPlayer;
    }

    public Message(String message, String senderPlayer, String ... receiversPlayer){
        this.message = message;
        this.sendTime = new Date().toString();
        this.senderPlayer = senderPlayer;
        this.receivers = new ArrayList<>(List.of(receiversPlayer));
    }

    /**
     * Getter for message content
     * @return the message content
     */
    public String getMessage(){
        return this.message;
    }

    /**
     * Getter for sender player
     * @return the nickname of the sender player
     */
    public String getSenderPlayer(){
        return this.senderPlayer;
    }

    /**
     * Getter for send time
     * @return the timestamp in which message has been pushed
     * inside chat
     */
    public String getSendTime() {
        return this.sendTime;
    }

    /**
     * Builds a {@link String} version of the message
     * @return the {@link String} version of the message
     */
    @Override
    public String toString(){
        return "[" + this.senderPlayer + " - " + this.sendTime + "] -> " + this.getReceivers() + " : " + this.message;
    }

    /**
     * Getter for receivers of the message
     * @return the receivers of the message
     */
    public ArrayList<String> getReceivers(){
        return this.receivers;
    }

}