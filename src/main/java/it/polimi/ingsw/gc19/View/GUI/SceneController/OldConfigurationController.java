package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;

public class OldConfigurationController extends AbstractController{
    private Configuration config;
    private Configuration.ConnectionType connectionType;

    public void setConfig(Configuration config) {
        this.config = config;
    }
    public void setConnectionType(Configuration.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
}
