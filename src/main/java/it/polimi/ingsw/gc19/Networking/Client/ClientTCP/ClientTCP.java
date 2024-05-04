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
import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.NetworkManagementInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat.ServerHeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClientTCP implements ConfigurableClient, NetworkManagementInterface, GameManagementInterface {
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

    public ClientTCP(MessageHandler messageHandler, ClientController clientController) throws IOException{
        this.messageHandler = messageHandler;
        this.clientController = clientController;

        try {
            this.socket = new Socket(ClientSettings.serverIP, ClientSettings.serverTCPPort);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            //Method throws IOException to notify Action Parsers that connection establishing have gone wrong
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

    private boolean send(MessageToServer message){
        synchronized (this.outputStreamLock) {
            try {
                this.outputStream.writeObject(message);
                this.finalizeSending();
            }
            catch (IOException ioException) {
                System.out.println("+++" + ioException.getMessage());
                return false;
            }
        }
        return true;
    }

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
                    this.clientController.disconnect();
                }
            }
        }
    }

    public void sendMessage(MessageToServer message){
        synchronized (this.messagesToSend){
            this.messagesToSend.addLast(message);
            this.messagesToSend.notifyAll();
        }
    }

    private void finalizeSending() throws IOException{
        this.outputStream.flush();
        this.outputStream.reset();
    }

    public void receiveMessages(){
        MessageToClient incomingMessage = null;
        while(!Thread.interrupted()) {
            try {
                incomingMessage = (MessageToClient) this.inputStream.readObject();
                //System.out.println(incomingMessage);
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

    public void startSendingHeartbeat(){
        this.heartBeatManager.startHeartBeatManager();
    }

    public void stopSendingHeartbeat() {
        this.heartBeatManager.stopHeartBeatManager();
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    public String getNickname(){
        return this.nickname;
    }

    @Override
    public void connect(String nickname) {
        this.sendMessage(new NewUserMessage(nickname));
        this.heartBeatManager.startHeartBeatManager();
    }

    @Override
    public void createGame(String gameName, int numPlayers) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, null));
    }

    @Override
    public void createGame(String gameName, int numPlayers, int seed) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, Integer.toUnsignedLong(seed)));
    }

    @Override
    public void joinGame(String gameName) {
        this.sendMessage(new JoinGameMessage(gameName, this.nickname));
    }

    @Override
    public void joinFirstAvailableGame() {
        this.sendMessage(new JoinFirstAvailableGameMessage(this.nickname));
    }

    @Override
    public void logoutFromGame() throws RuntimeException{
        if(!this.send(new RequestGameExitMessage(this.nickname))){
            throw new RuntimeException("Message could not be sent to server!");
        }
        synchronized (this.messagesToSend){
            this.messagesToSend.clear();
        }
    }

    @Override
    public void reconnect() throws IllegalStateException{
        Configuration configuration;
        String nick;

        try{
            configuration = ConfigurationManager.retriveConfiguration(this.nickname);
        } catch (IOException | IllegalStateException e) {
            throw new IllegalStateException("[EXCEPTION]: could not reconnect due to: " + e);
        }

        int numOfTry = 0;

        try{
            this.socket.close();

            this.socket = new Socket(ClientSettings.serverIP, ClientSettings.serverTCPPort);
            synchronized (this.outputStreamLock) {
                this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            }
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());

            while(!Thread.currentThread().isInterrupted() && numOfTry < 10){
                if(this.nickname != null){
                    nick = this.nickname;
                }
                else{
                    nick = configuration.getNick();
                }
                if(!this.send(new ReconnectToServerMessage(nick, configuration.getToken()))){
                    numOfTry++;
                    try{
                        TimeUnit.MILLISECONDS.sleep(250);
                    }
                    catch (InterruptedException interruptedException){
                        Thread.currentThread().interrupt();
                        return;
                    }
                    //@TODO: when correct message is arrived call startSendingHeartBeat
                }
                else{
                    return;
                }
            }

            if(numOfTry == 10){
                throw new RuntimeException("Could not send reconnection message to server!");
            }
        }
        catch (IOException ioException){
            throw new RuntimeException("[IOException]: could not start new socket because of " + ioException.getMessage());
        }
    }

    @Override
    public void disconnect() throws RuntimeException{
        ConfigurationManager.deleteConfiguration(this.nickname);

        if(!this.send(new DisconnectMessage(this.nickname))){
            throw new RuntimeException("Message could not be sent to server!");
        }

        this.stopClient();
    }

    @Override
    public void signalPossibleNetworkProblem() {
        if(!this.clientController.isDisconnected()){
            this.clientController.disconnect();
        }
        this.heartBeatManager.stopHeartBeatManager();
    }

    @Override
    public void sendHeartBeat() throws RuntimeException{
        if(!this.send(new ClientHeartBeatMessage(this.nickname))){
            throw new RuntimeException("Could not send heart beat message to server!");
        }
    }

    @Override
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) {
        this.sendMessage(new PlaceCardMessage(this.nickname, cardToInsert, anchorCard, directionToInsert, orientation));
    }

    @Override
    public void sendChatMessage(ArrayList<String> usersToSend, String messageToSend) {
        this.sendMessage(new PlayerChatMessage(new ArrayList<>(usersToSend), this.nickname, messageToSend));
    }

    @Override
    public void placeInitialCard(CardOrientation cardOrientation) {
        this.sendMessage(new DirectionOfInitialCardMessage(this.nickname, cardOrientation));
    }

    @Override
    public void pickCardFromTable(PlayableCardType type, int position) {
        this.sendMessage(new PickCardFromTableMessage(this.nickname, type, position));
    }

    @Override
    public void pickCardFromDeck(PlayableCardType type) {
        this.sendMessage(new PickCardFromDeckMessage(this.nickname, type));
    }

    @Override
    public void chooseColor(Color color) {
        this.sendMessage(new ChosenColorMessage(this.nickname, color));
    }

    @Override
    public void choosePrivateGoalCard(int cardIdx) {
        this.sendMessage(new ChosenGoalCardMessage(this.nickname, cardIdx));
    }

    @Override
    public void availableGames() {
        this.sendMessage(new RequestAvailableGamesMessage(this.nickname));
    }

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
