package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OwnAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.EndGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;


/**
 * This class represents a state where client is waiting for a message from the server to continue playing.
 * This can be because of a place/draw he just made or because it's someone else's turn.
 */
public class Wait extends ClientState {

    public Wait(ClientController clientController) {
        super(clientController);
    }

    /**
     * This method handles {@link CreatedPlayerMessage}. It sets {@link ViewState} of
     * {@link ClientController} to {@link NotGame} and configures "network interface"
     * associated to that player.
     * @param message is the {@link CreatedPlayerMessage} to be handled
     */
    @Override
    public void nextState(CreatedPlayerMessage message) {
        this.clientInterface.configure(message.getNick(), message.getToken());
        this.listenersManager.notifyPlayerCreationListener(message.getNick());
        clientController.setNextState(new NotGame(clientController), true);
    }

    /**
     * This method handles {@link TurnStateMessage}. It sets {@link ViewState} of
     * {@link ClientController} to {@link Pick} or {@link OtherTurn} depending on {@param message}
     * @param message the {@link TurnStateMessage} to be handled
     */
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

    /**
     * This method handles {@link EndGameMessage}. It sets {@link ViewState} of 
     * {@link ClientController} to {@link End}.
     * @param message the {@link EndGameMessage} to be handled
     */
    @Override
    public void nextState(EndGameMessage message) {
        super.nextState(message);
        clientController.setNextState(new End(clientController), true);
    }

    /**
     * This method handle {@link OwnAcceptedPickCardFromDeckMessage}. It sets {@link ViewState} in
     * {@link ClientController} to {@link OtherTurn}
     * @param message the {@link OwnAcceptedPickCardFromDeckMessage} to be handled
     */
    @Override
    public void nextState(OwnAcceptedPickCardFromDeckMessage message) {
        clientController.setNextState(new OtherTurn(clientController), true);
    }

    /**
     * This method handles {@link CreatedGameMessage}. It builds a new {@link LocalModel}
     * and updates state inside {@link ClientController}
     * @param message the {@link CreatedGameMessage} to be handled
     */
    @Override
    public void nextState(JoinedGameMessage message) {
        buildGame(message.getGameName());
        super.nextState(message);
    }

    /**
     * This method builds and set locally a new {@link LocalModel}.
     * @param gameName is the name of the game to build
     */
    private void buildGame(String gameName) {
        LocalModel localModel = new LocalModel();
        localModel.setListenersManager(listenersManager);
        localModel.setNickname(this.clientInterface.getNickname());
        localModel.setGameName(gameName);

        this.clientController.setLocalModel(localModel);
        this.clientInterface.getMessageHandler().setLocalModel(localModel);
        if(this.clientController.getView() != null) {
            this.clientController.getView().setLocalModel(localModel);
        }
    }


    /**
     * This method handles {@link CreatedGameMessage}. It builds a new {@link LocalModel}
     * and updates state inside {@link ClientController}
     * @param message the {@link CreatedGameMessage} to be handled
     */
    @Override
    public void nextState(CreatedGameMessage message) {
        buildGame(message.getGameName());
        super.nextState(message);
    }

    /**
     * Getter for {@link ViewState} associated to this state
     * @return the {@link ViewState} associated to the state
     */
    @Override
    public ViewState getState() {
        return ViewState.WAIT;
    }

}
