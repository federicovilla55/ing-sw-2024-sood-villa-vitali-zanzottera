package it.polimi.ingsw.gc19.Networking.RMI.Socket;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardFromTable;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OwnAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RMIServerAndMainControllerTest {
    private Client client1, client2, client3, client4, client5;
    private static VirtualMainServer virtualMainServer;

    @BeforeAll
    public static void setUpServer() throws IOException, NotBoundException{
        ServerApp.main(null);
        Registry registry = LocateRegistry.getRegistry(Settings.mainRMIServerName, 12122);
        virtualMainServer = (VirtualMainServer) registry.lookup(Settings.mainRMIServerName);
    }

    @BeforeEach
    public void setUpTest() throws RemoteException{
        this.client1 = new Client(virtualMainServer, "client1");
        this.client2 = new Client(virtualMainServer, "client2");
        this.client3 = new Client(virtualMainServer, "client3");
        this.client4 = new Client(virtualMainServer, "client4");
        this.client5 = new Client(virtualMainServer, "client5");
    }

    @AfterEach
    public void resetClients() throws RemoteException {
        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
        this.client5.disconnect();
    }

    @Test
    public void testCreateClient() throws RemoteException {
        this.client1.connect();
        assertMessageEquals(this.client1, new CreatedPlayerMessage(this.client1.getName()));
        this.client2.connect();
        assertMessageEquals(this.client2, new CreatedPlayerMessage(this.client2.getName()));
        assertNull(this.client1.getMessage());
        this.client3.connect();
        assertMessageEquals(this.client3, new CreatedPlayerMessage(this.client3.getName()));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        this.client4.connect();
        assertMessageEquals(this.client4, new CreatedPlayerMessage(this.client4.getName()));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        assertNull(this.client3.getMessage());

        this.client1.connect();
        assertMessageEquals(this.client1, new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
        assertNull(this.client2.getMessage());
        assertNull(this.client3.getMessage());
        assertNull(this.client4.getMessage());

        //Create new client with other name
        this.client5.setName("client1");
        this.client5.connect();
        assertMessageEquals(this.client5, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, null));
        this.client5.setName("client5");

        this.client1.stopSendingHeartBeat();
        this.client2.stopSendingHeartBeat();
        this.client3.stopSendingHeartBeat();
        this.client4.startSendingHeartBeat();

        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateGame() throws RemoteException {
        this.client1.connect();
        this.client1.clearQueue();
        this.client1.newGame("game1", 2);
        assertMessageEquals(this.client1, new CreatedGameMessage("game1").setHeader(this.client1.getName()));

        //Player already registered to some games
        this.client1.clearQueue();
        this.client1.newGame("game2", 2);
        assertMessageEquals(this.client1, new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));

        //Player already registered and game name equal
        this.client1.clearQueue();
        this.client1.newGame("game1", 2);
        assertMessageEquals(this.client1, new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));

        //Game name already in use
        this.client2.connect();
        this.client2.clearQueue();
        this.client2.newGame("game1", 2);
        assertMessageEquals(this.client2, new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE, null));

        this.client1.stopSendingHeartBeat();
        this.client2.stopSendingHeartBeat();
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMultiplePlayerInGame() throws RemoteException {
        this.client1.connect();
        this.client1.clearQueue();
        VirtualGameServer gameServer1 = this.client1.newGame("game3", 3);
        assertMessageEquals(this.client1, new CreatedGameMessage("game3").setHeader(this.client1.getName()));
        this.client1.clearQueue();

        this.client2.connect();
        this.client2.clearQueue();
        VirtualGameServer gameServer2 =  this.client2.joinGame("game3");
        assertMessageEquals(this.client2, new JoinedGameMessage("game3").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));
        this.client2.clearQueue();
        this.client1.clearQueue();

        this.client3.connect();
        this.client3.clearQueue();
        VirtualGameServer gameServer3 =  this.client3.joinGame("game3");
        assertMessageEquals(this.client3, new JoinedGameMessage("game3").setHeader(this.client3.getName()));
        assertMessageEquals(new NewPlayerConnectedToGameMessage(this.client3.getName()), this.client2, this.client1);

        this.client1.clearQueue();
        this.client2.clearQueue();
        this.client3.clearQueue();
        gameServer3.sendChatMessage(new ArrayList<>(List.of(this.client1.getName(), this.client2.getName())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client3.getName(), "Message in chat"));

        this.client1.clearQueue();
        this.client2.clearQueue();
        this.client3.clearQueue();
        gameServer3.chooseColor(Color.BLUE);
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client2, this.client1)), new AcceptedColorMessage(this.client3.getName(), Color.BLUE));
        assertMessageEquals(new ArrayList<>(List.of(this.client2, this.client1)), new AvailableColorsMessage(new ArrayList<>(List.of(Color.GREEN, Color.YELLOW, Color.RED))));

        this.client1.stopSendingHeartBeat();
        this.client2.stopSendingHeartBeat();
        this.client3.stopSendingHeartBeat();
        waitingThread(1000);
    }

    @Test
    public void testPlayerCanJoinFullGame() throws RemoteException {
        this.client1.connect();
        this.client1.clearQueue();
        VirtualGameServer gameServer1 = this.client1.newGame("game5", 2);
        assertMessageEquals(this.client1, new CreatedGameMessage("game5").setHeader(this.client1.getName()));
        this.client1.clearQueue();

        this.client2.connect();
        this.client2.clearQueue();
        VirtualGameServer gameServer2 =  this.client2.joinGame("game5");
        assertMessageEquals(this.client2, new JoinedGameMessage("game5").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));
        this.client2.clearQueue();
        this.client1.clearQueue();

        this.client3.connect();
        this.client3.clearQueue();
        VirtualGameServer gameServer3 =  this.client3.joinGame("game5");
        assertMessageEquals(this.client3, new GameHandlingError(Error.GAME_NOT_ACCESSIBLE, null));
    }

    @Test
    public void testMultipleGames() throws RemoteException{
        this.client1.connect();
        this.client1.clearQueue();
        VirtualGameServer gameServer1 = this.client1.newGame("game8", 2);
        assertMessageEquals(this.client1, new CreatedGameMessage("game8").setHeader(this.client1.getName()));
        this.client1.clearQueue();

        this.client2.connect();
        this.client2.clearQueue();
        VirtualGameServer gameServer2 =  this.client2.joinGame("game8");
        assertMessageEquals(this.client2, new JoinedGameMessage("game8").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));
        this.client2.clearQueue();
        this.client1.clearQueue();

        this.client3.connect();
        this.client3.clearQueue();
        VirtualGameServer gameServer3 = this.client3.newGame("game9", 2);
        assertMessageEquals(this.client3, new CreatedGameMessage("game9").setHeader(this.client3.getName()));
        this.client3.clearQueue();

        this.client4.connect();
        this.client4.clearQueue();
        VirtualGameServer gameServer4 =  this.client4.joinGame("game9");
        assertMessageEquals(this.client4, new JoinedGameMessage("game9").setHeader(this.client4.getName()));
        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getName()));
        this.client4.clearQueue();
        this.client3.clearQueue();

        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        gameServer3.sendChatMessage(new ArrayList<>(List.of(this.client3.getName(), this.client4.getName())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage(this.client3.getName(), "Message in chat"));

        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
    }

    @Test
    public void testJoinFirstAvailableGames() throws RemoteException{
        this.client1.connect();
        this.client1.clearQueue();
        VirtualGameServer gameServer1 = this.client1.newGame("game4", 2);
        assertNotNull(gameServer1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game4").setHeader(this.client1.getName()));
        this.client1.clearQueue();

        this.client2.connect();
        this.client2.clearQueue();
        VirtualGameServer gameServer2 =  this.client2.joinGame("game4");
        assertNotNull(gameServer2);
        assertMessageEquals(this.client2, new JoinedGameMessage("game4").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));
        this.client2.clearQueue();
        this.client1.clearQueue();

        this.client3.connect();
        this.client3.clearQueue();
        VirtualGameServer gameServer3 = this.client3.newGame("game7", 2);
        assertNotNull(gameServer3);
        assertMessageEquals(this.client3, new CreatedGameMessage("game7").setHeader(this.client3.getName()));
        this.client3.clearQueue();

        this.client4.connect();
        this.client4.clearQueue();
        VirtualGameServer gameServer4 =  this.client4.joinFirstAvailableGame();
        assertNotNull(gameServer4);
        assertMessageEquals(this.client4, new JoinedGameMessage("game7").setHeader(this.client4.getName()));
        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getName()));
        this.client4.clearQueue();
        this.client3.clearQueue();

        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        this.client5 = new Client(virtualMainServer, "client5");
        this.client5.connect();
        this.client5.clearQueue();
        VirtualGameServer gameServer5 =  this.client5.joinFirstAvailableGame();
        assertNull(gameServer5);
        assertMessageEquals(new GameHandlingError(Error.NO_GAMES_FREE_TO_JOIN, null));

    }

    @Test
    public void testDisconnectionWhileInLobby() throws RemoteException {
        this.client1.connect();
        this.client1.clearQueue();

        this.client2.connect();
        this.client2.clearQueue();
        VirtualGameServer virtualGameServer = this.client2.newGame("game11", 2);

        this.client2.stopSendingHeartBeat();

        waitingThread(1500);

        Client client6 = new Client(virtualMainServer, this.client2.getName());
        client6.connect();
        assertMessageEquals(client6, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, null));

        this.client2.clearQueue();
        this.client2.reconnect();
        assertMessageEquals(this.client2, new JoinedGameMessage("game11"));

        this.client1.stopSendingHeartBeat();
        waitingThread(1500);
        this.client1.startSendingHeartBeat();
        this.client1.clearQueue();
        this.client1.reconnect();
        assertMessageEquals(this.client1, new AvailableGamesMessage(new ArrayList<>(List.of("game1", "game11"))));

        this.client1.clearQueue();
        this.client1.reconnect();
        assertMessageEquals(this.client1, new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));

        this.client1.stopSendingHeartBeat();
        waitingThread(1500);
        Client client7 = new Client(virtualMainServer, this.client1.getName());
        client7.clearQueue();
        client7.reconnect();
        assertMessageEquals(client7, new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        Client client8 = new Client(virtualMainServer, this.client1.getName());
        client8.connect();
        client8.clearQueue();
        client8.reconnect();
        assertMessageEquals(client8, new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
    }

    @Test
    public void testDisconnectionWhileInGame() throws RemoteException {
        this.client1.connect();
        this.client1.clearQueue();
        VirtualGameServer gameServer1 = this.client1.newGame("game6", 2);
        assertNotNull(gameServer1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game6").setHeader(this.client1.getName()));
        this.client1.clearQueue();

        this.client2.connect();
        this.client2.clearQueue();
        VirtualGameServer gameServer2 =  this.client2.joinGame("game6");
        assertNotNull(gameServer2);
        assertMessageEquals(this.client2, new JoinedGameMessage("game6").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));
        this.client2.clearQueue();
        this.client1.clearQueue();

        this.client2.stopSendingHeartBeat();

        waitingThread(2000);

        //Situation: client 2 has disconnected from game
        Client client6 = new Client(virtualMainServer, this.client2.getName());
        VirtualGameServer virtualGameServer2 = client2.reconnect();
        assertMessageEquals(client2, new JoinedGameMessage("game6"));

        client6.clearQueue();
        client6.reconnect();
        assertMessageEquals(client6, new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        client2.clearQueue();
        client1.clearQueue();
        gameServer2.sendChatMessage(new ArrayList<>(List.of(this.client1.getName(), this.client2.getName())), "Chat message after disconnection!");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client2.getName(), "Chat message after disconnection!"));

    }

    @Test
    public void testFirePlayersAndGames() throws RemoteException {
        
        this.client1.connect();
        this.client2.connect();
        this.client3.connect();
        this.client4.connect();
        
        VirtualGameServer gameServer1 = this.client1.newGame("game13", 4, 1);
        VirtualGameServer gameServer2 = this.client2.joinGame("game13");
        VirtualGameServer gameServer3 = this.client3.joinGame("game13");
        VirtualGameServer gameServer4 = this.client4.joinGame("game13");
        
        assertNotNull(gameServer1);
        assertNotNull(gameServer2);
        assertNotNull(gameServer3);
        assertNotNull(gameServer4);

        allPlayersChooseColor(gameServer1, gameServer2, gameServer3, gameServer4);

        allPlayersChoosePrivateGoal(gameServer1, gameServer2, gameServer3, gameServer4);

        allPlayersPlacedInitialCard(gameServer1, gameServer2, gameServer3, gameServer4);

        this.client3.disconnect();
        this.client4.disconnect();

        // client1 turn
        gameServer1.placeCard("resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        waitingThread(100);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        //assertFalse(gameController.getGameAssociated().getFinalCondition());
        gameServer1.placeCard("gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP);
        // client1 reached 20 points: final condition should be true, but not in final round
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        // now it should be the final round:
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertTrue(gameController.getGameAssociated().isFinalRound());

        gameServer1.placeCard("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        // game should end and declare client1 the winner
        //assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals(gameServer1, gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());

        this.client1.clearQueue();
        this.client2.clearQueue();

        waitingThread(10000);

        assertMessageEquals(List.of(this.client2, this.client1), new DisconnectGameMessage("game13"));
    }

    @Test
    public void testReconnection() throws RemoteException {
        this.client1.connect();

        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();

        VirtualGameServer gameServer1 = this.client1.newGame("game15", 2);

        this.client2.connect();
        VirtualGameServer gameServer2 = this.client2.joinGame("game15");

        this.client1.clearQueue();
        this.client1.stopSendingHeartBeat();
        waitingThread(2500);

        Client client7 = new Client(virtualMainServer, this.client1.getName());
        waitingThread(1000);
        client7.setToken(token1);
        VirtualGameServer gameServer7 = client7.reconnect();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        assertNotEquals(gameServer1, gameServer7);

        this.client2.clearQueue();
        gameServer7.sendChatMessage(new ArrayList<>(List.of(this.client2.getName())), "Send chat message after reconnection");
        assertMessageEquals(this.client2, new NotifyChatMessage(client7.getName(),"Send chat message after reconnection"));
        assertNull(this.client1.getMessage());
    }

    private void dummyTurn(VirtualGameServer virtualGameServer, Client client, PlayableCardType cardType) throws RemoteException {
        dummyPlace(virtualGameServer, client);
        virtualGameServer.pickCardFromDeck(cardType);
    }

    private void dummyPlace(VirtualGameServer virtualGameServer, Client client) throws RemoteException {
        MessageToClient currMessage = client.getMessage();
        MessageToClient latestMessage = null;
        boolean found = false;

        while(currMessage != null){
            //System.out.println(currMessage.getClass());
            if((currMessage instanceof AcceptedPickCardFromTable) && (((AcceptedPickCardFromTable) currMessage)).getNick().equals(client.getName())){
                latestMessage = currMessage;
            }
            if(currMessage instanceof OwnAcceptedPickCardFromDeckMessage){
                latestMessage = currMessage;

            }
            if(currMessage instanceof OwnStationConfigurationMessage){
                latestMessage = currMessage;
            }
            currMessage = client.getMessage();
        }

        switch (latestMessage) {
            case AcceptedPickCardFromTable acceptedPickCardFromTable -> {
                client.setAnchorCard(client.getCardToPlace());
                client.setCardToPlace(acceptedPickCardFromTable.getPickedCard());
            }
            case OwnAcceptedPickCardFromDeckMessage ownAcceptedPickCardFromDeckMessage -> {
                client.setAnchorCard(client.getCardToPlace());
                client.setCardToPlace(ownAcceptedPickCardFromDeckMessage.getPickedCard());
            }
            case OwnStationConfigurationMessage ownStationConfigurationMessage -> {
                client.setAnchorCard(ownStationConfigurationMessage.getInitialCard());
                client.setCardToPlace(ownStationConfigurationMessage.getCardsInHand().getFirst());
            }
            default -> {
            }
        }

        assert client.getAnchorCard() != null;

        virtualGameServer.placeCard(client.getCardToPlace().getCardCode(), client.getAnchorCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        client.setAnchorCard(client.getCardToPlace());
    }

    private void allPlayersPlacedInitialCard(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2, 
                                             VirtualGameServer virtualGameServer3, VirtualGameServer virtualGameServer4) throws RemoteException {
        virtualGameServer1.placeInitialCard(CardOrientation.DOWN);
        virtualGameServer2.placeInitialCard(CardOrientation.DOWN);
        virtualGameServer3.placeInitialCard(CardOrientation.UP);
        virtualGameServer4.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2,
                                             VirtualGameServer virtualGameServer3, VirtualGameServer virtualGameServer4) throws RemoteException {
        virtualGameServer1.choosePrivateGoalCard(0);
        virtualGameServer2.choosePrivateGoalCard(1);
        virtualGameServer3.choosePrivateGoalCard( 0);
        virtualGameServer4.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2,
                                       VirtualGameServer virtualGameServer3, VirtualGameServer virtualGameServer4) throws RemoteException {
        virtualGameServer1.chooseColor(Color.RED);
        virtualGameServer2.chooseColor(Color.GREEN);
        virtualGameServer3.chooseColor(Color.BLUE);
        virtualGameServer4.chooseColor(Color.YELLOW);
    }

    private void assertMessageEquals(Client receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, Client ... receivers) {
        ArrayList<Client> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }

    private void assertMessageEquals(List<Client> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(Client::getName).toList();
        message.setHeader(receiversName);
        for(Client receiver : receivers) {
            assertEquals(message, receiver.getMessage());
        }
    }

    private  void clearQueue(List<Client> clients) {
        for(Client player : clients) {
            player.clearQueue();
        }
    }

    private void waitingThread(long millis){
        try{
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

class Client extends UnicastRemoteObject implements VirtualClient, Serializable{

    private final Deque<MessageToClient> incomingMessages;
    private final VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;
    private String name;
    private Boolean sendHeartBeat;
    private String token;
    private PlayableCard cardToPlace;
    private PlayableCard anchorCard;

    public Client(VirtualMainServer virtualMainServer, String name) throws RemoteException {
        super();
        this.virtualMainServer = virtualMainServer;
        this.name = name;
        this.incomingMessages = new ArrayDeque<>();
        this.sendHeartBeat = false;
        this.token = null;
        new Thread(){
            @Override
            public void run() {
                synchronized (Client.this){
                    while(true) {
                        try {
                            if (Client.this.sendHeartBeat) {
                                virtualMainServer.heartBeat(Client.this);
                                //System.out.println("send heartbeat " + Client.this.name);
                                Client.this.wait(100);
                            }
                            else{
                                Client.this.wait();
                            }
                        } catch (RemoteException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }.start();
    }

    public String getToken(){
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PlayableCard getAnchorCard() {
        return anchorCard;
    }

    public PlayableCard getCardToPlace() {
        return cardToPlace;
    }

    public void setCardToPlace(PlayableCard cardToPlace) {
        this.cardToPlace = cardToPlace;
    }

    public void setAnchorCard(PlayableCard anchorCard) {
        this.anchorCard = anchorCard;
    }

    public synchronized void stopSendingHeartBeat(){
        this.sendHeartBeat = false;
        this.notify();
    }

    public synchronized void startSendingHeartBeat(){
        this.sendHeartBeat = true;
        this.notify();
    }

    public void setVirtualGameServer(VirtualGameServer virtualGameServer) {
        this.virtualGameServer = virtualGameServer;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public void pushUpdate(MessageToClient message){
        if(message instanceof CreatedPlayerMessage){
            this.token = ((CreatedPlayerMessage) message).getToken();
        }
        synchronized (this.incomingMessages){
            this.incomingMessages.add(message);
        }
        /*if(message instanceof NotifyChatMessage){
            System.out.println(((NotifyChatMessage) message).getMessage());
        }*/
    }

    public MessageToClient getMessage(){
        try{
            Thread.sleep(100);
        }
        catch (InterruptedException ignored){}

        synchronized (this.incomingMessages){
            if(!this.incomingMessages.isEmpty()){
                return this.incomingMessages.remove();
            }
        }
        return null;
    }

    public void clearQueue(){
        try{
            Thread.sleep(100);
        }
        catch (InterruptedException ignored){}
        synchronized (this.incomingMessages){
            this.incomingMessages.clear();
        }
    }

    public void removeFirstMessage(){
        try{
            Thread.sleep(100);
        }
        catch (InterruptedException ignored){}
        synchronized (this.incomingMessages){
            if(!this.incomingMessages.isEmpty()) {
                this.incomingMessages.remove();
            }
        }
    }

    public void connect() throws RemoteException {
        this.virtualMainServer.newConnection(this, name);
        startSendingHeartBeat();
    }

    public VirtualGameServer newGame(String gameName, int numOfPlayer) throws RemoteException{
        return this.virtualMainServer.createGame(this, gameName, name, numOfPlayer);
    }

    public VirtualGameServer newGame(String gameName, int numOfPlayer, long randomSeed) throws RemoteException{
        return this.virtualMainServer.createGame(this, gameName, name, numOfPlayer, randomSeed);
    }

    public VirtualGameServer joinGame(String game) throws RemoteException {
        return this.virtualMainServer.joinGame(this, game, name);
    }

    public VirtualGameServer joinFirstAvailableGame() throws RemoteException {
        return this.virtualMainServer.joinFirstAvailableGame(this, this.name);
    }

    public void sendChatMessage(ArrayList<String> receivers, String message) throws RemoteException {
        this.virtualGameServer.sendChatMessage(receivers, message);
    }

    public void disconnect() throws RemoteException{
        this.virtualMainServer.disconnect(this, name);
    }

    public VirtualGameServer reconnect() throws RemoteException{
        return this.virtualMainServer.reconnect(this, name, token);
    }

}
