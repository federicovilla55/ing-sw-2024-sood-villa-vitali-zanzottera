package it.polimi.ingsw.gc19.Networking.Server.Message;

import java.util.Comparator;

/**
 * This enum is used to represent different priority level of messages.
 * Typically, priorities are ordered.
 */
public enum MessagePriorityLevel{
    LOW(0), HIGH(1);

    private final int priorityLevel;

    MessagePriorityLevel(int messagePriority){
        this.priorityLevel = messagePriority;
    }

    /**
     * This method returns a comparator for {@link MessagePriorityLevel}.
     * @return a {@code Comparator<MessagePriorityLevels>}
     */
    public static Comparator<MessagePriorityLevel> comparePriority(){
        return Comparator.comparing(v -> v.priorityLevel);
    }

}
