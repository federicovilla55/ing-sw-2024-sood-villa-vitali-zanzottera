package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OwnAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.EndGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;


/**
 * The client is waiting for a message from the server to continue playing.
 * This can be because of a place/draw he just made or because it's someone
 * else's turn.
 */
public class Wait extends ClientState {

    public Wait(ClientController clientController) {
        super(clientController);
    }

    @Override
    public void nextState(CreatedPlayerMessage message) {
        super.nextState(message);
        clientController.setNextState(new NotGame(clientController));
    }

    @Override
    public void nextState(TurnStateMessage message) {
        if (message.getNick().equals(clientController.getNickname()) && message.getTurnState() == TurnState.DRAW) {
            clientController.setNextState(new Pick(clientController));
        }
        else {
            if (!message.getNick().equals(clientController.getNickname())) {
                clientController.setNextState(new OtherTurn(clientController));
            }
        }
        this.listenersManager.notifyTurnStateListener(message.getNick(), message.getTurnState());
    }

    @Override
    public void nextState(EndGameMessage message) {
        clientController.setNextState(new End(clientController));
        super.nextState(message);
    }

    @Override
    public void nextState(OwnAcceptedPickCardFromDeckMessage message) {
        clientController.setNextState(new OtherTurn(clientController));
    }

    @Override
    public void nextState(JoinedGameMessage message) {
        buildGame(message.getGameName());
        super.nextState(message);
    }

    private void buildGame(String gameName) {
        LocalModel localModel = new LocalModel();
        localModel.setListenersManager(listenersManager);
        localModel.setNickname(this.clientInterface.getNickname());
        localModel.setGameName(gameName);
        this.clientController.setLocalModel(localModel);
        this.clientInterface.getMessageHandler().setLocalModel(localModel);

        clientController.setNextState(new Setup(clientController));
    }

    @Override
    public void nextState(CreatedGameMessage message) {
        buildGame(message.getGameName());
        super.nextState(message);
    }

    @Override
    public void nextState(GameConfigurationMessage message) {
        if (message.getGameState() == GameState.SETUP) {
            clientController.setNextState(new Setup(clientController));
        }
        else {
            if(message.getActivePlayer().equals(clientInterface.getNickname())){
                clientController.setNextState(new Place(clientController));
            }
            else{
                clientController.setNextState(new OtherTurn(clientController));
            }
        }
    }

    @Override
    public void nextState(DisconnectFromServerMessage message){
        super.nextState(message);
        this.clientInterface.getMessageHandler().interruptMessageHandler();
        this.clientInterface.stopClient();
    }

    @Override
    public void nextState(DisconnectFromGameMessage message) {
        super.nextState(message);
        this.clientController.setLocalModel(null);
        this.clientInterface.getMessageHandler().setLocalModel(null);
        this.clientController.setNextState(new NotGame(clientController));
    }

    @Override
    public ViewState getState() {
        return ViewState.WAIT;
    }

}
