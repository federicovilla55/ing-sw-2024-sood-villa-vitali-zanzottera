package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

import java.util.concurrent.TimeUnit;

/**
 * The client is currently disconnected from the game. A new thread is created
 * to try to reconnect to the game or the main lobby. As soon as a connection is
 * established, the client changes its state.
 */
class Disconnect extends ClientState {

    Thread reconnectScheduler;

    public Disconnect(ClientController clientController, ClientInterface clientInterface, ListenersManager listenersManager) {
        super(clientController, clientInterface, listenersManager);
        startReconnecting();
    }

    @Override
    public void nextState(JoinedGameMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.setNextState(new Wait(clientController, clientInterface, listenersManager));
    }

    @Override
    public void nextState(AvailableGamesMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.setNextState(new NotGame(clientController, clientInterface, listenersManager));
        super.nextState(message);
    }

    @Override
    public void nextState(GameHandlingErrorMessage message) {
        switch (message.getErrorType()) {
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                clientController.setNextState(new NotPlayer(clientController, clientInterface, listenersManager));
            }
            case Error.PLAYER_NOT_IN_GAME -> {
                clientController.setNextState(new Disconnect(clientController, clientInterface, listenersManager));
            }
            default -> {
                clientController.setNextState(new NotGame(clientController, clientInterface, listenersManager));
            }
        }
    }

    @Override
    public void nextState(NetworkHandlingErrorMessage message) {
        //@TODO: handle better the error?
        clientController.handleError(message);
    }

    @Override
    public ViewState getState() {
        return ViewState.DISCONNECT;
    }

    public void startReconnecting(){
        reconnectScheduler = new Thread(this::reconnect);
        reconnectScheduler.start();
    }

    public void reconnect() {
        int numReconnect = 0;

        while (numReconnect < ClientSettings.MAX_RECONNECTION_TRY_BEFORE_ABORTING && !Thread.currentThread().isInterrupted()) {

            try {
                clientInterface.reconnect();
            }
            catch (IllegalStateException e) {
                //@TODO: view
                clientController.setNextState(new NotPlayer(clientController, clientInterface, listenersManager));
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
    }

}
