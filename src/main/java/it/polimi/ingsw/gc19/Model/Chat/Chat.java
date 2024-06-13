package it.polimi.ingsw.gc19.Model.Chat;

import it.polimi.ingsw.gc19.Model.Publisher;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Model.MessageFactory;

import java.util.ArrayList;

/**
 * This class is used for AF of chat. It manages chat of the game
 * it is associated.
 */
public class Chat extends Publisher{

    /**
     * Messages written in chat and saved on server
     */
    private final ArrayList<Message> messagesInChat;

    public Chat(){
        this.messagesInChat = new ArrayList<>();
    }

    /**
     * Pushes a new {@link Message} inside {@link #messagesInChat}.
     * Signal to {@link MessageFactory} to
     * notify connected clients
     * @param messageToPush the new {@link Message} to push in chat
     */
    public void pushMessage(Message messageToPush){
        this.messagesInChat.addFirst(messageToPush);
        this.getMessageFactory().sendMessageToPlayer(messageToPush.getReceivers(),
                                                     new NotifyChatMessage(messageToPush.getSenderPlayer(), messageToPush.getMessage()).setHeader(messageToPush.getReceivers()));
    }

    /**
     * Build a {@link String} version of the chat
     * @return a {@link String} version of the chat content
     */
    @Override
    public String toString(){
        StringBuilder chatString = new StringBuilder();
        for (Message message : this.messagesInChat){
            chatString.append(message.toString()).append("\n");
        }
        return chatString.toString();
    }

    /**
     * Extract from {@link #messagesInChat} all messages written
     * by the specified player.
     * @param player the player to search
     * @return the concatenation of all {@link Message} written by {@param player}
     */
    public String getMessagesSentByPlayer(String player){
        StringBuilder chatString = new StringBuilder();
        for (Message message : this.messagesInChat){
            if(message.getSenderPlayer().equals(player)) {
                chatString.append(message).append("\n");
            }
        }
        return chatString.toString();
    }

    /**
     * Getter for number of messages written in chat (e.g. contained in {@link #messagesInChat})
     * @return the number of messages written in chat
     */
    public int getNumOfMessages(){
        return this.messagesInChat.size();
    }

}