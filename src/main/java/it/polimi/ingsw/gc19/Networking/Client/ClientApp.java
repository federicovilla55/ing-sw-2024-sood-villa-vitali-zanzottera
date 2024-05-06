package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.IOException;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        String nick;

        System.out.println("nickname: ");
        nick = scanner.nextLine();

        ClientController clientController = new ClientController();
        MessageHandler messageHandler = new MessageHandler(clientController);
        messageHandler.start();

        try {
            ClientTCP clientTCP = new ClientTCP(messageHandler, clientController);
            clientController.setClientInterface(clientTCP);
            messageHandler.setClient(clientTCP);
        }
        catch (IOException ioException){
            System.err.println("errore");
        }

        clientController.createPlayer(nick);

        System.out.println("create / join: ");
        int choice = scanner.nextInt();

        if(choice == 1){
            clientController.createGame("game1", 3);
        }
        else{
            clientController.joinGame("game1");
        }
    }
}
