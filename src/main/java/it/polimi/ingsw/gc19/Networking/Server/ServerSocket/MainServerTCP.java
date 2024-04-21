package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Model.Triplet;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Server;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToServer;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class MainServerTCP extends Server implements ObserverMessageToServer<MessageToServer> {

    private final HashMap<Socket, Triplet<ClientHandlerSocket, MessageToServerDispatcher, String>> connectedClients;
    private final ConcurrentHashMap<Socket, Long> lastHeartBeatOfClients;
    private final MessageToMainServerVisitor messageHandler;
    private final HeartBeatMessageToServerVisitor heartBeatHandler;

    public MainServerTCP(){
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new ConcurrentHashMap<>();
        this.messageHandler = new MessageToMainServerVisitor();
        this.heartBeatHandler = new HeartBeatMessageToServerVisitor();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::runHeartBeatTesterForClient, 0, Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS*1000 / 5, TimeUnit.MILLISECONDS);
    }

    @Override
    public void update(Socket senderSocket, MessageToServer message) {
        if(message instanceof HeartBeatMessage) {
            synchronized (heartBeatHandler) {
                this.heartBeatHandler.handleMessageToMainServer(senderSocket, message);
            }
            return;
        }
        synchronized (this.messageHandler) {
            this.messageHandler.handleMessageToMainServer(senderSocket, message);
        }
    }

    @Override
    public boolean accept(MessageToServer message) {
        return message instanceof GameHandlingMessage || message instanceof HeartBeatMessage;
    }

    /**
     * This method is called by TCPConnectionAcceptor to notify that a client has only connected but has sent nothing
     * @param socket
     */
    public void registerSocket(Socket socket, MessageToServerDispatcher messageToServerDispatcher){
        ClientHandlerSocket clientHandlerSocket;
        synchronized (this.connectedClients){
            if(!this.connectedClients.containsKey(socket)) {
                //At the beginning we don't have enough infos to generate secret code, and we don't want to open client socket,
                //so we put socket in hashmap with only client handler socket with output enabled

                clientHandlerSocket = new ClientHandlerSocket(socket);

                //starting sender thread
                clientHandlerSocket.start();

                messageToServerDispatcher.attachObserver(clientHandlerSocket);
                this.connectedClients.put(socket, new Triplet<>(clientHandlerSocket, messageToServerDispatcher, null));

                //At the beginning with socket we can only send messages, not receive them
                //clientHandlerSocket.startWritingMessages();
                //clientHandlerSocket.stopReadingMessages();

                synchronized (lastHeartBeatOfClients) {
                    lastHeartBeatOfClients.put(socket, new Date().getTime());
                }
            }
            else{
                this.connectedClients.get(socket).x().sendMessageToClient(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                                                                "Your socket is already registered in server!")
                                                                                  .setHeader(this.connectedClients.get(socket).x().getUsername()));
            }
        }
    }

    public ClientHandlerSocket getClientHandlerFromSocket(Socket socket, String nick){
        synchronized (this.connectedClients){
            if(!this.connectedClients.containsKey(socket)){
                //@TODO: there can be this case?
                return null;
            }
            if(this.connectedClients.get(socket).z() == null){
                this.connectedClients.get(socket).x().sendMessageToClient(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                                                              "Your player is not registered to server! Please register")
                                                                                .setHeader(nick));
                return null;
            }
            return this.connectedClients.get(socket).x();
        }
    }

    public void closeSocket(Socket socket){
        try{
            socket.close();
        }
        catch (IOException ioException){
            throw  new RuntimeException(ioException);
        }
    }

    private void runHeartBeatTesterForClient(){
        String playerName;
        ArrayList<Socket> socketToRemove = new ArrayList<>();
        synchronized (this.lastHeartBeatOfClients){
            for (Socket socket : this.lastHeartBeatOfClients.keySet()) {
                if (new Date().getTime() - this.lastHeartBeatOfClients.get(socket) > 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS) {
                    System.out.println("disconnecting -> " + socket + " because " + new Date().getTime() + "   " + this.lastHeartBeatOfClients.get(socket));
                    //this.lastHeartBeatOfClients.remove(socket);
                    socketToRemove.add(socket);
                }
            }
            lastHeartBeatOfClients.keySet().removeAll(socketToRemove);

            synchronized (this.connectedClients) {
                for (Socket socket : socketToRemove) {
                    //if(!this.connectedClients.containsKey(socket)) continue;
                    playerName = this.connectedClients.get(socket).x().getUsername();
                    System.out.println(playerName);
                    if (playerName != null) {
                        System.out.println("Il player " + playerName + " è ora inattivo.");
                        this.mainController.setPlayerInactive(playerName);
                    }
                }
            }

        }
    }

    public void resetMainServer() {
        synchronized (connectedClients) {
            this.connectedClients.clear();
        }
        synchronized (lastHeartBeatOfClients) {
            this.lastHeartBeatOfClients.clear();
        }
        this.mainController.resetMainController();
    }

    public void killClientHandlers(){
        synchronized (this.connectedClients) {
            for (var c : this.connectedClients.values()) {
                c.x().interruptClientHandler();
                c.y().interruptMessageDispatcher();
            }
        }
    }

    private class MessageToMainServerVisitor implements GameHandlingMessageVisitor, MessageToServerVisitor{

        private Socket clientSocket;
        private String nickname;

        public void handleMessageToMainServer(Socket socket, MessageToServer message){
            this.clientSocket = socket;
            this.nickname = message.getNickname();
            //System.out.println(this.nickname + " " + message.getClass());
            message.accept(this);
        }

        @Override
        public void visit(CreateNewGameMessage message) {
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket, this.nickname);
            System.out.println(nickname + " -> " + (clientHandlerSocket != null));
            if(clientHandlerSocket != null){
                System.out.println(mainController.createGame(message.getGameName(), message.getNumPlayer(), clientHandlerSocket, message.getRandomSeed()));
                synchronized (connectedClients){
                    //connectedClients.get(clientSocket).x().startReadingMessages();
                    //connectedClients.get(clientSocket).x().startWritingMessages();
                }
            }
        }

        @Override
        public void visit(NewUserMessage message) {
            System.out.println("ARRIVATO MESSAGGIO");
            ClientHandlerSocket clientHandlerSocket;

            synchronized (connectedClients){
                /* FIXME */
                /*if(!connectedClients.containsKey(clientSocket)){
                    //@TODO: is it necessary? Can cause problem if a player disconnects and then creates a new player
                    return;
                }*/
                if(connectedClients.get(clientSocket).z() != null){
                    //per eliminare il socket serve una disconnessione esplicita
                    connectedClients.get(clientSocket).x().sendMessageToClient(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                                                                     "Your socket client is already connected to server!")
                                                                                       .setHeader(this.nickname));
                    return;
                }
                else{
                    clientHandlerSocket = connectedClients.get(clientSocket).x();
                }
            }

            clientHandlerSocket.setUsername(nickname);
            boolean answer = mainController.createClient(clientHandlerSocket);
            System.out.println("need to create player -> " + nickname + " answer from main controller " + answer);

            if(answer){
                //clientHandlerSocket.startWritingMessages();
                //clientHandlerSocket.stopReadingMessages();
                String hashedMessage = computeHashOfClientHandler(clientHandlerSocket, nickname);
                System.out.println("created player ->" + nickname);
                synchronized (connectedClients){
                    connectedClients.put(clientSocket, new Triplet<>(clientHandlerSocket, connectedClients.get(clientSocket).y(), hashedMessage));
                }

                clientHandlerSocket.sendMessageToClient(new CreatedPlayerMessage(nickname, hashedMessage).setHeader(nickname));
            }
            else{
                clientHandlerSocket.setUsername(null);
            }
        }

        @Override
        public void visit(ReconnectToServerMessage message) {
            Socket socketBefore = null;
            boolean found = false;

            synchronized (connectedClients){
                for(var v : connectedClients.entrySet()){
                    if(v.getValue().z() != null && v.getValue().z().equals(message.getToken())) {
                        System.out.println("FOUND CLIENT1 " + mainController.isPlayerActive(nickname) + "  " + nickname);
                        socketBefore = v.getKey();

                        if(mainController.isPlayerActive(nickname)){
                            System.out.println("trying to reconnect from " + socketBefore + " from client " + nickname);
                            connectedClients.get(clientSocket).x().sendMessageToClient(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                                                                             "You are trying to reconnect a client that is already connected to sever!")
                                                                                               .setHeader(nickname));
                            return;
                        }

                        found = true;
                        break;
                    }
                }
            }

            if(found){

                if(!socketBefore.equals(clientSocket)){
                    synchronized (connectedClients) {
                        System.out.println(" -------------------- " + nickname + "  -> " + connectedClients.get(socketBefore).x().canWrite());
                        connectedClients.get(clientSocket).x().pullClientHandlerSocketConfigIntoThis(connectedClients.get(socketBefore).x());
                        connectedClients.put(clientSocket, new Triplet<>(connectedClients.get(clientSocket).x(), connectedClients.get(clientSocket).y(), connectedClients.get(socketBefore).z()));
                        connectedClients.remove(socketBefore);
                    }
                }

                synchronized (lastHeartBeatOfClients) {
                    if(nickname.equals("client1")) System.out.println("recoonection of client1");
                    lastHeartBeatOfClients.put(clientSocket, new Date().getTime());
                }

                synchronized (connectedClients){
                    if(connectedClients.get(clientSocket).x().getUsername() != null){
                        System.out.println("okkkk");
                        mainController.reconnect(connectedClients.get(clientSocket).x());
                    }
                }
            }
            else{
                synchronized (connectedClients){
                    connectedClients.get(clientSocket).x().sendMessageToClient(new GameHandlingError(Error.COULD_NOT_RECONNECT,
                                                                                                     "Can't perform reconnection!")
                                                                                       .setHeader(nickname));
                }

                /*synchronized (lastHeartBeatOfClients) {
                    if(nickname.equals("client1")) System.out.println("recoonection of client1");
                    lastHeartBeatOfClients.put(clientSocket, new Date().getTime());
                }*/

            }
        }

        @Override
        public void visit(DisconnectMessage message) {
            synchronized (connectedClients){
                if(connectedClients.containsKey(clientSocket)) {
                    //connectedClients.get(clientSocket).x().stopWritingMessages();
                    //connectedClients.get(clientSocket).x().stopReadingMessages();
                    var clientToDisconnect = connectedClients.remove(clientSocket);

                    if (clientToDisconnect.z() != null) {
                        mainController.disconnect(clientToDisconnect.x());
                    }
                    System.out.println("disconnected player -> " + clientToDisconnect.x());
                    //Killing thread responsible for sending messages
                    clientToDisconnect.x().interruptClientHandler();
                    //Killing thread in message dispatcher
                    clientToDisconnect.y().interruptMessageDispatcher();
                    closeSocket(clientSocket);
                }
            }
            synchronized (lastHeartBeatOfClients) {
                lastHeartBeatOfClients.remove(clientSocket);
            }
        }

        @Override
        public void visit(JoinGameMessage message) {
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket, nickname);
            if(clientHandlerSocket != null){
                if(mainController.registerToGame(clientHandlerSocket, message.getGameName())) {
                    synchronized (connectedClients) {
                        //System.out.println(message.getNickname() + " -> " + message.getGameName() + "  " + connectedClients.get(clientSocket).x().getGameController());
                        //connectedClients.get(clientSocket).x().startReadingMessages();
                        //connectedClients.get(clientSocket).x().startWritingMessages();
                    }
                }
            }
        }

        @Override
        public void visit(JoinFirstAvailableGameMessage message) {
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket, nickname);
            if(clientHandlerSocket != null){
                if(mainController.registerToFirstAvailableGame(clientHandlerSocket) != null) {
                    synchronized (connectedClients) {
                        //System.out.println(message.getNickname() + " -> " + message.getGameName() + "  " + connectedClients.get(clientSocket).x().getGameController());
                        //connectedClients.get(clientSocket).x().startReadingMessages();
                        //connectedClients.get(clientSocket).x().startWritingMessages();
                    }
                }
            }
        }

    }

    private class HeartBeatMessageToServerVisitor implements MessageToServerVisitor, HeartBeatMessageVisitor{

        private Socket clientSocket;

        public void handleMessageToMainServer(Socket socket, MessageToServer message){
            this.clientSocket = socket;
            //System.out.println(this.nickname + " " + message.getClass());
            message.accept(this);
        }

        @Override
        public void visit(HeartBeatMessage message) {
            synchronized (lastHeartBeatOfClients) {
                if (lastHeartBeatOfClients.containsKey(clientSocket)) {
                    Long last = lastHeartBeatOfClients.get(clientSocket);
                    Long curr = new Date().getTime();
                    System.err.println("heartbeat from " + message.getNickname() + "   " + clientSocket + " -> last " + last + " now " + curr);
                    lastHeartBeatOfClients.put(clientSocket, curr);
                }
            }
        }
    }

}
