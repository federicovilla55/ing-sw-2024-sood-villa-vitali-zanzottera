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
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;


public class NewConfigurationController extends AbstractController implements StateListener {

    private ClientInterface client;
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
        contentVBox.spacingProperty().bind(super.getStage().heightProperty().divide(7));
        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(4));
        buttonsHBox.spacingProperty().bind(super.getStage().widthProperty().divide(8));

        loadLogo();

        double fontSizeFactor = 0.04;
        titleLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() * fontSizeFactor),
                super.getStage().heightProperty()
        ));

        TCPButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 40),
                super.getStage().heightProperty()
        ));
        RMIButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() / 40),
                super.getStage().heightProperty()
        ));
    }

    private void loadLogo() {
        try {
            Image logoImage = new Image(new FileInputStream("src/main/resources/images/logo.png"));
            logoImageView.setImage(logoImage);
            System.out.println("Logo loaded");
            logoImageView.setPreserveRatio(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
