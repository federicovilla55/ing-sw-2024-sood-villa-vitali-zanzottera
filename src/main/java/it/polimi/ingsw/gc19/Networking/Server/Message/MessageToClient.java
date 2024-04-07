package it.polimi.ingsw.gc19.Networking.Server.Message;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;

/**
 *  This is an empty interface representing serializable messages
 *  sent from Server to Client
 */
public abstract class MessageToClient implements Remote, Serializable{

    private List<String> header;
    private MessagePriorityLevel messagePriorityLevel;

    public MessageToClient setHeader(List<String> header){
        this.header = header;
        this.messagePriorityLevel = MessagePriorityLevel.LOW;
        return this;
    }

    public MessageToClient setHeader(String header){
        this.header = new ArrayList<>(List.of(header));
        return this;
    }

    public MessageToClient setPriorityLevel(MessagePriorityLevel priority){
        this.messagePriorityLevel = priority;
        return this;
    }

    public MessagePriorityLevel getMessagePriorityLevel(){
        return this.messagePriorityLevel;
    }

    public List<String> getHeader(){
        return this.header;
    }

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
