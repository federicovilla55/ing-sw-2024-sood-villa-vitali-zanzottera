package it.polimi.ingsw.gc19.Networking.Server.Message;

import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This abstract class represents a generic message that server
 * can send to client. Every message has its own priority.
 */
public abstract class MessageToClient implements Serializable{

    /**
     * The header of the message. Message will be sent to all (and no other) players
     * in the header
     */
    private List<String> header;

    /**
     * The priority level of the message
     */
    private MessagePriorityLevel messagePriorityLevel = MessagePriorityLevel.LOW;

    /**
     * This method sets the header to the message. Header is the list
     * of players to which the message will be sent
     * @param header the list of players to whom message will be sent
     * @return the current updated {@link MessageToClient}
     */
    public MessageToClient setHeader(List<String> header){
        List<String> sorted = null;
        if(header != null) {
            sorted = new ArrayList<>(header);
            Collections.sort(sorted);
        }
        this.header = sorted;
        return this;
    }

    /**
     * This method sets the header to the message. Header is the list
     * of players to which the message will be sent
     * @param header is the player to whom message wil be sent
     * @return {@link MessageToClient} updated
     */
    public MessageToClient setHeader(String header){
        this.setHeader((header==null) ? null : new ArrayList<>(List.of(header)));
        return this;
    }

    /**
     * Setter for priority level of message
     * @param priority the {@link MessagePriorityLevel} associated to the message
     */
    public void setPriorityLevel(MessagePriorityLevel priority){
        this.messagePriorityLevel = priority;
    }

    /**
     * Getter for priority level of the message
     * @return the priority level of the message
     */
    public MessagePriorityLevel getMessagePriorityLevel(){
        return this.messagePriorityLevel;
    }

    /**
     * Getter for message header
     * @return the header of the message
     */
    public List<String> getHeader(){
        return this.header;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    public abstract void accept(MessageToClientVisitor visitor);

    /**
     * This method compares an {@link Object} with a {@link MessageToClient}
     * @param o the {@link Object} to compare
     * @return true if and only if the two objects are equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<? extends MessageToClient> thisClass = getClass();
        Class<?> otherClass = o.getClass();
        if (thisClass != otherClass) return false;

        for(Method method : thisClass.getMethods()) {
            if (method.getName().startsWith("get")) {
                try {
                    Object thisResult = method.invoke(this);
                    Object otherResult = method.invoke(o);
                    if (thisResult != null && !thisResult.equals(otherResult) || (thisResult == null && otherResult != null)) {
                        return false;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    /**
     * Overriding of {@link Object#hashCode()}
     * @return the hash code of the message
     */
    @Override
    public int hashCode() {
        int result = 17; // Start with a non-zero prime number as the initial hash value
        Class<? extends MessageToClient> thisClass = getClass();

        for (Method method : thisClass.getMethods()) {
            if (method.getName().startsWith("get")) {
                try {
                    Object value = method.invoke(this);
                    result = 31 * result + (value != null ? value.hashCode() : 0);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    /**
     * Overriding of {@link Object#toString()}.
     * @return a string version of the message
     */
    @Override
    public String toString() {
        String s = "";
        Class<? extends MessageToClient> thisClass = getClass();

        for (Method method : thisClass.getMethods()) {
            if (method.getName().startsWith("get")) {
                try {
                    s = s.concat(method.getName() + ":\n" + method.invoke(this) + "\n\n");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return s;
    }

}