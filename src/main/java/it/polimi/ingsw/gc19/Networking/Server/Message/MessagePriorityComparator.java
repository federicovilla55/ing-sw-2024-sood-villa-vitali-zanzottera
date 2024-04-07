package it.polimi.ingsw.gc19.Networking.Server.Message;

import java.util.Comparator;

public class MessagePriorityComparator implements Comparator<MessageToClient>{
    @Override
    public int compare(MessageToClient o1, MessageToClient o2) {
        return MessagePriorityLevel.comparePriority().compare(o1.getMessagePriorityLevel(), o2.getMessagePriorityLevel());
    }

}
