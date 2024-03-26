package it.polimi.ingsw.gc19.Model.Chat;

import org.junit.jupiter.api.BeforeEach;
import it.polimi.ingsw.gc19.Model.Chat.Chat;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTest {
    private Chat chat;
    @BeforeEach
    public void setUpTest() {
        chat = new Chat();
    }

    public String updateMessagesString(String messageString, Message m){
        return m.toString()+"\n"+messageString;
    }
    @Test
    public void addMessages(){
        OneToMoreMessage m1 = new OneToMoreMessage("Hello World!", "Player1", "Player2", "Player3", "Player4");
        OneToMoreMessage m2 = new OneToMoreMessage("This message is not for everyone.", "Player1", "Player2", "Player3");
        OneToOneMessage m3 = new OneToOneMessage("Sample Message One to One", "Player1", "Player2");
        OneToOneMessage m4 = new OneToOneMessage("Another Sample Message One to One", "Player2", "Player1");

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
        assertEquals(m1.getReceivers(), "Player2, Player3, Player4, ");
        assertEquals(m1.getSenderPlayer(), "Player1");

        chat.pushMessage(m2);
        assertEquals(chat.getNumOfMessages(), 2);
        messagesString = updateMessagesString(messagesString, m2);
        assertEquals(chat.toString(), messagesString);
        assertEquals(chat.getMessagesSentByPlayer("Player1"), messagesString);
        assertEquals(m2.getReceivers(), "Player2, Player3, ");
        assertEquals(m2.getSenderPlayer(), "Player1");

        chat.pushMessage(m3);
        assertEquals(chat.getNumOfMessages(), 3);
        messagesString = updateMessagesString(messagesString, m3);
        assertEquals(chat.toString(), messagesString);
        assertEquals(chat.getMessagesSentByPlayer("Player1"), messagesString);
        assertEquals(m3.getReceivers(), "Player2");
        assertEquals(m3.getSenderPlayer(), "Player1");

        assertEquals(chat.getMessagesSentByPlayer("Player2"), "");
        chat.pushMessage(m4);
        assertEquals(chat.getNumOfMessages(), 4);
        messagesString = updateMessagesString(messagesString, m4);
        assertEquals(chat.toString(), messagesString);
        assertNotEquals(chat.getMessagesSentByPlayer("Player1"), messagesString);
        assertEquals(chat.getMessagesSentByPlayer("Player2"), m4.toString()+"\n");
        assertEquals(m4.getSenderPlayer(), "Player2");
        assertEquals(m4.getReceivers(), "Player1");

        assertEquals(chat.getMessagesSentByPlayer("Player3"), "");
        assertEquals(chat.getMessagesSentByPlayer("Player4"), "");
    }
}
