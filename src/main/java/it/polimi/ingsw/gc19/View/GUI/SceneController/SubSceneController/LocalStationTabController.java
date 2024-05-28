package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.GUI.SceneController.AbstractController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.StationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import it.polimi.ingsw.gc19.View.Listeners.StateListener.StateListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class LocalStationTabController extends AbstractController implements LocalModelListener {

    @FXML
    private TabPane tabPane;

    private static Tab currentTab;

    public LocalStationTabController(AbstractController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.LOCAL_MODEL_LISTENER, this);
    }

    @FXML
    public void initialize(){
        buildTabs();

        tabPane.getStyleClass().add("floating");
    }

    private void buildTabs(){
        this.tabPane.getTabs().clear();

        if(this.getLocalModel() != null){

            for(LocalStationPlayer l : new ArrayList<>(this.getLocalModel().getStations().values())) {

                LocalStationController controller = null;
                BorderPane tabContent;

                try{
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(new File("src/main/resources/fxml/LocalStationScene.fxml").toURL());

                    controller = new LocalStationController(this, l.getOwnerPlayer());

                    loader.setController(controller);

                    tabContent = loader.load();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Tab tab = new Tab();

                tab.setText(l.getOwnerPlayer());
                tab.setContent(tabContent);

                tabContent.prefHeightProperty().bind(tabPane.heightProperty().subtract(65));
                tabContent.prefWidthProperty().bind(tabPane.widthProperty());
                tabContent.prefHeightProperty().addListener(
                        (observable, oldValue, newValue) -> tabContent.setMaxSize(tabContent.getPrefWidth(), tabContent.getPrefHeight())
                );

                this.tabPane.getTabs().add(tab);

                tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
                    if(newTab != null && !newTab.equals(oldTab)){
                        currentTab = newTab;
                    }
                });
            }

            //set current tab to previous selected one or default to own station of player
            if(currentTab == null) {
                tabPane.getSelectionModel().select(tabPane.getTabs().stream().filter(t -> t.getText().equals(this.getLocalModel().getNickname())).findFirst().get());
                currentTab = tabPane.getSelectionModel().getSelectedItem();
            }
            else {
                tabPane.getSelectionModel().select(currentTab);
            }
        }
    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        Platform.runLater(() -> {
            if (type == LocalModelEvents.NEW_PLAYER_CONNECTED) buildTabs();
        });
    }

}