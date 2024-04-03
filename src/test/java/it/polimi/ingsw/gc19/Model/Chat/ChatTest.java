package it.polimi.ingsw.gc19.Model.Chat;

import it.polimi.ingsw.gc19.Controller.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import it.polimi.ingsw.gc19.Model.Chat.Chat;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTest {
    private Chat chat;
    @BeforeEach
    public void setUpTest(){
        chat = new Chat();
        chat.setMessageFactory(new MessageFactory());
    }

    public String updateMessagesString(String messageString, Message m){
        return m.toString()+"\n"+messageString;
    }
    @Test
    public void addMessages(){
        Message m1 = new Message("Hello World!", "Player1", "Player2", "Player3", "Player4");
        Message m2 = new Message("This message is not for everyone.", "Player1", "Player2", "Player3");
        Message m3 = new Message("Sample Message One to One", "Player1", "Player2");
        Message m4 = new Message("Another Sample Message One to One", "Player2", "Player1");

        // This string contains the messages sent up to now
        // Following the pattern:
        // [ senderPlayer - senderTime ] -> messageReceivers : messageContent
        String messagesString = "";

        assertEquals(chat.getNumOfMessages(), 0);
        assertEquals(chat.getMessagesSentByPlayer("Player1"), "");
        chat.pushMessage(m1);
        assertEquals(chat.getNumOfMessages(), 1);
        messagesString = updateMessagesString(messagesString, m1);
        assertEquals(chat.toString(), messagesString);
        assertEquals(chat.getMessagesSentByPlayer("Player1"), messagesString);
        assertEquals(m1.getReceivers(), new ArrayList<>(List.of("Player2", "Player3", "Player4")));
        assertEquals(m1.getSenderPlayer(), "Player1");

        chat.pushMessage(m2);
        assertEquals(chat.getNumOfMessages(), 2);
        messagesString = updateMessagesString(messagesString, m2);
        assertEquals(chat.toString(), messagesString);
        assertEquals(chat.getMessagesSentByPlayer("Player1"), messagesString);
        assertEquals(m2.getReceivers(), new ArrayList<>(List.of("Player2", "Player3")));
        assertEquals(m2.getSenderPlayer(), "Player1");

        chat.pushMessage(m3);
        assertEquals(chat.getNumOfMessages(), 3);
        messagesString = updateMessagesString(messagesString, m3);
        assertEquals(chat.toString(), messagesString);
        assertEquals(chat.getMessagesSentByPlayer("Player1"), messagesString);
        assertEquals(m3.getReceivers(), new ArrayList<>(List.of("Player2")));
        assertEquals(m3.getSenderPlayer(), "Player1");

        assertEquals(chat.getMessagesSentByPlayer("Player2"), "");
        chat.pushMessage(m4);
        assertEquals(chat.getNumOfMessages(), 4);
        messagesString = updateMessagesString(messagesString, m4);
        assertEquals(chat.toString(), messagesString);
        assertNotEquals(chat.getMessagesSentByPlayer("Player1"), messagesString);
        assertEquals(chat.getMessagesSentByPlayer("Player2"), m4.toString()+"\n");
        assertEquals(m4.getSenderPlayer(), "Player2");
        assertEquals(m4.getReceivers(), new ArrayList<>(List.of("Player1")));

        assertEquals(chat.getMessagesSentByPlayer("Player3"), "");
        assertEquals(chat.getMessagesSentByPlayer("Player4"), "");
    }
}
