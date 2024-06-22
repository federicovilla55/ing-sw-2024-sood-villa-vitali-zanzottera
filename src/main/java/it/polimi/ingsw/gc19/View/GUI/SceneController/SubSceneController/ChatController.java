package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.GUI.SceneController.GUIController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.ChatListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.CheckComboBox;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A sub-scene controller specialized in chat. It manages sending and receiving
 * messages.
 * Every scene that may require to use chat must have this sub-controller.
 */
public class ChatController extends GUIController implements ChatListener, LocalModelListener, StationListener {

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

    public ChatController(GUIController controller){
        super(controller);

        controller.getClientController().getListenersManager().attachListener(ListenerType.CHAT_LISTENER, this);
        controller.getClientController().getListenersManager().attachListener(ListenerType.LOCAL_MODEL_LISTENER, this);
        controller.getClientController().getListenersManager().attachListener(ListenerType.STATION_LISTENER, this);
    }

    /**
     * Initializes chat sub-scene.
     */
    public void initialize(){
        textAreaSend.textProperty().addListener((observable, oldValue, newValue) -> textAreaSend.setStyle("-fx-border: none"));

        sendButton.setOnMouseClicked((event) -> sendMessage());
        sendButton.setBackground(Background.fill(Color.LIGHTBLUE));

        receivers.setTitle("Receivers");

        receivers.getItems().clear();
        if(this.getLocalModel() != null && this.getLocalModel().getOtherStations() != null){
            receivers.getItems().addAll(this.getLocalModel().getOtherStations().keySet());
            if(receivers.getItems().isEmpty()){
                receivers.setDisable(true);
                sendButton.setDisable(true);
            }
            //receivers.getCheckModel().check(this.getLocalModel().getNickname());
        }

        textFlow.getChildren().clear();
        if(this.getLocalModel() != null && this.getLocalModel().getMessages() != null){
            showChat(this.getLocalModel().getMessages());
        }
    }

    /**
     * This method is used to notify {@link ChatController} that
     * a new chat message has to be displayed.
     * @param msg the {@code ArrayList<Message>} stored in chat
     */
    @Override
    public void notify(ArrayList<Message> msg) {
        showChat(msg);
    }

    /**
     * Builds chat TextFlow pane and fill it with all chat messages.
     * If sender has already chosen his color then message is colored
     * with that color, other is black.
     * @param msg the <code>ArrayList&lt;Message&gt;</code> containing all
     *            the messages to be displayed.
     */
    private void showChat(ArrayList<Message> msg){
        Platform.runLater(() -> {

            textFlow.getChildren().clear();

            for(Message m : new ArrayList<Message>(msg)){
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

    /**
     * Notify {@link ClientController} that a new chat message has to be sent.
     * If text is empty highlight {@link ChatController#textAreaSend}.
     * If no receivers are specified shows a {@link Alert}.
     */
    public void sendMessage() {
        if(this.textAreaSend.getText().isEmpty()){
            this.textAreaSend.setStyle("""
                                            -fx-border: 5px; 
                                            -fx-border-style: solid;
                                            -fx-border-color: red;
                                            -fx-border-radius: 5px;
                                            -fx-background-insets: 0 0 -7 0;
                                            -fx-background-color: linear-gradient(to bottom, red, #ff0000);
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

    /**
     * This method is used to notify {@link ChatController}
     * about {@link LocalModel} events.
     * @param type a {@link LocalModelEvents} representing the event type
     * @param localModel the {@link LocalModel} on which the event happened
     * @param varArgs eventual arguments
     */
    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        Platform.runLater(() -> {
            switch (type){
                case LocalModelEvents.NEW_PLAYER_CONNECTED,
                        LocalModelEvents.RECONNECTED_PLAYER -> {
                    this.receivers.getItems().add(varArgs[0]);
                    this.receivers.setDisable(false);
                    this.sendButton.setDisable(false);
                }
                case LocalModelEvents.DISCONNECTED_PLAYER -> {
                    this.receivers.getItems().remove(varArgs[0]);
                    if(this.receivers.getItems().isEmpty()){
                        this.receivers.setDisable(true);
                        this.sendButton.setDisable(true);
                    }
                }
            }
        });
    }

    /**
     * This method is used to notify {@link ChatController}
     * about {@link PersonalStation} events. Particularly, it
     * is used to update colors of messages.
     * @param localStationPlayer is the {@link PersonalStation} that has changed
     */
    @Override
    public void notify(PersonalStation localStationPlayer) {
        showChat(this.getLocalModel().getMessages());
    }

    /**
     * This method is used to notify {@link ChatController}
     * about {@link OtherStation} events. Particularly, it
     * is used to update colors of messages.
     * @param otherStation is the {@link OtherStation} that has changed
     */
    @Override
    public void notify(OtherStation otherStation) {
        showChat(this.getLocalModel().getMessages());
    }

    /**
     * This method is currently not implemented.
     * @param varArgs variable arguments describing the error
     */
    @Override
    public void notifyErrorStation(String... varArgs) { }

}