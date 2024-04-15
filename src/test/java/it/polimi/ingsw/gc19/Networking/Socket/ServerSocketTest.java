package it.polimi.ingsw.gc19.Networking.Socket;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.DisconnectGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ServerSocketTest {

    private Client client1, client2, client3, client4;

    @BeforeAll
    public static void startServer(){
        ServerApp.startTCP();
    }

    @BeforeEach
    public void setUp(){
        this.client1 = new Client("client1");
        this.client2 = new Client("client2");
        this.client3 = new Client("client3");
        this.client4 = new Client("client4");
    }

    @AfterEach
    public void tearDown(){
        this.client1.disconnect();
        this.client1.stopClient();
        this.client2.disconnect();
        this.client2.stopClient();
        this.client3.disconnect();
        this.client3.stopClient();
        this.client4.disconnect();
        this.client4.stopClient();
    }

    @AfterAll
    public static void stopTCPServer(){
        ServerApp.stopTCP();
    }

    @Test
    public void testCreatePlayer(){
        this.client1.createPlayer();
        assertMessageEquals(this.client1, new CreatedPlayerMessage(this.client1.getName()));
        this.client2.createPlayer();
        assertMessageEquals(this.client2, new CreatedPlayerMessage((this.client2.getName())));
        this.client3.createPlayer();
        assertMessageEquals(this.client3, new CreatedPlayerMessage((this.client3.getName())));
        this.client4.createPlayer();
        assertMessageEquals(this.client4, new CreatedPlayerMessage((this.client4.getName())));

        this.client1.createPlayer();
        assertMessageEquals(this.client1, new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
    }

    @Test
    public void testPlayerCanJoinFullGame(){
        this.client1.createPlayer();
        this.client1.createGame("game5", 2, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game5").setHeader(this.client1.getName()));


        this.client2.createPlayer();
        this.client2.joinGame("game5", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game5").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client3.createPlayer();
        this.client3.joinGame("game5", false);
        assertMessageEquals(this.client3, new GameHandlingError(Error.GAME_NOT_ACCESSIBLE, null));
    }

    @Test
    public void testMultipleGames(){
        this.client1.createPlayer();
        this.client1.createGame("game8", 2, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game8").setHeader(this.client1.getName()));
        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearQueue();

        this.client2.createPlayer();
        this.client2.joinGame("game8", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game8").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));

        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);
        this.client1.clearQueue();
        this.client2.clearQueue();

        this.client3.createPlayer();
        this.client3.createGame("game9", 2, 1);
        assertMessageEquals(this.client3, new CreatedGameMessage("game9").setHeader(this.client3.getName()));

        this.client4.createPlayer();
        this.client4.joinGame("game9", false);
        assertMessageEquals(this.client4, new JoinedGameMessage("game9").setHeader(this.client4.getName()));
        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getName()));


        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        this.client3.sendChatMessage(new ArrayList<>(List.of(this.client3.getName(), this.client4.getName())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage(this.client3.getName(), "Message in chat"));

        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
    }

    @Test
    public void testFirePlayersAndGames() throws RemoteException {

        this.client1.createPlayer();
        this.client2.createPlayer();
        this.client3.createPlayer();
        this.client4.createPlayer();

        this.client1.createGame("game2", 4, 1);
        this.client2.joinGame("game2", true);
        this.client3.joinGame("game2", true);
        this.client4.joinGame("game2", true);

        allPlayersChooseColor(client1, client2, client3, client4);
        allPlayersChoosePrivateGoal(client1, client2, client3, client4);
        allPlayersPlacedInitialCard(client1, client2, client3, client4);

        assertMessageEquals(List.of(this.client4, this.client3, this.client2, this.client1), new StartPlayingGameMessage(this.client1.getName()));

        assertMessageEquals(List.of(this.client4, this.client3, this.client2, this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE));

        this.client3.disconnect();
        this.client4.disconnect();

        //assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyFirstTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        //assertFalse(gameController.getGameAssociated().getFinalCondition());
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP);
        // client1 reached 20 points: final condition should be true, but not in final round
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        dummyTurn(client2, PlayableCardType.RESOURCE);

        // now it should be the final round:
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertTrue(gameController.getGameAssociated().isFinalRound());

        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE));
        client1.placeCard("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.DRAW));
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        System.out.println("ok");

        // game should end and declare client1 the winner
        //assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals(client1, gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());


        waitingThread(4000);

        assertMessageEquals(List.of(this.client2, this.client1), new DisconnectGameMessage("game2"));
    }

    @Test
    public void testCreateGame(){
        //Client1 tries to create a game without having registered his player
        this.client1.createGame("game1", 3, 1);
        assertMessageEquals(this.client1, new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        this.client1.createPlayer();
        assertMessageEquals(this.client1, new CreatedPlayerMessage(this.client1.getName()));
        this.client1.createGame("game1", 3, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game1"));

        this.client2.createPlayer();
        this.client2.createGame("game1", 2, 1);
        assertMessageEquals(this.client2, new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE, null));

        this.client2.joinGame("game1", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game1"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));

        this.client3.joinGame("game1", false);
        assertMessageEquals(this.client3, new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        this.client3.createPlayer();
        this.client3.joinGame("game1", false);
        assertMessageEquals(this.client3, new JoinedGameMessage("game1"));
        assertMessageEquals(List.of(this.client2, this.client1), new NewPlayerConnectedToGameMessage(this.client3.getName()));
    }

    @Test
    public void testReconnection() throws RemoteException {
        this.client1.setOt("c1");
        this.client1.createPlayer();

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();

        this.client1.createGame("game15", 2, 1);

        client1.waitForMessage(GameConfigurationMessage.class);
        client1.clearQueue();

        this.client2.setOt("c2");
        this.client2.createPlayer();
        this.client2.joinGame("game15", true);

        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearQueue();

        this.client1.stopSendingHeartBeat();

        waitingThread(2500);

        Client client7 = new Client(this.client1.getName());
        client7.setToken(token1);
        client7.setOt("c7");

        client7.reconnect();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        /*try {
            Thread.sleep(3500);
        }
        catch (InterruptedException interruptedException){ };*/

        client7.sendChatMessage(new ArrayList<>(List.of(this.client2.getName())), "Send chat message after reconnection");

        System.out.println("ss");

        assertMessageEquals(this.client2, new NotifyChatMessage(client7.getName(), "Send chat message after reconnection"));
        System.out.println("ss");
        assertNull(this.client1.getMessage());

        client2.sendChatMessage(new ArrayList<>(List.of(this.client2.getName(), client1.getName())), "Send chat message after reconnection!");

        assertMessageEquals(List.of(this.client2, client7), new NotifyChatMessage(this.client2.getName(), "Send chat message after reconnection!"));
        assertNull(this.client1.getMessage());
    }

    private void dummyTurn(Client client, PlayableCardType cardType) throws RemoteException {
        dummyPlace(client);
        assertMessageEquals(this.client2, new TurnStateMessage(this.client2.getName(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(Client client, PlayableCardType cardType){
        dummyFirstPlace(client);
        assertMessageEquals(this.client2, new TurnStateMessage(this.client2.getName(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstPlace(Client client){
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        client.setAnchorCard(latestMessage.getInitialCard());
        client.setCardToPlace(latestMessage.getCardsInHand().getFirst());

        assertMessageEquals(this.client2, new TurnStateMessage(this.client2.getName(), TurnState.PLACE));
        client.placeCard(client.getCardToPlace().getCardCode(), client.getAnchorCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
    }

    private void dummyPlace(Client client){
        AcceptedPickCardMessage latestMessage;
        do {
            System.out.println(client.getName());
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getName()));

        client.setAnchorCard(client.getCardToPlace());
        client.setCardToPlace(latestMessage.getPickedCard());

        assertMessageEquals(this.client2, new TurnStateMessage(this.client2.getName(), TurnState.PLACE));

        client.placeCard(client.getCardToPlace().getCardCode(), client.getAnchorCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
    }

    private void allPlayersPlacedInitialCard(Client client1, Client client2, Client client3, Client client4){
        client1.placeInitialCard(CardOrientation.DOWN);
        client2.placeInitialCard(CardOrientation.DOWN);
        client3.placeInitialCard(CardOrientation.UP);
        client4.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(Client client1, Client client2, Client client3, Client client4) {
        client1.choosePrivateGoalCard(0);
        client2.choosePrivateGoalCard(1);
        client3.choosePrivateGoalCard(0);
        client4.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(Client client1, Client client2, Client client3, Client client4){
        client1.chooseColor(Color.RED);
        client2.chooseColor(Color.GREEN);
        client3.chooseColor(Color.BLUE);
        client4.chooseColor(Color.YELLOW);
    }

    private void assertMessageEquals(Client receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, Client... receivers) {
        ArrayList<Client> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }

    private void assertMessageEquals(List<Client> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(Client::getName).toList();
        message.setHeader(receiversName);
        for (Client receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void clearQueue(List<Client> clients) {
        for (Client player : clients) {
            player.clearQueue();
        }
    }

    private void waitingThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

class Client{

    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final Deque<MessageToClient> incomingMessages;
    private String name;
    private Boolean sendHeartBeat;
    private String token;
    private PlayableCard cardToPlace;
    private PlayableCard anchorCard;
    private final ScheduledExecutorService heartBeatThread = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService receiverThread = Executors.newSingleThreadExecutor();
    private String ot = null;

    public void setOt(String ot) {
        this.ot = ot;
    }

    public Client(String name){
        try{
            this.socket = new Socket(Settings.DEFAULT_SERVER_IP, Settings.DEFAULT_SERVER_PORT);
            this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.name = name;
        this.incomingMessages = new ArrayDeque<>();
        this.sendHeartBeat = false;
        this.token = null;

        this.heartBeatThread.scheduleAtFixedRate(this::heartBeat, 0, 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 5, TimeUnit.MILLISECONDS);
        this.receiverThread.submit(this::receiveMessages);
    }

    public void sendMessage(MessageToServer message){
        boolean sent;
        synchronized (this.outputStream){
            sent = false;
            while(!sent) {
                try {
                    //System.out.println(message);
                    this.outputStream.writeObject(message);
                    finalizeSending();
                    sent = true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void receiveMessages(){
        MessageToClient incomingMessage;
        while (true){
            try{
                incomingMessage = (MessageToClient) this.inputStream.readObject();
                System.out.println(name + " / " + ot +  " -> " + incomingMessage);
            } catch (IOException  | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            synchronized (this.incomingMessages){
                this.incomingMessages.add(incomingMessage);
                this.incomingMessages.notifyAll();
            }
        }
    }

    private void finalizeSending() throws IOException {
        synchronized (this.outputStream){
            this.outputStream.flush();
            this.outputStream.reset();
        }
    }

    private synchronized void heartBeat() {
        if (this.sendHeartBeat) {
            this.sendMessage(new HeartBeatMessage(this.name));
        }
    }

    public void stopClient(){
        try {
            this.socket.close();
        }
        catch (IOException ioException){
            throw new RuntimeException(ioException);
        }

        this.receiverThread.shutdownNow();
        this.heartBeatThread.shutdownNow();
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PlayableCard getAnchorCard() {
        return anchorCard;
    }

    public void setAnchorCard(PlayableCard anchorCard) {
        this.anchorCard = anchorCard;
    }

    public PlayableCard getCardToPlace() {
        return cardToPlace;
    }

    public void setCardToPlace(PlayableCard cardToPlace) {
        this.cardToPlace = cardToPlace;
    }

    public synchronized void stopSendingHeartBeat() {
        this.sendHeartBeat = false;
        this.notify();
    }

    public synchronized void startSendingHeartBeat() {
        this.sendHeartBeat = true;
        this.notify();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void waitForMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.incomingMessages) {
            while (this.incomingMessages.stream().noneMatch(messageToClientClass::isInstance)) {
                try {
                    this.incomingMessages.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public int waitAndNotifyTypeOfMessage(Class<? extends MessageToClient> messageToClientClass1, Class<? extends MessageToClient> messageToClientClass2) {
        synchronized (this.incomingMessages) {
            while (this.incomingMessages.stream().noneMatch(messageToClientClass1::isInstance) && this.incomingMessages.stream().noneMatch(messageToClientClass2::isInstance)) {
                try {
                    this.incomingMessages.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if(this.incomingMessages.stream().noneMatch(messageToClientClass1::isInstance)){
                return 1;
            }
            return 0;
        }
    }

    public MessageToClient getMessage() {
        return getMessage(MessageToClient.class);
    }

    public MessageToClient getMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.incomingMessages) {
            while (!this.incomingMessages.isEmpty()) {
                MessageToClient res = this.incomingMessages.remove();
                if (messageToClientClass.isInstance(res)) return res;
            }
        }
        return null;
    }

    public void clearQueue() {
        synchronized (this.incomingMessages) {
            this.incomingMessages.clear();
        }
    }

    public void createPlayer(){
        this.sendMessage(new NewUserMessage(this.name));
        this.startSendingHeartBeat();
    }

    public void disconnect(){
        this.sendMessage(new DisconnectMessage(this.name));
        this.stopSendingHeartBeat();
    }

    public void createGame(String gameName, int numOfPlayers, long randomSeed){
        this.sendMessage(new CreateNewGameMessage(this.name, gameName, numOfPlayers, randomSeed));
    }

    public void joinGame(String gameName, boolean wait){
        boolean found = false;
        if(wait) {
            while (!found) {
                this.sendMessage(new JoinGameMessage(gameName, this.name));
                //System.out.println(name + " -> " + gameName);
                if (waitAndNotifyTypeOfMessage(GameHandlingError.class, JoinedGameMessage.class) == 1) {
                    found = true;
                }
                else {
                    getMessage(GameHandlingError.class);
                }
            }
        }
        else{
            this.sendMessage(new JoinGameMessage(gameName, this.name));
        }
    }
    
    public void pickCardFromDeck(PlayableCardType type){
        this.sendMessage(new PickCardFromDeckMessage(name, type));
    }
    
    public void pickCardFromTable(PlayableCardType type, int position){
        this.waitForMessage(TurnStateMessage.class);
        this.sendMessage(new PickCardFromTableMessage(name, type, position));
    }
    
    public void placeCard(String toPlace, String anchorCode, Direction direction, CardOrientation orientation){
        this.sendMessage(new PlaceCardMessage(name, toPlace, anchorCode, direction, orientation));
    }
    
    public void placeInitialCard(CardOrientation orientation){
        this.sendMessage(new DirectionOfInitialCardMessage(name, orientation));
    }
    
    public void chooseColor(Color color){
        this.sendMessage(new ChosenColorMessage(name, color));
    }
    
    public void choosePrivateGoalCard(int cardIdx){
        this.sendMessage(new ChosenGoalCardMessage(name, cardIdx));
    }

    public void sendChatMessage(List<String> receivers, String message){
        this.sendMessage(new PlayerChatMessage(new ArrayList<>(receivers), name, message));
    }

    public void reconnect(){
        this.sendMessage(new ReconnectToServerMessage(this.name, this.token));
    }

}
