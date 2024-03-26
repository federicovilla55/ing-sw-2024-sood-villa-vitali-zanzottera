package it.polimi.ingsw.gc19.Model.Chat;

import it.polimi.ingsw.gc19.Model.Game.Player;

import java.util.ArrayList;

public class Chat{
    private final ArrayList<Message> messagesInChat;

    public Chat(){
        this.messagesInChat = new ArrayList<>();
    }

    public void pushMessage(Message messageToPush){
        this.messagesInChat.addFirst(messageToPush);
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

    public int getNunOfMessages(){
        return this.messagesInChat.size();
    }

}
