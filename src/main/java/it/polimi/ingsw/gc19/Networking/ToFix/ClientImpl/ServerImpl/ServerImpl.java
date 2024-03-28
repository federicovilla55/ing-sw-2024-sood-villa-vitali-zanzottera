package it.polimi.ingsw.gc19.Networking.ToFix.ClientImpl.ServerImpl;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerImpl {

    /*static List<ClientHandle> ActiveClient;
    static List<ClientHandle> NoNonActiveClient;
    public static void main(String[] args) throws IOException{
        List<Socket> ActiveList = new ArrayList<Socket>();

        ActiveClient  = new ArrayList<ClientHandle>();
        NoNonActiveClient = new ArrayList<ClientHandle>();
        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(12345);
        System.out.println("Server started.");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);
            ClientHandle Client = new ClientHandle(clientSocket);
            ActiveClient.add(Client);
            Thread clientHandlerThread = new Thread(Client);
            clientHandlerThread.start();
        }
    }

    public void ControlActiveCLient()
    {
        /*prende ActiveCLient GetLastTimeStep
        SYetmgetmillis-GetLastTimeStep > tot
        Va a chiamare SetToNonActive(nickname)

    }*/
}
