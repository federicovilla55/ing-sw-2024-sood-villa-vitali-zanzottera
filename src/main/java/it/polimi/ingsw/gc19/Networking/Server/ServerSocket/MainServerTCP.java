package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Utils.Triplet;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Server;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToServer;
import it.polimi.ingsw.gc19.Controller.MainController;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * This class is the TCP main server. It extends {@link Server} and implements {@link ObserverMessageToServer<MessageToServer>}.
 * It can be instanced only once (singleton pattern). It receives {@link MessageToServer}
 * from {@link MessageToServerDispatcher} concerning game handling (e.g. create new game, register to game...)
 * and heart beat message.
 */
public class MainServerTCP extends Server implements ObserverMessageToServer<MessageToServer>{

    private static MainServerTCP instance = null;
    private final HashMap<Socket, Triplet<ClientHandlerSocket, MessageToServerDispatcher, String>> connectedClients;
    private final ConcurrentHashMap<Socket, Long> lastHeartBeatOfClients;
    private final MessageToMainServerVisitor gameHandlingMessageHandler;
    private final HeartBeatMessageToServerVisitor heartBeatHandler;

    private MainServerTCP(){
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new ConcurrentHashMap<>();
        this.gameHandlingMessageHandler = new MessageToMainServerVisitor();
        this.heartBeatHandler = new HeartBeatMessageToServerVisitor();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::runHeartBeatTesterForClient, 0, Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS * 1000 / 10, TimeUnit.MILLISECONDS);
    }

    /**
     * This method is used to implement Singleton pattern for {@link MainServerTCP}.
     * @return {@link MainServerTCP} instance.
     */
    public static MainServerTCP getInstance(){
        if(instance == null){
            instance = new MainServerTCP();
        }
        return instance;
    }

    /**
     * This method is used by {@link MessageToServerDispatcher} to notify TCP server that
     * a new message has arrived and hence has to be opened. Thread executing this method
     * will wait until {@link Socket} from which it's receiving messages is put inside
     * connected clients hashmap.
     * @param senderSocket {@link Socket} from which message has arrived
     * @param message {@link MessageToServer} arrived
     */
    @Override
    public void update(Socket senderSocket, MessageToServer message) {
        if(this.heartBeatHandler.canAccept(message)) {
            synchronized (this.heartBeatHandler) {
                this.heartBeatHandler.handleMessageToMainServer(senderSocket, message);
            }
        }
        else {
            if (this.gameHandlingMessageHandler.canAccept(message)) {
                synchronized (this.connectedClients){
                    while(!this.connectedClients.containsKey(senderSocket)){
                        try{
                            this.connectedClients.wait();
                        }
                        catch (InterruptedException interruptedException){
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                synchronized (this.gameHandlingMessageHandler) {
                    this.gameHandlingMessageHandler.handleMessageToMainServer(senderSocket, message);
                }
                return;
            }
            System.err.println("[ERROR] Main TCP Server could not accept message of class " + message.getClass() + " received from socket " + senderSocket);
        }
    }

    /**
     * This method tells caller that message can be accepted by the object
     * @param message {@link MessageToServer} that could be accepted
     * @return true if and only if message can be accepted
     */
    @Override
    public boolean accept(MessageToServer message) {
        return message instanceof GameHandlingMessage || message instanceof HeartBeatMessage;
    }

    /**
     * This method is called by TCPConnectionAcceptor to notify that a client has only connected but has sent nothing
     * @param socket {@link Socket} is the client's socket
     */
    public void registerSocket(Socket socket, MessageToServerDispatcher messageToServerDispatcher){
        ClientHandlerSocket clientHandlerSocket;

        synchronized (this.connectedClients){
            if(!this.connectedClients.containsKey(socket)) {
                try {
                    clientHandlerSocket = new ClientHandlerSocket(socket);

                    clientHandlerSocket.start();

                    messageToServerDispatcher.attachObserver(clientHandlerSocket);
                    this.connectedClients.put(socket, new Triplet<>(clientHandlerSocket, messageToServerDispatcher, null));
                    this.connectedClients.notifyAll();

                    lastHeartBeatOfClients.put(socket, new Date().getTime());
                }
                catch (IOException ioException){
                    System.err.println("[EXCEPTION] IOException occurred while trying to build object output stream from socket " + socket + ". Closing socket...");
                    scheduleSocketClosing(socket);
                }
            }
        }
    }

    /**
     * This method is used to find {@link ClientHandlerSocket} from socket. If socket
     * is not registered inside connected clients hashmap it returns null. If socket
     * is registered but its client handler hasn't any token it sends to client a
     * {@link NetworkHandlingErrorMessage} (with <code>NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER</code>)
     * @param socket socket to search for inside connected clients hash map
     * @return {@link ClientHandlerSocket} is socket has his client handler and client has already sent a
     * {@link NewUserMessage} otherwise returns null.
     */
    private ClientHandlerSocket getClientHandlerFromSocket(Socket socket){
        synchronized (this.connectedClients) {
            if(!this.connectedClients.containsKey(socket)){
                return null;
            }
            if (this.connectedClients.get(socket).z() == null) {
                this.connectedClients.get(socket).x().update(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                                                             "Your player is not registered to server! Please register...")
                                                                     .setHeader(this.connectedClients.get(socket).x().getUsername()));
                return null;
            }
            return this.connectedClients.get(socket).x();
        }
    }

    /**
     * This method is used to close {@param socket}.
     * @param socket socket to close.
     */
    private void closeSocket(Socket socket){
        try{
            if(socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        catch (IOException ioException){
            System.err.println("[IOException] IOException occurred while trying to close socket " + socket + ". Skipping...");
        }
    }

    /**
     * This method is used to schedule {@param socket} closure. It tries to shut down both input
     * and output of {@param socket}. If this cannot be done within a certain time it closes
     * socket with {@link MainServerTCP#closeSocket(Socket)}
     * @param socket socket to close
     */
    public void scheduleSocketClosing(Socket socket){
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        ExecutorService scheduledClose = Executors.newSingleThreadExecutor();

        timer.schedule(() -> {
            closeSocket(socket);
            scheduledClose.shutdownNow();
        }, 250, TimeUnit.MILLISECONDS);

        scheduledClose.submit(() -> {
            while (!socket.isInputShutdown() || !socket.isOutputShutdown()) { };
            closeSocket(socket);
            timer.shutdownNow();
        });

    }

    /**
     * This method is used to check heart beat timing of connected clients.
     * If client do not send heart beat message for more than <code>Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS</code>
     * it deletes it from <code>lastHeartBeatOfClients</code> hashmap, it sets it to inactive
     * using {@link MainController#setPlayerInactive(String)}. It doesn't remove it from <code>connectedClients</code>
     * hashmap because client can reconnect and thus it is necessary to keep its private token.
     */
    @Override
    protected void runHeartBeatTesterForClient(){
        String playerName;
        ArrayList<Socket> socketToRemove = new ArrayList<>();

        for (Socket socket : this.lastHeartBeatOfClients.keySet()) {
            if (new Date().getTime() - this.lastHeartBeatOfClients.get(socket) > 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS) {
                socketToRemove.add(socket);
            }
        }

        lastHeartBeatOfClients.keySet().removeAll(socketToRemove);

        synchronized (this.connectedClients) {
            for (Socket socket : socketToRemove) {
                if(connectedClients.containsKey(socket)){
                    playerName = this.connectedClients.get(socket).x().getUsername();
                    if (playerName != null) {
                        this.mainController.setPlayerInactive(playerName);
                    }
                }
            }
        }

    }

    /**
     * This method is used to reset main TCP server and main controller.
     */
    @Override
    public void resetServer() {
        synchronized (connectedClients) {
            this.connectedClients.clear();
        }
        synchronized (lastHeartBeatOfClients) {
            this.lastHeartBeatOfClients.clear();
        }
        this.mainController.resetMainController();
    }

    /**
     * This method is used to interrupt clients' handler and message dispatcher thread
     * It removes them as observers from {@link MessageToServerDispatcher}.
     */
    @Override
    public void killClientHandlers(){
        Set<Tuple<ClientHandlerSocket, MessageToServerDispatcher>> removingObservers = new HashSet<>();
        synchronized (this.connectedClients) {
            for (var c : this.connectedClients.entrySet()) {
                removingObservers.add(new Tuple<>(c.getValue().x(), c.getValue().y()));

                c.getValue().x().interruptClientHandler();
                c.getValue().y().interruptMessageDispatcher();

                closeSocket(c.getKey());
            }
        }

        for(var r : removingObservers){
            r.y().removeObserver(this);
            r.y().removeObserver(r.x());
        }
    }

    private class MessageToMainServerVisitor implements GameHandlingMessageVisitor, MessageToServerVisitor{
        private Socket clientSocket;
        private String nickname;

        /**
         * This method is used by the caller to know if {@param message} can
         * be accepted by thi class
         * @param message {@link MessageToServer} to check
         * @return <code>true</code> if {@param message} can be accepted by this class
         */
        public boolean canAccept(MessageToServer message){
            return message instanceof GameHandlingMessage;
        }

        /**
         * This method is the entry point for class {@link MessageToMainServerVisitor}
         * @param socket {@link Socket} associated to the client
         * @param message {@link MessageToServer} to handle
         */
        public void handleMessageToMainServer(Socket socket,MessageToServer message){
            this.clientSocket = socket;
            this.nickname = message.getNickname();
            message.accept(this);
        }

        /**
         * This method is used to visit {@link CreateNewGameMessage} received from client.
         * It searches for {@link ClientHandlerSocket} associated to {@param socket}. If exists,
         * it calls {@link MainController} to effectively build a new game.
         * @param message {@link MessageToServer} to handle
         */
        @Override
        public void visit(CreateNewGameMessage message) {
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket);
            if(clientHandlerSocket != null){
                if(message.getRandomSeed() != null) {
                    mainController.createGame(message.getGameName(), message.getNumPlayer(), clientHandlerSocket, message.getRandomSeed());
                }
                else{
                    mainController.createGame(message.getGameName(), message.getNumPlayer(), clientHandlerSocket, Math.abs(new Random().nextLong()));
                }
            }
        }

        /**
         * This method is used to visit {@link NewUserMessage} received from client.
         * It checks that client is new to server (e.g. it is in <code>connectedClients</code> hashmap,
         * and it has a not <code>null</code> token). If yes, it calls {@link MainController} to build
         * the player, compute its private token and send to client a new
         * {@link CreatedPlayerMessage}. If no, it closes player's socket
         * @param message {@link NewUserMessage} to handle
         */
        @Override
        public void visit(NewUserMessage message) {
            ClientHandlerSocket clientHandlerSocket;

            synchronized (connectedClients) {
                if(connectedClients.containsKey(clientSocket)){
                    if (connectedClients.get(clientSocket).z() != null) {
                        connectedClients.get(clientSocket).x().update(new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                                                                                   "Your socket client is already connected to server!")
                                                                                           .setHeader(connectedClients.get(clientSocket).x().getUsername()));
                        return;
                    }
                    else {
                        clientHandlerSocket = connectedClients.get(clientSocket).x();
                    }

                    clientHandlerSocket.setUsername(nickname);

                    if (mainController.createClient(clientHandlerSocket)) {
                        String hashedMessage = computeHashOfClientHandler(clientHandlerSocket, nickname);
                        connectedClients.put(clientSocket, new Triplet<>(clientHandlerSocket, connectedClients.get(clientSocket).y(), hashedMessage));
                        connectedClients.notifyAll();

                        clientHandlerSocket.update(new CreatedPlayerMessage(nickname, hashedMessage).setHeader(connectedClients.get(clientSocket).x().getUsername()));
                    }
                }
                else{
                    closeSocket(clientSocket); //Maybe write message?
                }
            }
        }

        /**
         * This method is used to reconnect a client to the server.
         * First, it checks if client is already active (e.g. it's constantly sending heartbeats).
         * If ye, it sends to it a {@link NetworkHandlingErrorMessage}. If no, it checks if in
         * <code>connectedClients</code> there is a {@link Socket} with the same token. If exists,
         * it closes the previous socket, interrupts previous {@link ClientHandlerSocket} and {@link MessageToServerDispatcher},
         * builds new {@link ClientHandlerSocket} and notifies {@link MainController} that a player
         * has to be reconnected.
         * @param message {@link ReconnectToServerMessage} to handle
         */
        @Override
        public void visit(ReconnectToServerMessage message) {
            Socket socketBefore = null;
            boolean found = false;

            synchronized (connectedClients) {
                for (var v : connectedClients.entrySet()) {
                    if (v.getValue().z() != null && v.getValue().z().equals(message.getToken())) {
                        socketBefore = v.getKey();

                        if (mainController.isPlayerActive(nickname)) {
                            connectedClients.get(clientSocket).x().update(new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                                                                          "You are trying to reconnect a client that is already connected to sever!")
                                                                                  .setHeader(connectedClients.get(clientSocket).x().getUsername()));
                            return;
                        }

                        found = true;
                        break;
                    }
                }

                if (found) {

                    if (!socketBefore.equals(clientSocket)) {

                        closeSocket(socketBefore); //close

                        Triplet<ClientHandlerSocket, MessageToServerDispatcher, String> clientBeforeToRemove;
                        clientBeforeToRemove = connectedClients.remove(socketBefore);

                        //if (clientBeforeToRemove != null) {
                        clientBeforeToRemove.y().removeObserver(MainServerTCP.this);
                        clientBeforeToRemove.y().removeObserver(clientBeforeToRemove.x());
                        clientBeforeToRemove.y().interruptMessageDispatcher();
                        clientBeforeToRemove.y().interruptMessageDispatcher();

                        //synchronized (connectedClients) {
                        connectedClients.get(clientSocket).x().pullClientHandlerSocketConfigIntoThis(clientBeforeToRemove.x());
                        connectedClients.put(clientSocket, new Triplet<>(connectedClients.get(clientSocket).x(), connectedClients.get(clientSocket).y(), clientBeforeToRemove.z()));
                        connectedClients.notifyAll();

                        connectedClients.remove(socketBefore);
                        //}
                    }

                    lastHeartBeatOfClients.put(clientSocket, new Date().getTime());

                    if (connectedClients.get(clientSocket).x().getUsername() != null) {
                        mainController.reconnect(connectedClients.get(clientSocket).x());
                    }
                }
                else {
                    connectedClients.get(clientSocket).x().update(new NetworkHandlingErrorMessage(NetworkError.COULD_NOT_RECONNECT,
                                                                                                  "Can't perform reconnection!")
                                                                          .setHeader(connectedClients.get(clientSocket).x().getUsername()));
                }
            }
        }

        /**
         * This method is used to handle {@link DisconnectMessage} received from player.
         * It removes socket associated to the client who sent the message from both
         * <code>connectedClients</code> and <code>lastHeartBeatOfClients</code>, notify {@link MainController}
         * that a client has to be disconnected and, lastly, it interrupts {@link ClientHandlerSocket}
         * and {@link MessageToServerDispatcher} thread.
         * @param message {@link DisconnectMessage} to handle
         *
         */
        @Override
        public void visit(DisconnectMessage message) {
            Triplet<ClientHandlerSocket, MessageToServerDispatcher, String> clientToDisconnect;

            synchronized (connectedClients){
                clientToDisconnect = connectedClients.remove(clientSocket);
            }

            if (clientToDisconnect != null && clientToDisconnect.z() != null){
                mainController.disconnect(clientToDisconnect.x());

                clientToDisconnect.y().removeObserver(clientToDisconnect.x());
                clientToDisconnect.y().removeObserver(MainServerTCP.this);

                clientToDisconnect.x().interruptClientHandler();
                clientToDisconnect.y().interruptMessageDispatcher();

                closeSocket(clientSocket);
            }

            lastHeartBeatOfClients.remove(clientSocket);
        }

        /**
         * This method is used to handle {@link JoinGameMessage} received from client.
         * First, it checks if client is registered to server using {@link MainServerTCP#getClientHandlerFromSocket(Socket)}.
         * If yes, it notifies {@link MainController} to register {@link ClientHandlerSocket} to
         * the specified game if possible.
         * @param message {@link JoinGameMessage} to handle.
         */
        @Override
        public void visit(JoinGameMessage message) {
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket);
            if(clientHandlerSocket != null){
                mainController.registerToGame(clientHandlerSocket, message.getGameName());
            }
        }

        /**
         * This method is used to handle {@link JoinFirstAvailableGameMessage}.
         * First, it checks if client is registered to server using {@link MainServerTCP#getClientHandlerFromSocket(Socket)}.
         * If yes, it notifies {@link MainController} that a player would
         * like to join first available game.
         * @param message {@link JoinFirstAvailableGameMessage} to handle.
         */
        @Override
        public void visit(JoinFirstAvailableGameMessage message) {
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket);
            if(clientHandlerSocket != null){
                mainController.registerToFirstAvailableGame(clientHandlerSocket);
            }
        }

        /**
         * This method is used to handle {@link RequestAvailableGamesMessage} message.
         * First, it checks if requesting client is already registered to server using
         * {@link MainServerTCP#getClientHandlerFromSocket(Socket)}. If yes, it sends
         * to a {@link AvailableGamesMessage} with all the a
         * @param message {@link RequestAvailableGamesMessage} to handle.
         */
        @Override
        public void visit(RequestAvailableGamesMessage message){
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket);
            if(clientHandlerSocket != null){
                clientHandlerSocket.update(new AvailableGamesMessage(new ArrayList<>(mainController.findAvailableGames())));
            }
        }

    }

    private class HeartBeatMessageToServerVisitor implements MessageToServerVisitor, HeartBeatMessageVisitor{

        private Socket clientSocket;

        /**
         * This method is used by caller to know if {@link HeartBeatMessageToServerVisitor} can
         * accept {@param message} object.
         * @param message {@link MessageToServer} to check.
         * @return <code>true</code> if and only if {@param message} can be accepted by this object.
         */
        public boolean canAccept(MessageToServer message){
            return message instanceof HeartBeatMessage;
        }

        /**
         * This method is the entry point for {@link HeartBeatMessageToServerVisitor} object.
         * @param socket the {@link Socket} from which the message has arrived.
         * @param message {@link MessageToServer} to handle
         */
        public void handleMessageToMainServer(Socket socket, MessageToServer message){
            this.clientSocket = socket;
            message.accept(this);
        }

        /**
         * This method is used handle a {@link HeartBeatMessage} received from client.
         * It checks if {@link Socket} is in <code>lastHeartBeatOfClients</code> and if yes,
         * it updates the hashmap
         * @param message {@link HeartBeatMessage} to handle.
         */
        @Override
        public void visit(HeartBeatMessage message) {
            if (lastHeartBeatOfClients.containsKey(clientSocket)) {
                lastHeartBeatOfClients.put(clientSocket, new Date().getTime());
            }
        }
    }

}
