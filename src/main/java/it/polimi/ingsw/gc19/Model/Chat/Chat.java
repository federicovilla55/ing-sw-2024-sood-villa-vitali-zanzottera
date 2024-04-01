package it.polimi.ingsw.gc19.Model.Chat;

import it.polimi.ingsw.gc19.Model.Publisher;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat extends Publisher{
    private final ArrayList<Message> messagesInChat;

    public Chat(){
        this.messagesInChat = new ArrayList<>();
    }

    public void pushMessage(Message messageToPush){
        this.messagesInChat.addFirst(messageToPush);
        this.getMessageFactory().sendMessageToPlayer(messageToPush.getReceivers(),
                                                     new NotifyChatMessage(messageToPush.getReceivers(), messageToPush.getSenderPlayer(), messageToPush.getMessage()));
    }

    @Override
    public String toString(){
        StringBuilder chatString = new StringBuilder();
        for (Message message : this.messagesInChat){
            chatString.append(message.toString()).append("\n");
        }
        return chatString.toString();
    }

    public String getMessagesSentByPlayer(String player){
        StringBuilder chatString = new StringBuilder();
        for (Message message : this.messagesInChat){
            if(message.getSenderPlayer().equals(player)) {
                chatString.append(message).append("\n");
            }
        }
        return chatString.toString();
    }

    public int getNumOfMessages(){
        return this.messagesInChat.size();
    }

}
