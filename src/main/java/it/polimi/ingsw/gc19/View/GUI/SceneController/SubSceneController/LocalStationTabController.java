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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class LocalStationTabController extends AbstractController implements LocalModelListener, StationListener {

    @FXML
    private TabPane tabPane;

    private ArrayList<LocalStationController> localControllers;

    public LocalStationTabController(AbstractController controller) {
        super(controller);

        this.localControllers = new ArrayList<>();
    }

    @FXML
    public void initialize(){
        buildTabs();
    }

    //Se in fase di setup genera il controller per il steup altrimenti genera un controllore per il gioco

    private void buildTabs(){
        this.tabPane.getTabs().clear();

        if(this.getLocalModel() != null){

            for(LocalStationPlayer l : new ArrayList<>(this.getLocalModel().getStations().values())) {

                LocalStationController controller;
                BorderPane tabContent;

                try{
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(new File("src/main/resources/fxml/LocalStationScene.fxml").toURL());

                    switch (this.getClientController().getState()){
                        case ViewState.SETUP -> controller = new LocalStationControllerForSetup(this, l.getOwnerPlayer());
                        default -> controller = null;
                    }

                    this.localControllers.add(controller);
                    loader.setController(controller);

                    tabContent = loader.load();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Tab tab = new Tab();

                tab.setText(l.getOwnerPlayer());
                tab.setContent(tabContent);

                this.tabPane.getTabs().add(tab);
            }
        }
    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String... varArgs) {
        switch (type){
            //case LocalModelEvents.DISCONNECTED_PLAYER ->
        }
    }

    @Override
    public void notify(PersonalStation localStationPlayer) {

    }

    @Override
    public void notify(OtherStation otherStation) {

    }

    @Override
    public void notifyErrorStation(String... varArgs) {

    }
}
