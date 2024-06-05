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
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Objects;


public class NewConfigurationController extends GUIController implements StateListener {

    private ClientInterface client;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Button TCPButton, RMIButton;

    @FXML
    private ImageView logoImageView;

    @FXML
    private VBox contentVBox;

    @FXML
    private Label titleLabel;

    @FXML
    private HBox buttonsHBox;

    protected NewConfigurationController(GUIController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    public NewConfigurationController(ClientController controller, CommandParser parser, Stage stage){
        super(controller, parser, stage);

        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    @FXML
    public void initialize(){
        super.getStage().setMaximized(false);

        TCPButton.setOnMouseClicked((event) -> TCPPress());
        RMIButton.setOnMouseClicked((event) -> RMIPress());
        contentVBox.spacingProperty().bind(super.getStage().heightProperty().divide(7));
        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(4));
        buttonsHBox.spacingProperty().bind(super.getStage().widthProperty().divide(8));

        loadLogo();

        super.getStage().sizeToScene();
        super.getStage().setResizable(false);

        super.setBackground(borderPane, false);

        super.getStage().sizeToScene();
        super.getStage().setResizable(false);
    }

    private void loadLogo() {
        Image logoImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/logo.png")));
        logoImageView.setImage(logoImage);
        logoImageView.setPreserveRatio(true);
    }


    public void RMIPress()  {
        ClientRMIFactory connectionRMI = new ClientRMIFactory();

        try {
            this.client = connectionRMI.createClient(super.getClientController());
        } catch (RemoteException | RuntimeException ex) {
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
