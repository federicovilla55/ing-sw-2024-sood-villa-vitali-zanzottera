package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.ChatListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.controlsfx.control.CheckComboBox;

import java.util.ArrayList;

public class ChatController extends AbstractController implements ChatListener, LocalModelListener {

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

    public void initialize(){
        String style = "-fx-background-color:transparent;"+
                       "-fx-border-style: solid inside; " +
                       "-fx-border-color: black; " +
                       "-fx-border-insets: 5;";

        scrollText.setStyle(style);
        scrollPaneSend.setStyle(style);
        textAreaSend.setStyle("-fx-background-color:transparent;");
        textFlow.setStyle("-fx-background-color:transparent;");

        textAreaSend.textProperty().addListener((observable, oldValue, newValue) -> textAreaSend.setStyle("-fx-border: none"));

        sendButton.setStyle(style);
        sendButton.setBackground(Background.fill(Color.LIGHTBLUE));

        receivers.setTitle("Receivers");
        receivers.setStyle(style);
    }

    @Override
    public void notify(ArrayList<Message> msg) {
        Text sender = new Text(), message = new Text();

        textFlow.getChildren().clear();

        for(Message m : msg){
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

        if(this.receivers.getCheckModel().getCheckedItems().size() > 1){
            this.getClientController().sendChatMessage(this.textAreaSend.getText(), this.receivers.getCheckModel().getCheckedItems());
        }
        else{
            Alert noReceiversAlert = new Alert(Alert.AlertType.ERROR);
            noReceiversAlert.setTitle("Chat error");
            noReceiversAlert.setContentText("No receivers specified! Please specify alt least one receiver.");
            Platform.runLater(noReceiversAlert::show);
        }
    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        this.receivers.getItems().addAll(this.getLocalModel().getStations().keySet().stream().toList());
    }

}
