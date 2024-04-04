package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class MessageFactoryTest {

    private MessageFactory testMessageFactory;

    private NamedObserverStub observer1;
    private NamedObserverStub observer2;
    private NamedObserverStub observer3;
    private NamedObserverStub observer4;
    private NamedObserverStub observer5;



    private AnonymousObserverStub logger;
    @BeforeEach
    void setUp() {
        this.testMessageFactory = new MessageFactory();
        this.observer1 = new NamedObserverStub("Player 1");
        this.observer2 = new NamedObserverStub("Player 2");
        this.observer3 = new NamedObserverStub("Player 3");
        this.observer4 = new NamedObserverStub("Player 4");
        this.observer5 = new NamedObserverStub("Player 5");
        this.logger = new AnonymousObserverStub();

        testMessageFactory.attachObserver("Player 1", observer1);
        testMessageFactory.removeObserver("Player 1");
        testMessageFactory.attachObserver("Player 1", observer1);
        testMessageFactory.attachObserver("Player 2", observer2);
        testMessageFactory.removeObserver(observer2);
        testMessageFactory.attachObserver("Player 2", observer2);
        testMessageFactory.attachObserver("Player 3", observer3);
        testMessageFactory.attachObserver("Player 4", observer4);
        //Player 5 is not attached to this message factory: no message should go to it

        testMessageFactory.attachObserver(logger);
        testMessageFactory.removeObserver(logger);
        testMessageFactory.attachObserver(logger);

    }

    @Test
    void testSendMessageToPlayer() {
        testMessageFactory.sendMessageToPlayer("Player 2", new MessageToClient() {});
        
        assertTrue(observer1.messageQueue.isEmpty());
        assertEquals(1,observer2.messageQueue.size());
        assertTrue(observer3.messageQueue.isEmpty());
        assertTrue(observer4.messageQueue.isEmpty());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }

    @Test
    void testSendMessageToMultiplePlayers() {
        testMessageFactory.sendMessageToPlayer(List.of("Player 2", "Player 4"), new MessageToClient() {});

        assertTrue(observer1.messageQueue.isEmpty());
        assertEquals(1,observer2.messageQueue.size());
        assertTrue(observer3.messageQueue.isEmpty());
        assertEquals(1,observer4.messageQueue.size());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }

    @Test
    void sendMessageToAllGamePlayers() {
        testMessageFactory.sendMessageToAllGamePlayers(new MessageToClient() {});

        assertEquals(1,observer1.messageQueue.size());
        assertEquals(1,observer2.messageQueue.size());
        assertEquals(1,observer3.messageQueue.size());
        assertEquals(1,observer4.messageQueue.size());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }

    @Test
    void sendMessageToAllGamePlayersExcept() {
        testMessageFactory.sendMessageToAllGamePlayersExcept(new MessageToClient() {}, "Player 3");

        assertEquals(1,observer1.messageQueue.size());
        assertEquals(1,observer2.messageQueue.size());
        assertTrue(observer3.messageQueue.isEmpty());
        assertEquals(1,observer4.messageQueue.size());
        assertTrue(observer5.messageQueue.isEmpty());
        assertEquals(1,logger.messageQueue.size());
    }
}

class NamedObserverStub implements Observer<MessageToClient> {
    final Queue<MessageToClient> messageQueue;

    final String observerName;
    public NamedObserverStub(String observerName) {
        this.messageQueue = new ArrayDeque<>();
        this.observerName = observerName;
    }

    @Override
    public void update(MessageToClient message) {
        messageQueue.add(message);
    }
}

class AnonymousObserverStub implements Observer<MessageToClient> {
    final Queue<MessageToClient> messageQueue;

    public AnonymousObserverStub() {
        this.messageQueue = new ArrayDeque<>();
    }

    @Override
    public void update(MessageToClient message) {
        messageQueue.add(message);
    }
}