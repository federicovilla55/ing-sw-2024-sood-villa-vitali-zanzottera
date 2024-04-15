package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Networking.Server.Settings;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class TCPConnectionAcceptor extends Thread{

    private ServerSocket serverSocket;
    private final MainServerTCP mainServerTCP;

    public TCPConnectionAcceptor(MainServerTCP mainServerTCP){
        this.mainServerTCP = mainServerTCP;

        try {
            this.serverSocket = new ServerSocket(Settings.DEFAULT_SERVER_PORT);
        }
        catch (IOException ioException){
            //@TODO: handle this exception
        }
    }

    public void run(){
        Socket clientSocket;
        MessageToServerDispatcher messageToServerDispatcher;
        while(true){
            clientSocket = null;
            try{
                clientSocket = serverSocket.accept();
            }
            catch(IOException ioException){
                //@TODO: handle this exception
            }
            assert clientSocket != null;

            messageToServerDispatcher = new MessageToServerDispatcher(clientSocket);
            messageToServerDispatcher.attachObserver(this.mainServerTCP);
            mainServerTCP.registerSocket(clientSocket, messageToServerDispatcher);
            messageToServerDispatcher.start();
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    /* public void stopServer(){
        synchronized (this.connectedClients) {
            this.connectedClients.clear();
        }
        for(Map.Entry<Socket, ClientHandlerSocket> c : this.connectedClients.entrySet()){
            c.getValue().killClientSocketHandler();
        }
    }

    public void disconnectClient(Socket socket){
        synchronized (this.connectedClients){
            this.connectedClients.remove(socket);
        }
    }*/

    //@TODO: how to stop kill server?
    //@TODO: is it better to implement Runnable?

}
/*
private ServerSocket mainServerSocket;
    private static TCPConnectionAcceptor TCPConnectionAcceptorInstance = null;
    private final ConcurrentHashMap<Socket, ClientHandlerSocket> connectedClients;
    private final ExecutorService acceptorExecutor = Executors.newSingleThreadExecutor();

    public static TCPConnectionAcceptor getInstance(){
        if(TCPConnectionAcceptorInstance == null){
            TCPConnectionAcceptorInstance = new TCPConnectionAcceptor();
        }
        return TCPConnectionAcceptorInstance;
    }

    public TCPConnectionAcceptor(){
        this.connectedClients = new ConcurrentHashMap<>();

        try {
            this.mainServerSocket = new ServerSocket(Settings.DEFAULT_SERVER_PORT);
        }
        catch (IOException ioException){
            //@TODO: handle this exception
        }

        this.acceptorExecutor.submit(this::acceptConnection);
    }

    private void acceptConnection(){
        Socket clientSocket;
        while(true){
            clientSocket = null;
            try{
                clientSocket = mainServerSocket.accept();
            }
            catch(IOException ioException){
                //@TODO: handle this exception
            }
            assert clientSocket != null;
            synchronized (this.connectedClients) {
                if (!this.connectedClients.containsKey(clientSocket) || this.connectedClients.get(clientSocket).waitingForReconnection()) {
                    ClientHandlerSocket clientHandlerSocket = new ClientHandlerSocket(clientSocket);
                    this.connectedClients.put(clientSocket, clientHandlerSocket);
                }
            }
        }
    }

    public void stopServer(){
        synchronized (this.connectedClients) {
            this.connectedClients.clear();
        }
        for(Map.Entry<Socket, ClientHandlerSocket> c : this.connectedClients.entrySet()){
            c.getValue().killClientSocketHandler();
        }
    }

    public void disconnectClient(Socket socket){
        synchronized (this.connectedClients){
            this.connectedClients.remove(socket);
        }
    }

    //@TODO: how to stop kill server?
    //@TODO: is it better to implement Runnable?
 */

/*private  List<ClientHandler> ActiveList;
    final MainController masterMainController;

    private ServerSocket serverSocket;

    public ServerTcp(List<ClientHandler> ActiveList, MainController masterMainController) throws IOException
    {
        this.ActiveList = ActiveList;
        this.masterMainController = masterMainController;
        this.serverSocket = new ServerSocket(1234);
    }
    public void run()
    {
        System.out.println("Server started.");
        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("New client connected: " + clientSocket);
            ClientHandlerSocket Client = null;
            try {
                Client = new ClientHandlerSocket(clientSocket, masterMainController);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ActiveList.add(Client);
            Thread clientHandlerThread = new Thread(Client);
            clientHandlerThread.start();
        }
    }*/
