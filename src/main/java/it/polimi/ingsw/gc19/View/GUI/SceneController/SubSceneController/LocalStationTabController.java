package it.polimi.ingsw.gc19.View.GUI.SceneController.SubSceneController;

import it.polimi.ingsw.gc19.View.GUI.SceneController.GUIController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenerType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LocalStationTabController extends GUIController implements LocalModelListener {

    @FXML
    private TabPane stations;

    private static Tab currentTab;

    public LocalStationTabController(GUIController controller) {
        super(controller);

        super.getClientController().getListenersManager().attachListener(ListenerType.LOCAL_MODEL_LISTENER, this);
    }

    @FXML
    public void initialize(){
        buildTabs();

        stations.getStyleClass().add("floating");
    }

    private void buildTabs(){
        this.stations.getTabs().clear();

        if(this.getLocalModel() != null){

            for(LocalStationPlayer l : new ArrayList<>(this.getLocalModel().getStations().values())) {

                LocalStationController controller = null;
                BorderPane tabContent;

                try{
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(new File("/fxml/LocalStationScene.fxml").toURL());

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

                tabContent.prefHeightProperty().bind(stations.heightProperty().subtract(65));
                tabContent.prefWidthProperty().bind(stations.widthProperty());
                tabContent.prefHeightProperty().addListener(
                        (observable, oldValue, newValue) -> tabContent.setMaxSize(tabContent.getPrefWidth(), tabContent.getPrefHeight())
                );

                if(this.getLocalModel().getNickname().equals(l.getOwnerPlayer())) {
                    this.stations.getTabs().addFirst(tab);
                    stations.getSelectionModel().select(currentTab);
                    System.out.println(l.getOwnerPlayer());
                }else{
                    this.stations.getTabs().add(tab);
                }


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