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
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;


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
