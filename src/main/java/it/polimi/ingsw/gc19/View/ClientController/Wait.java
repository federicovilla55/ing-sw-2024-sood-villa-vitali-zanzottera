package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OwnAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.EndGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;


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
        this.clientInterface.configure(message.getNick(), message.getToken());
        this.listenersManager.notifyPlayerCreationListener(message.getNick());
        clientController.setNextState(new NotGame(clientController), true);
    }

    @Override
    public void nextState(TurnStateMessage message) {
        if (message.getNick().equals(clientController.getNickname()) && message.getTurnState() == TurnState.DRAW) {
            clientController.setNextState(new Pick(clientController), true);
        }
        else {
            if (!message.getNick().equals(clientController.getNickname())) {
                clientController.setNextState(new OtherTurn(clientController), true);
            }
        }
    }

    @Override
    public void nextState(EndGameMessage message) {
        clientController.setNextState(new End(clientController), true);
        super.nextState(message);
    }

    @Override
    public void nextState(OwnAcceptedPickCardFromDeckMessage message) {
        clientController.setNextState(new OtherTurn(clientController), true);
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
        this.clientController.getView().setLocalModel(localModel);
    }

    @Override
    public void nextState(CreatedGameMessage message) {
        buildGame(message.getGameName());
        super.nextState(message);
    }

    @Override
    public void nextState(DisconnectFromServerMessage message){
        super.nextState(message);
        this.clientInterface.getMessageHandler().interruptMessageHandler();
        this.clientInterface.stopClient();
    }

    @Override
    public void nextState(OwnStationConfigurationMessage message){
        if(clientController.getLocalModel() == null){
            LocalModel localModel = new LocalModel();

            localModel.setListenersManager(listenersManager);
            localModel.setNickname(message.getNick());

            clientController.setLocalModel(localModel);
            clientController.getView().setLocalModel(localModel);
            clientController.getClientInterface().getMessageHandler().setLocalModel(localModel);
        }

        this.clientController.getLocalModel().setPersonalStation(new PersonalStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                                                               message.getNumPoints(), message.getPlacedCardSequence(), message.getPrivateGoalCard(),
                                                               message.getGoalCard1(), message.getGoalCard2(), message.getCardsInHand(), message.getInitialCard()));
    }

    @Override
    public ViewState getState() {
        return ViewState.WAIT;
    }

}
