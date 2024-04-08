package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;

public class ClientHandlerSocket extends ClientHandler implements Runnable{
    /*private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private Controller MasterController;*/

    public ClientHandlerSocket(Socket clientSocket, MainController masterMainController) throws IOException {
        super("User", new GameController(new Game(4)));
        //this.in = new ObjectInputStream(System.in);
    }
    /*public ClientHandleSocket(Socket clientSocket, Controller MasterController) throws IOException
    {
        this.clientSocket = clientSocket;
        //this.nickName = null;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new  ObjectInputStream(clientSocket.getInputStream());
        //this.getLastTimeStep = System.currentTimeMillis();
        this.MasterController = MasterController;
    }*/

    public void run() {

    }

    @Override
    public void sendMessageToClient(MessageToClient message) {

    }

    @Override
    public void update(MessageToClient message) {

    }

    /*@Override
    public void newConnection(VirtualClient client, String nickName) throws RemoteException {

    }

    @Override
    public void createGame(String nickName, String gameName, int numPlayer) throws RemoteException {

    }

    @Override
    public void joinGame(String nickName, String GameName) throws RemoteException {

    }

    @Override
    public void PlaceCard(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) throws RemoteException {

    }


    @Override
    public void HeartBeat(String nickName) throws RemoteException {

    }
    @Override
    public void Reconnect() throws RemoteException {

    }

    @Override
    public void SendChatTo(String nickName, ArrayList<String> UsersToSend, String messageToSend) throws RemoteException {

    }

    @Override
    public void SetInitialCard(String nickName, CardOrientation cardOrientation) throws RemoteException {

    }

    @Override
    public void DrawFromTable(String nickname, PlayableCardType type, int position) {

    }

    @Override
    public void DrawFromDeck(String nickname, PlayableCardType type) {

    }*/
}
