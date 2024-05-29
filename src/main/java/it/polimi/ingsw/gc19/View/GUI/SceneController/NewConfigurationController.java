package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMIFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCPFactory;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Objects;


public class NewConfigurationController extends AbstractController implements StateListener {

    private ClientInterface client;
    @FXML
    private Button TCPButton, RMIButton;

    protected NewConfigurationController(AbstractController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    public NewConfigurationController(ClientController controller, CommandParser parser, Stage stage){
        super(controller, parser, stage);

        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    @FXML
    public void initialize(){
        TCPButton.setOnMouseClicked((event) -> TCPPress());
        RMIButton.setOnMouseClicked((event) -> RMIPress());
    }

    public void RMIPress()  {
        ClientRMIFactory connectionRMI = new ClientRMIFactory();

        try {
            this.client = connectionRMI.createClient(super.getClientController());
        } catch (RemoteException ex) {
            System.exit(1);
            return;
        }

        super.getClientController().setClientInterface(client);
    }

    public void TCPPress(){
        ClientTCPFactory connectionTCP = new ClientTCPFactory();

        try {
            this.client = connectionTCP.createClient(super.getClientController());
        } catch (IOException ex) {
            System.exit(1);
            return;
        }

        super.getClientController().setClientInterface(client);
    }
    @Override
    public void notify(ViewState viewState) {
        super.getClientController().getListenersManager().removeListener(ListenerType.STATE_LISTENER, this);

        super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
    }
}
