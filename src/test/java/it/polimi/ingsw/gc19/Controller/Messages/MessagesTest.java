package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Controller.Controller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessagesTest{

    private Controller controller;
    private ClientStub player1, player2, player3;

    @BeforeEach
    public void setUp(){
        this.controller = Controller.getController();

        this.player1 = new ClientStub("player1");
        this.player2 = new ClientStub("player2");
        this.player3 = new ClientStub("player3");
    }

    @Test
    public void setUpGameTest(){
        this.controller.createClient(this.player1);
        Assertions.assertEquals(1, this.player1.sizeOfIncomingMessages());

        this.controller.createClient(this.player2);
        //No nw messages has been sent to player1
        Assertions.assertEquals(1, this.player1.sizeOfIncomingMessages());
        Assertions.assertEquals(1, this.player2.sizeOfIncomingMessages());

        this.player1.removeIncomingMessage(0);
        this.controller.createGame("game1", 3, this.player1);
        Assertions.assertEquals(5, this.player1.sizeOfIncomingMessages());
    }
}
