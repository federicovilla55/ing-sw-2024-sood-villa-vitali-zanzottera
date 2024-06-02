package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GUI.SceneController.SetupController;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesEnum;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.ChatListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ChatController extends AbstractController implements ChatListener, LocalModelListener, StationListener {

    @FXML
    private ScrollPane scrollText, scrollPaneSend;

    @FXML
    private TextArea textAreaSend;

    @FXML
    private TextFlow textFlow;

    @FXML
    private CheckComboBox<String> receivers;

    @FXML
    public Button sendButton;

    public ChatController(AbstractController controller){
        super(controller);

        controller.getClientController().getListenersManager().attachListener(ListenerType.CHAT_LISTENER, this);
        controller.getClientController().getListenersManager().attachListener(ListenerType.LOCAL_MODEL_LISTENER, this);
        controller.getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
    }

    public void initialize(){
        textAreaSend.textProperty().addListener((observable, oldValue, newValue) -> textAreaSend.setStyle("-fx-border: none"));

        sendButton.setOnMouseClicked((event) -> sendMessage());
        sendButton.setBackground(Background.fill(Color.LIGHTBLUE));

        receivers.setTitle("Receivers");

        receivers.getItems().clear();
        if(this.getLocalModel() != null && this.getLocalModel().getStations() != null){
            receivers.getItems().addAll(this.getLocalModel().getStations().keySet());
            receivers.getCheckModel().check(this.getLocalModel().getNickname());
        }

        textFlow.getChildren().clear();
        if(this.getLocalModel() != null && this.getLocalModel().getMessages() != null){
            showChat(this.getLocalModel().getMessages());
        }
    }

    @Override
    public void notify(ArrayList<Message> msg) {
        showChat(msg);
    }

    private void showChat(ArrayList<Message> msg){
        Platform.runLater(() -> {

            textFlow.getChildren().clear();

            for(Message m : msg){
                Text sender = new Text(), message = new Text();
                for(LocalStationPlayer l : new ArrayList<>(this.getLocalModel().getStations().values())){
                    if(l.getOwnerPlayer().equals(m.getSenderPlayer())){
                        if(l.getChosenColor() != null) {
                            sender.setStyle("-fx-fill: " + l.getChosenColor().toString().toLowerCase());
                        }
                        else{
                            sender.setStyle("-fx-fill: black");
                        }

                        sender.setText("[" + m.getSendTime() + "] " + m.getSenderPlayer() + ": \n");

                        message.setText(m.getMessage() + "\n");

                        textFlow.getChildren().add(sender);
                        textFlow.getChildren().add(message);
                    }
                }
            }
        });
    }

    public void sendMessage() {
        if(this.textAreaSend.getText().isEmpty()){
            this.textAreaSend.setStyle("""
                                            -fx-border: 5px; 
                                            -fx-border-style: solid;
                                            -fx-border-color: red;
                                            -fx-border-radius: 5px;
                                            -fx-background-insets: 0 0 -7 0;
                                            -fx-background-color: linear-gradient(from 0% red, to 100% #ff0000);
                                        """);
            return;
        }

        if(!this.receivers.getCheckModel().getCheckedItems().isEmpty()){
            this.getClientController().sendChatMessage(this.textAreaSend.getText(), this.receivers.getCheckModel().getCheckedItems());
        }
        else{
            Alert noReceiversAlert = new Alert(Alert.AlertType.ERROR);
            noReceiversAlert.initOwner(super.getStage().getScene().getWindow());
            noReceiversAlert.setTitle("Chat error");
            noReceiversAlert.setContentText("No receivers specified! Please specify alt least one receiver.");
            Platform.runLater(noReceiversAlert::show);
        }
    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        Platform.runLater(() -> {
            switch (type){
                case LocalModelEvents.NEW_PLAYER_CONNECTED, LocalModelEvents.RECONNECTED_PLAYER -> this.receivers.getItems().add(varArgs[0]);
                case LocalModelEvents.DISCONNECTED_PLAYER -> this.receivers.getItems().remove(varArgs[0]);
            }
        });
    }

    @Override
    public void notify(PersonalStation localStationPlayer) {
        showChat(this.getLocalModel().getMessages());
    }

    @Override
    public void notify(OtherStation otherStation) {
        showChat(this.getLocalModel().getMessages());
    }

    @Override
    public void notifyErrorStation(String... varArgs) { }

}
