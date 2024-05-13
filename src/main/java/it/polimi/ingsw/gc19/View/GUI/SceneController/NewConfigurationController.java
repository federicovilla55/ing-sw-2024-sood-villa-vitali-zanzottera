package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMIFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCPFactory;
import it.polimi.ingsw.gc19.View.ClientController.NotPlayer;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.TUI.GeneralListener;
import it.polimi.ingsw.gc19.View.UI;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class NewConfigurationController extends AbstractController implements UI, GeneralListener {

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
        super.getClientController().getListenersManager().attachListener(this);
        super.getClientController().setView(this);
        super.getClientController().setClientInterface(client);
        System.out.println("Successfully connected to the server!");
        System.out.print("> ");
        super.getClientController().setNextState(new NotPlayer(super.getClientController()));
        System.out.println("Rmi");
        System.out.println("Tcp");
    }


    @Override
    public void notify(ArrayList<Message> msg) {

    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {

    }

    @Override
    public void notify(PersonalStation localStationPlayer) {

    }

    @Override
    public void notify(OtherStation otherStation) {

    }

    @Override
    public void notifyErrorStation(String... varArgs) {

    }

    @Override
    public void notify(LocalTable localTable) {

    }

    @Override
    public void notify(String nick, TurnState turnState) {

    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {

    }

    @Override
    public void notifyPlayerCreation(String name) {

    }

    @Override
    public void notifyPlayerCreationError(String error) {

    }

    @Override
    public void notify(SetupEvent type) {

    }

    @Override
    public void notify(ViewState viewState) {

    }

    @Override
    public void notifyGenericError(String errorDescription) {

    }

    @Override
    public void notify(String message) {

    }
}
