package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientFactory.ClientRMIFactory;
import it.polimi.ingsw.gc19.Networking.Client.ClientFactory.ClientTCPFactory;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientFactory.*;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Objects;

/**
 * A scene controller for the new configuration. It lets user
 * choose their preferred connection type and consequently builds
 * the "network interface"
 */
public class NewConfigurationController extends GUIController implements StateListener {

    /**
     * The {@link ClientInterface} that will be built
     */
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

    /**
     * Initializes the scene with its buttons, logo and background
     */
    @FXML
    public void initialize(){
        TCPButton.setOnMouseClicked((event) -> TCPPress());
        RMIButton.setOnMouseClicked((event) -> RMIPress());
        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(3));

        loadLogo();

        super.setBackground(borderPane, false);
    }

    /**
     * Loads logo in the scene
     */
    private void loadLogo() {
        Image logoImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/logo.png")));
        logoImageView.setImage(logoImage);
        logoImageView.setPreserveRatio(true);
    }

    /**
     * When user press "RMI" button, this methods will build
     * a new {@link ClientRMI} using {@link ClientFactory}. If an error
     * occurs during the process an {@link Alert} is shown and application shuts down
     */
    public void RMIPress()  {
        ClientRMIFactory connectionRMI = new ClientRMIFactory();

        new Thread(() -> {
            try {
                this.client = connectionRMI.createClient(super.getClientController());
            }
            catch (RemoteException | RuntimeException ex) {
                Platform.runLater(() -> {
                                      Alert alert = new Alert(Alert.AlertType.ERROR);
                                      alert.setTitle("RMI network problems");
                                      alert.setContentText(ex.getMessage());

                                      alert.initOwner(super.getStage().getScene().getWindow());

                                      alert.showAndWait();
                                  });

                System.exit(1);
            }
        }).start();

        super.getClientController().setClientInterface(client);
    }

    /**
     * When user press "TCP" button, this methods will build
     * a new {@link ClientTCP} using {@link ClientFactory}. If an error
     * occurs during the process an {@link Alert} is shown and application shuts down
     */
    public void TCPPress(){
        ClientTCPFactory connectionTCP = new ClientTCPFactory();

        new Thread(() -> {
            try {
                this.client = connectionTCP.createClient(super.getClientController());
            }
            catch (IOException ex) {
                Platform.runLater(() -> {
                                      Alert alert = new Alert(Alert.AlertType.ERROR);
                                      alert.setTitle("TCP network problems");
                                      alert.setContentText(ex.getMessage());
                                      alert.initOwner(super.getStage().getScene().getWindow());

                                      alert.showAndWait();
                                  });

                System.exit(1);
            }
        }).start();

        super.getClientController().setClientInterface(client);
    }

    /**
     * Used to notify {@link NewConfigurationController} about events
     * concerning {@link ViewState}.
     * @param viewState the new {@link ViewState}
     */
    @Override
    public void notify(ViewState viewState) {
        if(viewState == ViewState.NOT_PLAYER) {
            super.getClientController().getListenersManager().removeListener(ListenerType.STATE_LISTENER, this);

            super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
        }
    }
}
