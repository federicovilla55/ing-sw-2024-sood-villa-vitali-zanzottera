package it.polimi.ingsw.gc19.Networking.Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class Server {

    static ServerSocket serverSocket = null;
    public static void main(String[] args) throws IOException{

        serverSocket = new ServerSocket(12345);
        System.out.println("Server started.");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);

            Thread clientHandlerThread = new Thread(new ClientHandle(clientSocket));
            clientHandlerThread.start();
        }
    }
}
