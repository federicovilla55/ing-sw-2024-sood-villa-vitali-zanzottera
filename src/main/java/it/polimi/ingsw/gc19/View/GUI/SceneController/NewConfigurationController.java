package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMIFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCPFactory;
import it.polimi.ingsw.gc19.View.ClientController.NotPlayer;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;

import java.io.IOException;
import java.rmi.RemoteException;


public class NewConfigurationController extends AbstractController{

    private ClientInterface client;
    @FXML
    public void RMIPress(ActionEvent e)  {
        ClientRMIFactory connectionRMI = new ClientRMIFactory();
        try {
            this.client = connectionRMI.createClient(super.getClientController());
        } catch (RemoteException ex) {
            System.exit(1);
            return;
        }
        super.getClientController().setClientInterface(client);
        System.out.println("Successfully connected to the server!");
        System.out.print("> ");
        super.getClientController().setNextState(new NotPlayer(super.getClientController()));
        System.out.println("Rmi");

    }

    @FXML
    public void TCPPress(ActionEvent e){
        ClientTCPFactory connectionTCP = new ClientTCPFactory();
        try {
            this.client = connectionTCP.createClient(super.getClientController());
        } catch (IOException ex) {
            System.exit(1);
            return;
        }
        super.getClientController().setClientInterface(client);
        System.out.println("Successfully connected to the server!");
        System.out.print("> ");
        super.getClientController().setNextState(new NotPlayer(super.getClientController()));
        System.out.println("Rmi");
        System.out.println("Tcp");
    }

}
