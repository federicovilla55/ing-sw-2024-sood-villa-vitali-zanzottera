package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.Disconnect;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A scene controller used to display all {@link Configuration} read from {@link ClientSettings#CONFIG_FILE_PATH} folder
 * and letting user reconnect with a specific one.
 */
public class OldConfigurationController extends GUIController implements StateListener{

    /**
     * All the {@link Configuration} read from {@link ClientSettings#CONFIG_FILE_PATH} folder.
     * They will be displayed by controller to let user reconnect
     */
    private ArrayList<Configuration> configs;

    /**
     * The {@link ClientInterface} to be built
     */
    private ClientInterface client;

    @FXML
    private TableView<Configuration> confTable;
    @FXML
    private TableColumn<Configuration, String> nicknameCol;
    @FXML
    private TableColumn<Configuration, String> timeCol;
    @FXML
    private TableColumn<Configuration, Configuration.ConnectionType> conTypeCol;
    @FXML
    private Button reconnectButton, newConfButton, deleteConf, deleteAllConf;
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

    /**
     * Setter for {@link #configs}
     * @param configs the {@code ArrayList<Configuration>} to be set in {@link #configs}
     */
    public void setConfig(ArrayList<Configuration> configs) {
        this.configs = configs;
    }

    /**
     * Sets up the {@link TableView} used to display {@link #configs}
     */
    public void setUpConfigTable() {
        nicknameCol.setCellValueFactory(new PropertyValueFactory<Configuration, String>("nick"));
        timeCol.setCellValueFactory(new PropertyValueFactory<Configuration, String>("timestamp"));
        conTypeCol.setCellValueFactory(new PropertyValueFactory<Configuration, Configuration.ConnectionType>("connectionType"));
        confTable.setItems(FXCollections.observableArrayList(this.configs));
    }

    /**
     * Initializes the scene.
     */
    @FXML
    private void initialize() {
        logoImageView.fitHeightProperty().bind(super.getStage().heightProperty().divide(4));
        buttonsHBox.spacingProperty().bind(super.getStage().widthProperty().divide(8));

        nicknameCol.prefWidthProperty().bind(confTable.widthProperty().divide(3));
        timeCol.prefWidthProperty().bind(confTable.widthProperty().divide(3));
        conTypeCol.prefWidthProperty().bind(confTable.widthProperty().divide(3));

        loadLogo();

        newConfButton.setOnAction(this::onNewConfigurationPressed);
        reconnectButton.setOnAction(this::onReconnectPress);
        deleteConf.setOnAction(this::onDeleteConf);
        deleteAllConf.setOnAction(this::onDeleteAll);
    }

    /**
     * Loads Codex Naturalis' logo in the scene
     */
    private void loadLogo() {
        Image logoImage = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("it/polimi/ingsw/gc19/images/logo.png")));
        logoImageView.setImage(logoImage);
        logoImageView.setPreserveRatio(true);
    }

    /**
     * This method is executed when user press the reconnection button.
     * It signals {@link ClientController} to try to reconnect the user
     * using the specified configuration.
     * An {@link Alert} is shown if an error occurs during this process
     * @param e the {@link ActionEvent} of the mouse pressed
     */
    private void onReconnectPress(ActionEvent e) {
        Configuration config = confTable.getSelectionModel().getSelectedItem();
        Configuration.ConnectionType connectionType;

        if(config != null) {
            connectionType = config.getConnectionType();

            new Thread(() -> {
                try {
                    client = connectionType.getClientFactory().createClient(super.getClientController());
                }
                catch (IOException | RuntimeException ex) {
                    Platform.runLater(() -> {
                                          Alert alert = new Alert(Alert.AlertType.ERROR);
                                          alert.setTitle(connectionType.toString().toUpperCase() + " network problems");
                                          alert.setContentText(ex.getMessage());
                                          alert.initOwner(super.getStage().getScene().getWindow());

                                          alert.showAndWait();
                                      });

                    System.exit(1);
                }
            }).start();

            client.configure(config.getNick(), config.getToken());

            super.getClientController().setNickname(config.getNick());
            super.getClientController().setClientInterface(client);

            super.getClientController().setNextState(new Disconnect(super.getClientController()), true);
        }
    }

    /**
     * This method is executed when user press the new configuration button.
     * It, simply, changes scene to {@link SceneStatesEnum#NEW_CONFIGURATION_SCENE}
     * @param e the {@link ActionEvent}  of the mouse clicked
     */
    private void onNewConfigurationPressed(ActionEvent e){
        super.getClientController().getListenersManager().removeListener(this);

        changeToNextScene(SceneStatesEnum.NEW_CONFIGURATION_SCENE);
    }

    /**
     * This method is executed when user press the "delete all" button.
     * It deletes all configuration in {@link #configs} and in {@link ClientSettings#CONFIG_FILE_PATH}
     * @param e the {@link ActionEvent} of the mouse pressed
     */
    private synchronized void onDeleteAll(ActionEvent e){
        List<Configuration> toRemove = configs.stream().toList();
        for(Configuration conf : toRemove){
            confTable.getSelectionModel().select(conf);
            onDeleteConf(e);
        }
    }

    /**
     * This method is executed when user press the "delete" button.
     * It deletes a specific configuration in {@link #configs} and in {@link ClientSettings#CONFIG_FILE_PATH}
     * @param e the {@link ActionEvent} of the mouse pressed
     */
    private void onDeleteConf(ActionEvent e){
        Configuration config = confTable.getSelectionModel().getSelectedItem();
        configs.remove(config);
        confTable.setItems(FXCollections.observableArrayList(this.configs));

        if(config != null){
            ConfigurationManager.deleteConfiguration(config.getNick());
        }
    }

    /**
     * Used to notify {@link OldConfigurationController} about events
     * concerning {@link ViewState}.
     * @param viewState the new {@link ViewState}
     */
    @Override
    public void notify(ViewState viewState) {
        switch (viewState){
            case ViewState.NOT_PLAYER -> super.changeToNextScene(SceneStatesEnum.LOGIN_SCENE);
            case ViewState.NOT_GAME -> super.changeToNextScene(SceneStatesEnum.GAME_SELECTION_SCENE);
            case ViewState.SETUP -> {
                super.setLocalModel(super.getClientController().getLocalModel());
                super.changeToNextScene(SceneStatesEnum.SETUP_SCENE);
            }
            case ViewState.PICK, ViewState.PLACE, ViewState.OTHER_TURN, ViewState.PAUSE, ViewState.END -> {
                super.setLocalModel(super.getClientController().getLocalModel());
                super.changeToNextScene(SceneStatesEnum.PLAYING_AREA_SCENE);
            }
        }
    }

}