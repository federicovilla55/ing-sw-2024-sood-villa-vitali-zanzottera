package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class MessageFactoryTest {

    private MessageFactory testMessageFactory;

    private NamedObserverStubMessageToClient observer1;
    private NamedObserverStubMessageToClient observer2;
    private NamedObserverStubMessageToClient observer3;
    private NamedObserverStubMessageToClient observer4;
    private NamedObserverStubMessageToClient observer5;



    private AnonymousObserverStubMessageToClient logger;
    @BeforeEach
    void setUp() {
        this.testMessageFactory = new MessageFactory();
        this.observer1 = new NamedObserverStubMessageToClient("Player 1");
        this.observer2 = new NamedObserverStubMessageToClient("Player 2");
        this.observer3 = new NamedObserverStubMessageToClient("Player 3");
        this.observer4 = new NamedObserverStubMessageToClient("Player 4");
        this.observer5 = new NamedObserverStubMessageToClient("Player 5");
        this.logger = new AnonymousObserverStubMessageToClient();

        testMessageFactory.attachObserver("Player 1", observer1);
        testMessageFactory.removeObserver("Player 1");
        testMessageFactory.attachObserver("Player 1", observer1);
        testMessageFactory.attachObserver("Player 2", observer2);
        testMessageFactory.removeObserver(observer2);
        testMessageFactory.attachObserver("Player 2", observer2);
        testMessageFactory.attachObserver("Player 3", observer3);
        testMessageFactory.attachObserver("Player 4", observer4);
        testMessageFactory.attachObserver("Player 5", observer5);
        testMessageFactory.removeObserver(observer5);
        //Player 5 is not attached to this message factory: no message should go to it

        testMessageFactory.attachObserver(logger);
        testMessageFactory.removeObserver(logger);
        testMessageFactory.attachObserver(logger);

    }

    @Test
    void testSendMessageToPlayer() {
        testMessageFactory.sendMessageToPlayer("Player 2", new MessageToClientStub());

        assertTrue(observer1.messageQueue.isEmpty());
        assertEquals(1,observer2.messageQueue.size());
        assertTrue(observer3.messageQueue.isEmpty());
        assertTrue(observer4.messageQueue.isEmpty());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }

    @Test
    void testSendMessageToMultiplePlayers() {
        testMessageFactory.sendMessageToPlayer(List.of("Player 2", "Player 4"), new MessageToClientStub());

        assertTrue(observer1.messageQueue.isEmpty());
        assertEquals(1,observer2.messageQueue.size());
        assertTrue(observer3.messageQueue.isEmpty());
        assertEquals(1,observer4.messageQueue.size());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }

    @Test
    void sendMessageToAllGamePlayers() {
        testMessageFactory.sendMessageToAllGamePlayers(new MessageToClientStub());

        assertEquals(1,observer1.messageQueue.size());
        assertEquals(1,observer2.messageQueue.size());
        assertEquals(1,observer3.messageQueue.size());
        assertEquals(1,observer4.messageQueue.size());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }

    @Test
    void sendMessageToAllGamePlayersExcept() {
        testMessageFactory.sendMessageToAllGamePlayersExcept(new MessageToClientStub(), "Player 3");

        assertEquals(1,observer1.messageQueue.size());
        assertEquals(1,observer2.messageQueue.size());
        assertTrue(observer3.messageQueue.isEmpty());
        assertEquals(1,observer4.messageQueue.size());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }

    @Test
    void testSendMessageToNonExistingPlayer() {
        testMessageFactory.sendMessageToPlayer("NonExistingPlayer", new MessageToClientStub());

        assertTrue(observer1.messageQueue.isEmpty());
        assertTrue(observer2.messageQueue.isEmpty());
        assertTrue(observer3.messageQueue.isEmpty());
        assertTrue(observer4.messageQueue.isEmpty());
        assertTrue(observer5.messageQueue.isEmpty());
    }

    @Test
    void testSendMessageWithNoReceivers() {
        testMessageFactory.sendMessageToPlayer(List.of(), new MessageToClientStub());

        assertTrue(observer1.messageQueue.isEmpty());
        assertTrue(observer2.messageQueue.isEmpty());
        assertTrue(observer3.messageQueue.isEmpty());
        assertTrue(observer4.messageQueue.isEmpty());
        assertTrue(observer5.messageQueue.isEmpty());
    }

    @Test
    void testSendMessageRemoveObserver() {
        testMessageFactory.removeObserver(observer4);
        testMessageFactory.sendMessageToAllGamePlayers(new MessageToClientStub());

        assertEquals(1,observer1.messageQueue.size());
        assertEquals(1,observer2.messageQueue.size());
        assertEquals(1,observer3.messageQueue.size());
        assertTrue(observer4.messageQueue.isEmpty());
        assertTrue(observer5.messageQueue.isEmpty());
    }
}

class NamedObserverStubMessageToClient implements ObserverMessageToClient<MessageToClient> {
    final Queue<MessageToClient> messageQueue;

    final String observerName;
    public NamedObserverStubMessageToClient(String observerName) {
        this.messageQueue = new ArrayDeque<>();
        this.observerName = observerName;
    }

    @Override
    public void update(MessageToClient message) {
        messageQueue.add(message);
    }
}

class AnonymousObserverStubMessageToClient implements ObserverMessageToClient<MessageToClient> {
    final Queue<MessageToClient> messageQueue;

    public AnonymousObserverStubMessageToClient() {
        this.messageQueue = new ArrayDeque<>();
    }

    @Override
    public void update(MessageToClient message) {
        messageQueue.add(message);
    }
}

class MessageToClientStub extends MessageToClient {
        @Override
        public void accept(MessageToClientVisitor visitor) {
        }
}