package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Server;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class MainServerTCP extends Server implements ObserverMessageToServer<MessageToServer> {

    private final HashMap<Socket, Tuple<ClientHandlerSocket, String>> connectedClients;
    private final ConcurrentHashMap<Socket, Tuple<Long, ScheduledExecutorService>> lastHeartBeatOfClients;
    private final MessageToMainServerVisitor messageHandler;

    public MainServerTCP(){
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new ConcurrentHashMap<>();
        this.messageHandler = new MessageToMainServerVisitor();
    }

    @Override
    public void update(Socket senderSocket, MessageToServer message) {
        synchronized (this.messageHandler) {
            this.messageHandler.handleMessageToMainServer(senderSocket, message);
        }
    }

    @Override
    public boolean accept(MessageToServer message) {
        return message instanceof GameHandlingMessage || message instanceof HeartBeatMessage;
    }

    private void runHeartBeatTesterForClient(Socket socket, String nick){
        while(!Thread.currentThread().isInterrupted()) {
            if (new Date().getTime() - this.lastHeartBeatOfClients.get(socket).x() > 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS) {
                if (nick != null) {
                    mainController.setPlayerInactive(nick);
                }
                this.lastHeartBeatOfClients.get(socket).y().shutdownNow();
                //@TODO: what to do with out messages? Disable also them?
                synchronized (this.connectedClients) {
                    this.connectedClients.get(socket).x().stopReadingMessages();
                    this.connectedClients.get(socket).x().stopWritingMessages();
                }
                this.lastHeartBeatOfClients.remove(socket);
            }
        }
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
                messageToServerDispatcher.attachObserver(clientHandlerSocket);
                this.connectedClients.put(socket, new Tuple<>(clientHandlerSocket, null));
                //At the beginning with socket we can only send messages, not receive them
                clientHandlerSocket.startWritingMessages();
                clientHandlerSocket.stopReadingMessages();

                synchronized (this.lastHeartBeatOfClients){
                    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
                    this.lastHeartBeatOfClients.put(socket, new Tuple<>(new Date().getTime(), executorService));
                    executorService.schedule(() -> runHeartBeatTesterForClient(socket, null), 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS, TimeUnit.MILLISECONDS);
                }

            }
            else{
                this.connectedClients.get(socket).x().sendMessageToClient(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                                                                "Your socket is already registered in server!")
                                                                                  .setHeader(this.connectedClients.get(socket).x().getName()));
            }
        }
    }

    public ClientHandlerSocket getClientHandlerFromSocket(Socket socket, String nick){
        synchronized (this.connectedClients){
            if(!this.connectedClients.containsKey(socket)){
                //@TODO: there can be this case?
                return null;
            }
            if(this.connectedClients.get(socket).y() == null){
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

    private class MessageToMainServerVisitor implements GameHandlingMessageVisitor, HeartBeatMessageVisitor, MessageToServerVisitor{

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
            if(clientHandlerSocket != null){
                mainController.createGame(message.getGameName(), message.getNumPlayer(), clientHandlerSocket, message.getRandomSeed());
                synchronized (connectedClients){
                    connectedClients.get(clientSocket).x().startReadingMessages();
                    connectedClients.get(clientSocket).x().startWritingMessages();
                }
            }
        }

        @Override
        public void visit(NewUserMessage message) {
            ClientHandlerSocket clientHandlerSocket;

            synchronized (connectedClients){
                if(!connectedClients.containsKey(clientSocket)){
                    //@TODO: is it necessary?
                    return;
                }
                if(connectedClients.get(clientSocket).y() != null){
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

            //Seting the username associated to client handler
            clientHandlerSocket.setUsername(nickname);
            clientHandlerSocket.startWritingMessages();
            clientHandlerSocket.stopReadingMessages();
            String hashedMessage = computeHashOfClientHandler(clientHandlerSocket, nickname);

            if(mainController.createClient(clientHandlerSocket)){
                synchronized (connectedClients){
                    connectedClients.put(clientSocket, new Tuple<>(clientHandlerSocket, hashedMessage));
                }

                //@TODO: capire se il fatto che le operazioni non sono atomiche in bloccco è uyn problema
                //Uccido il thread che prima si occupava degli heartbeat con nickname null (giocatore non ha ancora registrato il suo nome)
                //e lo ritiro su con il nome giusto
                synchronized (lastHeartBeatOfClients) {
                    lastHeartBeatOfClients.get(clientSocket).y().shutdownNow();
                    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
                    lastHeartBeatOfClients.put(clientSocket, new Tuple<>(new Date().getTime(), scheduledExecutorService));
                    scheduledExecutorService.schedule(() -> runHeartBeatTesterForClient(clientSocket, nickname), 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 5, TimeUnit.MILLISECONDS);
                }

                clientHandlerSocket.sendMessageToClient(new CreatedPlayerMessage(nickname, hashedMessage).setHeader(nickname));
            }
        }

        @Override
        public void visit(ReconnectToServerMessage message) {
            Socket socketBefore = null;
            ClientHandlerSocket clientSocketBefore;
            boolean found = false;

            synchronized (connectedClients){
                for(var v : connectedClients.entrySet()){
                    if(v.getValue().y() != null && v.getValue().y().equals(message.getToken())){
                        v.getValue().x().stopSendingMessages();

                        socketBefore = v.getKey();
                        closeSocket(socketBefore);
                        clientSocketBefore = v.getValue().x();
                        connectedClients.remove(socketBefore);

                        connectedClients.get(clientSocket).x().pullClientHandlerSocketConfigIntoThis(clientSocketBefore);
                        System.out.println(connectedClients.get(clientSocket).x().getGameController());
                        connectedClients.put(clientSocket, new Tuple<>(connectedClients.get(clientSocket).x(), connectedClients.get(clientSocket).y()));
                        found = true;
                        break;
                    }
                }
            }

            if(found){
                lastHeartBeatOfClients.get(socketBefore).y().shutdownNow();
                lastHeartBeatOfClients.remove(socketBefore);
                ScheduledExecutorService heartBeatExecutor = new ScheduledThreadPoolExecutor(1);
                heartBeatExecutor.schedule(() -> runHeartBeatTesterForClient(clientSocket, nickname), 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 5, TimeUnit.MILLISECONDS);
                lastHeartBeatOfClients.put(clientSocket, new Tuple<>(new Date().getTime(), heartBeatExecutor));

                synchronized (connectedClients){
                    if(connectedClients.get(clientSocket).x() != null){
                        mainController.reconnect(connectedClients.get(clientSocket).x());
                    }
                }
            }
            else{
                synchronized (connectedClients){
                    connectedClients.get(clientSocket).x().sendMessageToClient(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                                                                     "You are not registered to server! Please register...")
                                                                                       .setHeader(nickname));
                }
            }
        }

        @Override
        public void visit(DisconnectMessage message) {
            synchronized (connectedClients){
                connectedClients.get(clientSocket).x().stopWritingMessages();
                connectedClients.get(clientSocket).x().stopReadingMessages();

                //Killing thread responsible for sending messages
                connectedClients.get(clientSocket).x().stopSendingMessages();

                if(connectedClients.get(clientSocket).y() != null) {
                    mainController.disconnect(connectedClients.remove(clientSocket).x());
                }
                else{
                    connectedClients.remove(clientSocket);
                }
                closeSocket(clientSocket);
            }
            if(lastHeartBeatOfClients.containsKey(clientSocket)) {
                lastHeartBeatOfClients.get(clientSocket).y().shutdownNow();
                lastHeartBeatOfClients.remove(clientSocket);
            }

            try {
                clientSocket.close();
            }
            catch (IOException ioException){
                //@TODO: handle this exception
            }
            //@TODO: if client handler socket has some thread kill them
        }

        @Override
        public void visit(JoinGameMessage message) {
            ClientHandlerSocket clientHandlerSocket = getClientHandlerFromSocket(clientSocket, nickname);
            if(clientHandlerSocket != null){
                if(mainController.registerToGame(clientHandlerSocket, message.getGameName())) {
                    synchronized (connectedClients) {
                        //System.out.println(message.getNickname() + " -> " + message.getGameName() + "  " + connectedClients.get(clientSocket).x().getGameController());
                        connectedClients.get(clientSocket).x().startReadingMessages();
                        connectedClients.get(clientSocket).x().startWritingMessages();
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
                        connectedClients.get(clientSocket).x().startReadingMessages();
                        connectedClients.get(clientSocket).x().startWritingMessages();
                    }
                }
            }
        }

        @Override
        public void visit(HeartBeatMessage message) {
            String nickname = message.getNickname();
            ScheduledExecutorService heartBeatTester;
            if(lastHeartBeatOfClients.containsKey(clientSocket)){
                heartBeatTester = lastHeartBeatOfClients.get(clientSocket).y();
                heartBeatTester.shutdownNow();
                ScheduledExecutorService heartTester = new ScheduledThreadPoolExecutor(1);
                heartTester.schedule(() -> runHeartBeatTesterForClient(clientSocket, nickname), 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 5, TimeUnit.MILLISECONDS);
                lastHeartBeatOfClients.put(clientSocket, new Tuple<>(new Date().getTime(), heartBeatTester));
            }
        }
    }

}
