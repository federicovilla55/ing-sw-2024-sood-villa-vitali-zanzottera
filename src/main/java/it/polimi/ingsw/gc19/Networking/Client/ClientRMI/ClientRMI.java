package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Client.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.HeartBeatManager;
import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.NetworkManagementInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat.ServerHeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualMainServer;
import it.polimi.ingsw.gc19.View.GameLocalView.ActionParser;

import java.io.*;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a client using RMI for communication with the server.
 */
public class ClientRMI extends UnicastRemoteObject implements VirtualClient, ConfigurableClient, NetworkManagementInterface, GameManagementInterface {

    private final Registry registry;
    private VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;
    private final Object virtualGameServerLock;

    private final HeartBeatManager heartBeatManager;

    private String nickname;

    private final MessageHandler messageHandler;
    private final ActionParser actionParser;

    public ClientRMI(MessageHandler messageHandler, ActionParser actionParser) throws RuntimeException, RemoteException{
        super();

        try {
            this.registry = LocateRegistry.getRegistry("localhost");
            virtualMainServer = (VirtualMainServer) registry.lookup(ClientSettings.serverRMIName);
        }
        catch (RemoteException remoteException){
            throw new RuntimeException("[Remote Exception]: could not locate registry.");
        }
        catch (NotBoundException notBoundException){
            throw new RuntimeException("[Not Bound Exception]: could not lookup on registry.");
        }

        this.heartBeatManager = new HeartBeatManager(this);

        this.virtualGameServer = null;
        this.virtualGameServerLock = new Object();

        this.messageHandler = messageHandler;
        this.actionParser = actionParser;
    }

    @Override
    public void connect(String nickname){
        if(this.actionParser.isDisconnected()){
            return;
        }
        try{
            this.virtualMainServer.newConnection(this, nickname);
            this.heartBeatManager.startHeartBeatManager();
        }
        catch (RemoteException e) {
            this.actionParser.disconnect();
        }
    }

    @Override
    public void createGame(String gameName, int numPlayers){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void createGame(String gameName, int numPlayers, int seed){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers, seed);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void joinGame(String gameName){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.joinGame(this, gameName, this.nickname);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void joinFirstAvailableGame(){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.joinFirstAvailableGame(this, this.nickname);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void reconnect() throws IllegalStateException{
        Configuration clientConfig;
        String nick;

        try{
            clientConfig = ConfigurationManager.retriveConfiguration(this.nickname);
        }
        catch (IllegalStateException | IOException e){
            throw new IllegalStateException("[EXCEPTION]: could not reconnect due to: " + e);
        }

        int numOfTry = 0;

        try {
            virtualMainServer = (VirtualMainServer) this.registry.lookup(ClientSettings.serverRMIName);

            while(!Thread.currentThread().isInterrupted() && numOfTry < 10) {
                if(this.nickname != null){
                    nick = this.nickname;
                }
                else{
                    nick = clientConfig.getNick();
                }
                try {
                    if(this.nickname != null) {
                        this.virtualGameServer = this.virtualMainServer.reconnect(this, nick, clientConfig.getToken());
                    }
                    else{
                        this.virtualGameServer = this.virtualMainServer.reconnect(this, nick, clientConfig.getToken());
                    }
                    //@TODO: when correct message is arrived call startSendingHeartBeat
                    return;
                }
                catch (RemoteException remoteException){
                    numOfTry++;

                    try{
                        TimeUnit.MILLISECONDS.sleep(250);
                    }
                    catch (InterruptedException interruptedException){
                        Thread.currentThread().interrupt(); //This operation can be dangerous?
                        return;
                    }
                }
            }

            if(numOfTry == 10){
                throw new RuntimeException("[Remote Exception]: could not invoke remote method of RMI Server.");
            }
        }
        catch (NotBoundException e) {
            throw new RuntimeException("[Not Bound Exception]: could not invoke remote method of RMI Server.");
        }
        catch (RemoteException remoteException){
            throw new RuntimeException("[Remote Exception]: could not invoke remote method of RMI Server.");
        }
    }

    @Override
    public void logoutFromGame() throws RuntimeException{
        try {
            this.virtualMainServer.disconnectFromGame(this, this.nickname);
        }
        catch (RemoteException e) {
            throw new RuntimeException("Cannot disconnect from server because of Remote Exception: " + e);
        }
        synchronized (this.virtualGameServerLock){
            this.virtualGameServer = null;
        }
    }

    @Override
    public void disconnect() throws RuntimeException{
        try {
            this.virtualMainServer.disconnect(this, this.nickname);
        }
        catch (RemoteException e) {
            throw new RuntimeException("Cannot disconnect from server because of Remote Exception: " + e);
        }

        ConfigurationManager.deleteConfiguration(this.nickname);

        stopClient();
    }

    @Override
    public void signalPossibleNetworkProblem() {
        if(!this.actionParser.isDisconnected()) {
            this.actionParser.disconnect();
        }
        this.heartBeatManager.stopHeartBeatManager();
    }

    @Override
    public void sendHeartBeat() throws RuntimeException{
        try {
            this.virtualMainServer.heartBeat(this);
        }
        catch (RemoteException remoteException){
            throw new RuntimeException("Could not send heartbeat due to exception: " + remoteException.getClass());
        }
    }

    public void startSendingHeartbeat(){
        this.heartBeatManager.startHeartBeatManager();
    }

    public void stopSendingHeartbeat() {
        this.heartBeatManager.stopHeartBeatManager();
    }

    public void stopClient(){
        try{
            UnicastRemoteObject.unexportObject(this.registry, true);
        }
        catch (NoSuchObjectException ignored){ };

        stopSendingHeartbeat();
    }

    @Override
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if (this.virtualGameServer != null) {
                    this.virtualGameServer.placeCard(cardToInsert, anchorCard, directionToInsert, orientation);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if (this.virtualGameServer != null) {
                    this.virtualGameServer.sendChatMessage(UsersToSend, messageToSend);
                }
            } catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void placeInitialCard(CardOrientation cardOrientation){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if (this.virtualGameServer != null) {
                    this.virtualGameServer.placeInitialCard(cardOrientation);
                }
            } catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void pickCardFromTable(PlayableCardType type, int position){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.pickCardFromTable(type, position);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void pickCardFromDeck(PlayableCardType type){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.pickCardFromDeck(type);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void chooseColor(Color color){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.chooseColor(color);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void choosePrivateGoalCard(int cardIdx){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.choosePrivateGoalCard(cardIdx);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void availableGames() {
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualMainServer.requestAvailableGames(this, this.nickname);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public MessageHandler getMessageHandler(){
        return this.messageHandler;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public void pushUpdate(MessageToClient message) throws RemoteException {
        if(message instanceof ServerHeartBeatMessage){ //How to not use instance of? MessageHandler has to open the message
            this.heartBeatManager.heartBeat();
        }
        else{
            if(this.nickname == null || message.getHeader() == null || message.getHeader().contains(this.nickname)){
                this.messageHandler.update(message);
            }
        }
    }

    @Override
    public void configure(String nick, String token) {
        this.nickname = nick;

        try {
            ConfigurationManager.saveConfiguration(new Configuration(nick, token, Configuration.ConnectionType.RMI));
        }
        catch (RuntimeException runtimeException){
            System.err.println("[CONFIG]: could not write config file. Skipping...");
        }
    }

}