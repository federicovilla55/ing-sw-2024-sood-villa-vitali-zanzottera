package it.polimi.ingsw.gc19.Model;

/**
 * This is an abstract class used for implementing Publisher - Subscriber
 * design pattern.
 * All classes extending {@link Publisher} can send messages to their connected
 * {@link MessageFactory}.
 */
public abstract class Publisher{

    /**
     * Connected {@link MessageFactory}
     */
    private MessageFactory messageFactory;

    /**
     * Setter for {@link MessageFactory}.
     * @param messageFactory the {@link MessageFactory} to set in class {@link Publisher}.
     */
    public void setMessageFactory(MessageFactory messageFactory){
        this.messageFactory = messageFactory;
    }

    /**
     * Getter for {@link MessageFactory}.
     * @return the {@link MessageFactory} owned by object.
     */
    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

}
