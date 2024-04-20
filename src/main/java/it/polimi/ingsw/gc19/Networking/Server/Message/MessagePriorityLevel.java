package it.polimi.ingsw.gc19.Networking.Server.Message;

import java.util.Comparator;

public enum MessagePriorityLevel{
    LOW(0), HIGH(1);

    private final int priorityLevel;

    private MessagePriorityLevel(int messagePriority){
        this.priorityLevel = messagePriority;
    }

    public static Comparator<MessagePriorityLevel> comparePriority(){
        return Comparator.comparing(v -> v.priorityLevel);
    }

}
