package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.NewUserMessage;
import it.polimi.ingsw.gc19.View.ClientController.Disconnect;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.TableListener;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingListener;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


import java.io.File;
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
    public void onRecconectPress(ActionEvent e) {
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
            super.attachToListener(SceneStatesEnum.OldConfigurationScene);
            super.setToView();
            super.getClientController().setNickname(config.getNick());
            super.getClientController().setClientInterface(client);
            super.getClientController().setNextState(new Disconnect(super.getClientController()), false);
        }
    }
    @FXML
    public void onNewConfigurationPressed(ActionEvent e) throws IOException {
        Parent root;
        File url = new File(SceneStatesEnum.NewConfigurationScene.value());
        FXMLLoader loader = new FXMLLoader(url.toURL());
        root = loader.load();
        NewConfigurationController controller = loader.getController();
        controller.setCommandParser(this.getCommandParser());
        controller.setClientController(this.getClientController());
        controller.setStage(this.getStage());
        Platform.runLater(() -> {
            this.getStage().setScene(new Scene(root));
            this.getStage().show();
        });
    }

    @Override
    public void notify(ViewState viewState) {
        switch (viewState){
            case ViewState.NOT_PLAYER -> super.changeToNextScene(SceneStatesEnum.LoginScene);
            case ViewState.NOT_GAME -> super.changeToNextScene(SceneStatesEnum.GameSelectionScene);
            case ViewState.SETUP -> {
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
