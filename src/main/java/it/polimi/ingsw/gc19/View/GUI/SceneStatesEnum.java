package it.polimi.ingsw.gc19.View.GUI;

/**
 * Enumeration of all the (sub-) scene FXML locations.
 */
public enum SceneStatesEnum {
    START_SCENE("it/polimi/ingsw/gc19/fxml/Start.fxml"),
    LOGIN_SCENE("it/polimi/ingsw/gc19/fxml/Login.fxml"),
    GAME_SELECTION_SCENE("it/polimi/ingsw/gc19/fxml/GameSelection.fxml"),
    PLAYING_AREA_SCENE("it/polimi/ingsw/gc19/fxml/PlayingArea.fxml"),
    NEW_CONFIGURATION_SCENE("it/polimi/ingsw/gc19/fxml/NewConfiguration.fxml"),
    OLD_CONFIGURATION_SCENE("it/polimi/ingsw/gc19/fxml/OldConfiguration.fxml"),
    SETUP_SCENE("it/polimi/ingsw/gc19/fxml/SetupScene.fxml"),
    RECONNECTION_SCENE("it/polimi/ingsw/gc19/fxml/ReconnectionWaitScene.fxml"),
    CHAT_SUB_SCENE("it/polimi/ingsw/gc19/fxml/ChatScene.fxml"),
    TABLE_SUB_SCENE("it/polimi/ingsw/gc19/fxml/TableScene.fxml"),
    GAME_INFOS_SUB_SCENE("it/polimi/ingsw/gc19/fxml/GameInformationScene.fxml"),
    LOCAL_STATION_SUB_SCENE("it/polimi/ingsw/gc19/fxml/LocalStationScene.fxml"),
    LOCAL_STATION_TAB_SUB_SCENE("it/polimi/ingsw/gc19/fxml/LocalStationTab.fxml");

    private final String value;

    SceneStatesEnum(final String value) {
        this.value = value;
    }

    /**
     * Getter for {@link #value} associated to enum
     * @return the relative {@link String} path to the FXML file.
     */
    public String value() {
        return value;
    }

}