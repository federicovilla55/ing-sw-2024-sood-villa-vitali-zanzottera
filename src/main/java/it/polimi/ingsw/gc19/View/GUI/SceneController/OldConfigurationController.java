package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.Disconnect;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class OldConfigurationController extends AbstractController implements StateListener, GameHandlingListener{

    private List<Configuration> configs;

    @FXML
    private TableView<Configuration> confTable;

    @FXML
    private TableColumn<Configuration, String> nicknameCol;

    @FXML
    private TableColumn<Configuration, String> timeCol;

    @FXML
    private TableColumn<Configuration, Configuration.ConnectionType> conTypeCol;

    @FXML
    private Button ReconnectButton, NewConfButton;

    @FXML
    private ImageView logoImageView;

    @FXML
    private VBox contentVBox;

    @FXML
    private Label titleLabel;

    @FXML
    private HBox buttonsHBox;


    public OldConfigurationController(ClientController controller, CommandParser parser, Stage stage){
        super(controller, parser, stage);

        super.getClientController().getListenersManager().attachListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    public OldConfigurationController(AbstractController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);
        super.getClientController().getListenersManager().attachListener(ListenerType.STATE_LISTENER, this);
    }

    public void setConfig(List<Configuration> configs) {
        this.configs = configs;
    }

    public void setUpConfigTable() {
        nicknameCol.setCellValueFactory(new PropertyValueFactory<Configuration, String>("nick"));
        timeCol.setCellValueFactory(new PropertyValueFactory<Configuration, String>("timestamp"));
        conTypeCol.setCellValueFactory(new PropertyValueFactory<Configuration, Configuration.ConnectionType>("connectionType"));
        confTable.setItems(FXCollections.observableArrayList(this.configs));
    }

    public void initialize() {
        contentVBox.spacingProperty().bind(super.getStage().heightProperty().divide(9));
        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(4));
        buttonsHBox.spacingProperty().bind(super.getStage().widthProperty().divide(8));

        nicknameCol.prefWidthProperty().bind(confTable.widthProperty().divide(3));
        timeCol.prefWidthProperty().bind(confTable.widthProperty().divide(3));
        conTypeCol.prefWidthProperty().bind(confTable.widthProperty().divide(3));

        loadLogo();

        double fontSizeFactor = 0.03;
        titleLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getHeight() * fontSizeFactor),
                super.getStage().heightProperty()
        ));

        NewConfButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getWidth() / 70),
                super.getStage().widthProperty()
        ));
        ReconnectButton.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font(super.getStage().getWidth() / 70),
                super.getStage().widthProperty()
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


    @FXML
    public void onReconnectPress(ActionEvent e) {
        ClientInterface client;
        Configuration config = confTable.getSelectionModel().getSelectedItem();
        Configuration.ConnectionType connectionType;
        if(config != null) {
            System.out.println(config.getNick());
            connectionType = config.getConnectionType();
            try {
                client = connectionType.getClientFactory().createClient(super.getClientController());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            client.configure(config.getNick(), config.getToken());
            super.getClientController().setNickname(config.getNick());
            super.getClientController().setClientInterface(client);
            super.getClientController().setNextState(new Disconnect(super.getClientController()), false);
        }
    }
    @FXML
    public void onNewConfigurationPressed(ActionEvent e){
        super.getClientController().getListenersManager().removeListener(ListenerType.STATE_LISTENER, this);
        super.getClientController().getListenersManager().removeListener(ListenerType.GAME_HANDLING_EVENTS_LISTENER, this);

        changeToNextScene(SceneStatesEnum.NEW_CONFIGURATION_SCENE);
    }

    @Override
    public void notify(ViewState viewState) {
        switch (viewState){
            case ViewState.NOT_PLAYER -> super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
            case ViewState.NOT_GAME -> super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
            case ViewState.SETUP -> {
                super.setLocalModel(super.getClientController().getLocalModel());
                super.changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            }
            case ViewState.PAUSE -> System.out.println("Game is in pause! Sorry, you have to wait...");
            case ViewState.DISCONNECT -> System.err.println("[NETWORK PROBLEMS]: there are network problems. In background, we are trying to fix them...");
            case ViewState.END -> System.out.println("ciao");
        }
        System.out.println(viewState);
    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {

    }

}
