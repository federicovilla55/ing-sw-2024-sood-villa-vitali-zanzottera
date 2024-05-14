package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

import java.util.concurrent.TimeUnit;

/**
 * The client is currently disconnected from the game. A new thread is created
 * to try to reconnect to the game or the main lobby. As soon as a connection is
 * established, the client changes its state.
 */
public class Disconnect extends ClientState {
    Thread reconnectScheduler;

    public Disconnect(ClientController clientController) {
        super(clientController);
        startReconnecting();
    }

    /**
     * This method is used when a disconnected client receives a {@link JoinedGameMessage}.
     * First, it interrupts reconnection routine, then set {@link ViewState} of
     * {@link ClientController} to {@link Wait} and notifies view.
     * If <code>{@link ClientController#getLocalModel()} == null</code>, then it builds
     * a new {@link LocalModel}, sets <code>gameName</code> and <code>nickname</code>
     * and manages {@link ListenersManager}
     * @param message is the {@link JoinedGameMessage} to be handled
     */
    @Override
    public void nextState(JoinedGameMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.setNextState(new Wait(clientController), false);

        if(clientController.getLocalModel() == null){
            this.clientController.getView().notify("All has gone right. You are reconnected to server!");

            LocalModel localModel = new LocalModel();
            localModel.setListenersManager(listenersManager);
            localModel.setGameName(message.getGameName());
            localModel.setNickname(message.getHeader().getFirst());

            clientController.setLocalModel(localModel);
            clientController.getView().setLocalModel(localModel);
            clientController.getClientInterface().getMessageHandler().setLocalModel(localModel);
        }
        else{
            this.clientController.getView().notify("Network problems solved. You are reconnected to server!");
        }
    }

    /**
     * This method is used when a disconnected client receives an {@link AvailableGamesMessage}.
     * First, it interrupts reconnection routine, set {@link ViewState} of {@link ClientController}
     * to {@link NotGame} and notifies view.
     * @param message is the {@link AvailableGamesMessage} to be handled
     */
    @Override
    public void nextState(AvailableGamesMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.setNextState(new NotGame(clientController), true);

        this.clientController.getView().notify("Network problems solved. You are reconnected to server!");

        super.nextState(message);
    }

    /**
     * This method handles a {@link GameHandlingErrorMessage}. It sets
     * {@link ViewState} in {@link ClientController} to {@link NotPlayer} if {@link GameHandlingErrorMessage#getErrorType()}
     * is {@link Error#PLAYER_NAME_ALREADY_IN_USE}; otherwise it sets it to {@link NotGame}
     * if {@link GameHandlingErrorMessage#getErrorType()} is {@link Error#PLAYER_NAME_ALREADY_IN_USE} ot
     * {@link Error#GAME_NOT_FOUND}.
     * @param message the {@link GameHandlingErrorMessage} to be handled
     */
    @Override
    public void nextState(GameHandlingErrorMessage message) {
        switch (message.getErrorType()) {
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                clientController.setNextState(new NotPlayer(clientController), true);
                this.listenersManager.notifyErrorPlayerCreationListener("Player name is already in use!");
            }
            case Error.PLAYER_NOT_IN_GAME, Error.GAME_NOT_FOUND -> {
                this.listenersManager.notifyErrorPlayerCreationListener("You are not in a game! ");
                clientController.setNextState(new NotGame(clientController), true);
            }
        }
    }

    /**
     * This method handles {@link NetworkHandlingErrorMessage}. First, it stops
     * reconnection routine and then calls {@link ClientController#handleError(NetworkHandlingErrorMessage)}
     * to handle the error.
     * @param message the {@link NetworkHandlingErrorMessage} to be handled
     */
    @Override
    public void nextState(NetworkHandlingErrorMessage message) {
        reconnectScheduler.interrupt();
        clientController.handleError(message);
        /*
            FIXME: ask for CLIENT_ALREADY_CONNECTED_TO_SERVER
         */
    }

    /**
     * Getter for {@link ViewState} associated to this state
     * @return the {@link ViewState} associated to this state.
     */
    @Override
    public ViewState getState() {
        return ViewState.DISCONNECT;
    }

    /**
     * This method is used to start reconnection routine
     */
    public void startReconnecting(){
        reconnectScheduler = new Thread(this::reconnect);
        reconnectScheduler.start();
    }

    /**
     * This method performs reconnection. It tries to reconnect to server for,
     * at max, {@link ClientSettings#MAX_RECONNECTION_TRY_BEFORE_ABORTING}, waiting between one trial
     * and other for {@link ClientSettings#WAIT_BETWEEN_RECONNECTION_TRY_IN_CASE_OF_EXPLICIT_NETWORK_ERROR}.
     * For reconnection, it calls {@link ClientInterface#reconnect()}. When reconnection fails, it notifies
     * view and then executes <code>System.exit()</code>
     */
    public void reconnect() {
        int numReconnect = 0;

        while (numReconnect < ClientSettings.MAX_RECONNECTION_TRY_BEFORE_ABORTING && !Thread.currentThread().isInterrupted()) {

            try {
                clientInterface.reconnect();
            }
            catch (IllegalStateException e) {
                if(clientController.getView() != null)
                    clientController.getView().notifyGenericError("Could not reconnect, returning to the lobby");
                clientController.setNextState(new NotPlayer(clientController), true);
                Thread.currentThread().interrupt();
                return;
            }
            catch (RuntimeException e) {
                numReconnect++;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(1000 * ClientSettings.WAIT_BETWEEN_RECONNECTION_TRY_IN_CASE_OF_EXPLICIT_NETWORK_ERROR);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        if(numReconnect == ClientSettings.MAX_DISCONNECTION_TRY_IN_CASE_OF_ERROR_BEFORE_ABORTING){
            this.clientController.getView().notifyGenericError("[ERROR]: unable to reconnect to server! You are going to exit the application");

            try{
                TimeUnit.SECONDS.sleep(5);
            }
            catch (InterruptedException interruptedException){
                Thread.currentThread().interrupt();
            }

            System.exit(0);
        }
    }

}
