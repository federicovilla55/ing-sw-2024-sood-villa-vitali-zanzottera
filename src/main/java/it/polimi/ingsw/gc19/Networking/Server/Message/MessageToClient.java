package it.polimi.ingsw.gc19.Networking.Server.Message;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class represents a generic message that server
 * can send to client. Every message has its own priority.
 */
public abstract class MessageToClient implements Remote, Serializable{

    private List<String> header;
    private MessagePriorityLevel messagePriorityLevel = MessagePriorityLevel.LOW;

    /**
     * This method sets the header to the message. Header is the list
     * of players to which the message will be sent
     * @param header the list of players to whom message will be sent
     * @return the current updated {@link MessageToClient}
     */
    public MessageToClient setHeader(List<String> header){
        this.header = header;
        return this;
    }

    /**
     * This method sets the header to the message. Header is the list
     * of players to which the message will be sent
     * @param header is the player to whom message wil be sent
     * @return
     */
    public MessageToClient setHeader(String header){
        this.header = (header==null) ? null : new ArrayList<>(List.of(header));
        if(this.header != null) this.header.sort(null);
        return this;
    }

    /**
     * Setter for priority level of message
     * @param priority the {@link MessagePriorityLevel} associated to the message
     * @return the updated {@link MessageToClient} object
     */
    public MessageToClient setPriorityLevel(MessagePriorityLevel priority){
        this.messagePriorityLevel = priority;
        return this;
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
