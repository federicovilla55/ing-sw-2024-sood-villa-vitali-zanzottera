package it.polimi.ingsw.gc19.Networking.Client.ClientTCP;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.*;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.ClientHeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.HeartBeatManager;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat.ServerHeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.MainServerTCP;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * This class represents the "network interface" of clients
 * that use TCP for network communication.
 */
public class ClientTCP implements ClientInterface {

    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private final Object outputStreamLock;

    private String nickname;
    private final MessageHandler messageHandler;
    private final ClientController clientController;

    private final HeartBeatManager heartBeatManager;

    private final Thread senderThread;
    private final Thread receiverThread;

    private final Deque<MessageToServer> messagesToSend;

    public ClientTCP(MessageHandler messageHandler) throws IOException{
        this.messageHandler = messageHandler;
        this.clientController = messageHandler.getClientController();

        try {
            this.socket = new Socket(ClientSettings.TCP_SERVER_IP, ClientSettings.SERVER_TCP_PORT);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            throw new IOException();
        }

        this.outputStreamLock = new Object();

        this.messagesToSend = new ArrayDeque<>();

        this.heartBeatManager = new HeartBeatManager(this);

        this.receiverThread = new Thread(this::receiveMessages);
        this.senderThread = new Thread(this::sendMessageToServer);
        this.receiverThread.start();
        this.senderThread.start();
    }

