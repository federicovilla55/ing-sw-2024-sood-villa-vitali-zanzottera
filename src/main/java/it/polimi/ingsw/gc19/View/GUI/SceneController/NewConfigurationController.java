package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMIFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCPFactory;
import it.polimi.ingsw.gc19.View.ClientController.NotPlayer;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import it.polimi.ingsw.gc19.View.TUI.GeneralListener;
import it.polimi.ingsw.gc19.View.UI;
import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class NewConfigurationController extends AbstractController implements StateListener {

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
        super.attachToListener(SceneStatesEnum.NewConfigurationScene);
        super.setToView();
        super.getClientController().setClientInterface(client);
        //super.getClientController().setNextState(new NotPlayer(super.getClientController()));
    }
    @FXML
    public void TCPPress(ActionEvent e){
        changeToNextScene(SceneStatesEnum.SETUP_SCENE);
        /*ClientTCPFactory connectionTCP = new ClientTCPFactory();
        try {
            this.client = connectionTCP.createClient(super.getClientController());
        } catch (IOException ex) {
            System.exit(1);
            return;
        }
        super.attachToListener();
        super.setToView();
        super.getClientController().setClientInterface(client);
        //super.getClientController().setNextState(new NotPlayer(super.getClientController()));*/
    }
    @Override
    public void notify(ViewState viewState) {
        super.changeToNextScene(SceneStatesEnum.LoginScene);
    }
}
