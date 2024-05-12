package it.polimi.ingsw.gc19.View.GUI.SceneController;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.SceneStatesConst;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;

public class AbstractController {
    private LocalModel localModel;
    private CommandParser commandParser;
    private ClientController clientController;
    private SceneStatesConst scenePath;

    public void setLocalModel(LocalModel localModel){
        this.localModel = localModel;
    }

    public void setCommandParser(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    public  void setScenePath(SceneStatesConst scenePath) {
        this.scenePath = scenePath;
    }

    public ClientController getClientController() {
        return clientController;
    }

    public CommandParser getCommandParser() {
        return commandParser;
    }

    public LocalModel getLocalModel() {
        return localModel;
    }

    public SceneStatesConst getScenePath() {
        return scenePath;
    }
}