    /**
     * This method is used to send a message to the server using TCP.
     * @param message the {@link MessageToServer} to be sent
     * @return <code>true</code> if and only if message has been sent correctly (e.g.
     * no {@link IOException} occurred during the sending)
     */
    private boolean send(MessageToServer message){
        synchronized (this.outputStreamLock) {
            try {
                this.outputStream.writeObject(message);
                this.finalizeSending();
            }
            catch (IOException ioException) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is used by thread responsible for sending messages
     * to server.
     */
    private void sendMessageToServer(){
        MessageToServer message;

        while(!Thread.currentThread().isInterrupted()) {
            if(!this.clientController.isDisconnected()) {
                synchronized (this.messagesToSend) {
                    while (this.messagesToSend.isEmpty()) {
                        try {
                            this.messagesToSend.wait();
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    message = this.messagesToSend.removeFirst();
                }

                if (message != null && !send(message)) {
                    synchronized (this.messagesToSend) {
                        this.messagesToSend.clear();
                    }
                    this.signalPossibleNetworkProblem();
                }
            }
        }
    }

    /**
     * This method is used to add a new {@link MessageToServer} to the queue
     * of messages that have to be sent to server.
     * @param message the {@link MessageToServer} that have to be sent to server.
     */
    public void sendMessage(MessageToServer message){
        synchronized (this.messagesToSend){
            this.messagesToSend.addLast(message);
            this.messagesToSend.notifyAll();
        }
    }

    /**
     * This method is used to finalize the message sending: it flushes
     * and reset {@link ObjectOutputStream}.
     * @throws IOException if an error occurred while performing the action
     */
    private void finalizeSending() throws IOException{
        this.outputStream.flush();
        this.outputStream.reset();
    }

    /**
     * This method is used to receive messages from the network and
     * dispatch to the correct class ({@link MessageHandler} or {@link HeartBeatManager})
     */
    private void receiveMessages(){
        MessageToClient incomingMessage = null;
        while(!Thread.interrupted()) {
            try {
                incomingMessage = (MessageToClient) this.inputStream.readObject();
            }
            catch (ClassNotFoundException | IOException ignored){ }

            if(incomingMessage != null && (this.nickname == null || incomingMessage.getHeader() == null || incomingMessage.getHeader().contains(this.nickname))) {
                if(incomingMessage instanceof ServerHeartBeatMessage){
                    this.heartBeatManager.heartBeat();
                }
                else {
                    this.messageHandler.update(incomingMessage);
                }
            }
        }
    }

    /**
     * This method is used to stop client. First, it shuts down
     * {@link HeartBeatManager}, then clears messages to send queue, interrupts
     * both receiver and sender thread and closes socket
     */
    public void stopClient(){
        this.heartBeatManager.stopHeartBeatManager();

        this.senderThread.interrupt();
        this.receiverThread.interrupt();

        synchronized (this.messagesToSend){
            this.messagesToSend.clear();
        }

        try {
            if (socket != null) {
                this.socket.shutdownOutput();
                this.socket.shutdownInput();
                this.socket.close();
            }
        }
        catch (IOException ignored){ }
    }

    /**
     * This method is used to notify the object to start its {@link HeartBeatManager}
     */
    public void startSendingHeartbeat(){
        this.heartBeatManager.startHeartBeatManager();
    }

    /**
     * This method is used to notify the object to stop its {@link HeartBeatManager}
     */
    public void stopSendingHeartbeat() {
        this.heartBeatManager.stopHeartBeatManager();
    }

    /**
     * Getter for {@link MessageHandler} associated to the object
     * @return the {@link MessageHandler} associated to the object
     */
    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    /**
     * Getter for nickname of the player owning this "interface"
     * @return the name of the player who owns this "interface
     */
    public String getNickname(){
        return this.nickname;
    }

    /**
     * This method is used to connect to {@link MainServerTCP} and
     * register the nickname. It sends {@link NewUserMessage}.
     * @param nickname the nickname of the player
     */
    @Override
    public void connect(String nickname) {
        this.sendMessage(new NewUserMessage(nickname));
        this.heartBeatManager.startHeartBeatManager();
    }

    /**
     * This method is used to ask server to create a new game. It sends
     * {@link CreateNewGameMessage}.
     * @param gameName the name of the game that will be created.
     * @param numPlayers the number of players to play with.
     */
    @Override
    public void createGame(String gameName, int numPlayers) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, null));
    }

    /**
     * This method is used to ask server to create a new game with the specified seed.
     * It sends {@link CreateNewGameMessage}.
     * @param gameName the name of the game that will be created.
     * @param numPlayers the number of players for the game.
     * @param seed the seed for the game.
     */
    @Override
    public void createGame(String gameName, int numPlayers, int seed) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, Integer.toUnsignedLong(seed)));
    }

    /**
     * This method is used to ask server to join the game with the specified name.
     * It sends {@link JoinGameMessage}.
     * @param gameName The name of the game to join.
     */
    @Override
    public void joinGame(String gameName) {
        this.sendMessage(new JoinGameMessage(gameName, this.nickname));
    }

    /**
     * This method is used to ask server to join the first available game.
     * It sends {@link JoinFirstAvailableGameMessage}.
     */
    @Override
    public void joinFirstAvailableGame() {
        this.sendMessage(new JoinFirstAvailableGameMessage(this.nickname));
    }

    /**
     * This method is used to ask server to logout from the game player is currently registered to.
     * It clears, also, all the messages haven't been sent.
     * @throws RuntimeException if an error occurs while performing the requested action
     */
    @Override
    public void logoutFromGame() throws RuntimeException{
        if(!this.send(new RequestGameExitMessage(this.nickname))){
            throw new RuntimeException("Message could not be sent to server!");
        }

        synchronized (this.messagesToSend){
            this.messagesToSend.clear();
        }
    }

    /**
     * This method is used when client has to reconnect to server. It
     * works in all situations, including rebooting the machine.
     * @throws IllegalStateException when an error happened managing configuration
     */
    @Override
    public void reconnect() throws IllegalStateException{
        Configuration configuration;
        String nick;

        try{
            configuration = ConfigurationManager.retriveConfiguration(this.nickname);
        } catch (IOException | IllegalStateException e) {
            throw new IllegalStateException("[EXCEPTION]: could not reconnect due to: " + e);
        }

        try{
            this.socket.close();

            this.socket = new Socket(ClientSettings.TCP_SERVER_IP, ClientSettings.SERVER_TCP_PORT);
            synchronized (this.outputStreamLock) {
                this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            }
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());

            if(this.nickname != null){
                nick = this.nickname;
            }
            else{
                nick = configuration.getNick();
            }

            if(!this.send(new ReconnectToServerMessage(nick, configuration.getToken()))){
                throw new RuntimeException("Could not send message to server!");
            }
        }
        catch (IOException ioException){
            throw new RuntimeException("[IOException]: could not start new socket because of " + ioException.getMessage());
        }
    }

    /**
     * This method is used when client need to disconnect from server.
     * It sends {@link DisconnectMessage}.
     * @throws RuntimeException if an error occurs while trying to perform the requested action
     */
    @Override
    public void disconnect() throws RuntimeException{
        ConfigurationManager.deleteConfiguration(this.nickname);

        if(!this.send(new DisconnectMessage(this.nickname))){
            throw new RuntimeException("Message could not be sent to server!");
        }

        this.stopClient();
    }

    /**
     * This method is used to notify {@link ClientController} that
     * maybe a network error has occurred.
     */
    @Override
    public void signalPossibleNetworkProblem() {
        if(!this.clientController.isDisconnected()){
            this.clientController.signalPossibleNetworkProblem();
        }
        this.heartBeatManager.stopHeartBeatManager();
    }

    /**
     * This method is used to send {@link ClientHeartBeatMessage} to the
     * {@link MainServerTCP}.
     * @throws RuntimeException if an error has occurred while performing the action.
     */
    @Override
    public void sendHeartBeat() throws RuntimeException{
        if(!this.send(new ClientHeartBeatMessage(this.nickname))){
            throw new RuntimeException("Could not send heart beat message to server!");
        }
    }

    /**
     * This method is used to send a {@link PlaceCardMessage} to server.
     * @param cardToInsert the card that needs to be placed.
     * @param anchorCard the card from which to place the card.
     * @param directionToInsert the direction from the anchorCard in which player wants to place the card.
     * @param orientation the orientation in which player wants to place cardToInsert.
     */
    @Override
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) {
        this.sendMessage(new PlaceCardMessage(this.nickname, cardToInsert, anchorCard, directionToInsert, orientation));
    }

    /**
     * This method is used to send a {@link PlayerChatMessage} to server.
      * @param usersToSend a list containing the nickname of the users to whom player wants to send the message.
     * @param messageToSend the message we want to send.
     */
    @Override
    public void sendChatMessage(ArrayList<String> usersToSend, String messageToSend) {
        this.sendMessage(new PlayerChatMessage(new ArrayList<>(usersToSend), this.nickname, messageToSend));
    }

    /**
     * This method is used to send a {@link DirectionOfInitialCardMessage} to server
     * @param cardOrientation the orientation in which we want to place the initial card.
     */
    @Override
    public void placeInitialCard(CardOrientation cardOrientation) {
        this.sendMessage(new DirectionOfInitialCardMessage(this.nickname, cardOrientation));
    }

    /**
     * This method is used to send a {@link PickCardFromTableMessage} message to server.
     * @param type the type of card we want to pick {@link PlayableCardType}
     * @param position the position in the table we want to take the card from.
     */
    @Override
    public void pickCardFromTable(PlayableCardType type, int position) {
        this.sendMessage(new PickCardFromTableMessage(this.nickname, type, position));
    }

    /**
     * This method is used to send a {@link PickCardFromTableMessage} to server.
     * @param type the type of card we want to pick {@link PlayableCardType}.
     */
    @Override
    public void pickCardFromDeck(PlayableCardType type) {
        this.sendMessage(new PickCardFromDeckMessage(this.nickname, type));
    }

    /**
     * This method is used to send a {@link ChosenColorMessage} to server.
     * @param color the selected color.
     */
    @Override
    public void chooseColor(Color color) {
        this.sendMessage(new ChosenColorMessage(this.nickname, color));
    }

    /**
     * This method is used to send a {@link ChosenGoalCardMessage} to server
     * @param cardIdx which of the two proposed goal card we want to choose.
     */
    @Override
    public void choosePrivateGoalCard(int cardIdx) {
        this.sendMessage(new ChosenGoalCardMessage(this.nickname, cardIdx));
    }

    /**
     * This method is used to send a {@link RequestAvailableGamesMessage} to server.
     */
    @Override
    public void availableGames() {
        this.sendMessage(new RequestAvailableGamesMessage(this.nickname));
    }

    /**
     * This method is used to configure the "interface" (e.g. storing on
     * a separate JSON file the configuration) after {@link CreatedPlayerMessage}
     * has arrived
     * @param nick the nickname of the player
     * @param token the token associated to the client.
     */
    @Override
    public void configure(String nick, String token){
        this.nickname = nick;

        try {
            ConfigurationManager.saveConfiguration(new Configuration(nick, token, Configuration.ConnectionType.TCP));
        }
        catch (RuntimeException runtimeException){
            System.err.println("[CONFIG]: could not write config file. Skipping...");
        }
    }

}
