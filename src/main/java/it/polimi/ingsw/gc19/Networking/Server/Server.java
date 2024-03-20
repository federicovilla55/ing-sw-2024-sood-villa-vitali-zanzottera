package it.polimi.ingsw.gc19.Networking.Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static void main(String[] args) throws IOException{
        List<Socket> ActiveList = new ArrayList<Socket>();
        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(12345);
        System.out.println("Server started.");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);
            ActiveList.add(clientSocket);
            Thread clientHandlerThread = new Thread(new ClientHandle(clientSocket));
            clientHandlerThread.start();
        }
    }
}
