package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.NewUserMessage;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
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

public class OldConfigurationController extends AbstractController{


    private List<Configuration> configs;
    private Configuration.ConnectionType connectionType;

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
        Configuration config = confTable.getSelectionModel().getSelectedItem();
        if(config != null) {
            System.out.println(config.getNick());
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

}
