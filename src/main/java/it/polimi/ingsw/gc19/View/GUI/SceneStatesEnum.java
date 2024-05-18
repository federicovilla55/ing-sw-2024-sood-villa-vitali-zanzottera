package it.polimi.ingsw.gc19.View.GUI;

import it.polimi.ingsw.gc19.View.Listeners.ListenerType;

import java.util.List;

public enum SceneStatesEnum {
    StartScene("src/main/resources/fxml/Start.fxml", "", List.of(ListenerType.STATE_LISTENER)),
    LoginScene("src/main/resources/fxml/Login.fxml", "", List.of(ListenerType.STATE_LISTENER,
            ListenerType.PLAYER_CREATION_LISTENER)),
    GameSelectionScene("src/main/resources/fxml/GameSelection.fxml", "",  List.of(ListenerType.STATE_LISTENER,
            ListenerType.GAME_HANDLING_EVENTS_LISTENER)),
    PlayingAreaScene("src/main/resources/fxml/PlayingArea.fxml", "",  List.of(ListenerType.STATE_LISTENER)),
    NewConfigurationScene( "src/main/resources/fxml/NewConfiguration.fxml", "",  List.of(ListenerType.STATE_LISTENER)),
    OldConfigurationScene("src/main/resources/fxml/OldConfiguration.fxml", "",  List.of(ListenerType.STATE_LISTENER,
            ListenerType.GAME_HANDLING_EVENTS_LISTENER)),
    SETUP_SCENE("src/main/resources/fxml/SetupScene.fxml", "",  List.of(ListenerType.STATE_LISTENER));

    private final String value;
    private final String cssPath;
    private final List<ListenerType> listeners;
    SceneStatesEnum(final String value, String cssPath, List<ListenerType> listeners) {
        this.value = value;
        this.cssPath = cssPath;
        this.listeners = listeners;
    }


    public String value() {
        return value;
    }
    public String getCssPath(){return cssPath;}
    public List<ListenerType> getListeners(){return listeners;}

}
