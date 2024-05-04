package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * The client is currently disconnected from the game. A new thread is created
 * to try to reconnect to the game or the main lobby. As soon as a connection is
 * established, the client changes its state.
 */
class Disconnect extends ClientState {
    Thread reconnectScheduler;

    public Disconnect(ClientController clientController, ClientInterface clientInterface) {
        super(clientController, clientInterface);
        startReconnecting();
    }

    @Override
    public void nextState(JoinedGameMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.viewState = new Wait(clientController, clientInterface);
    }

    //When we receive correct message from server, we notify client to start sending heartbeat
    @Override
    public void nextState(AvailableGamesMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.viewState = new NotGame(clientController, clientInterface);
    }

    @Override
    public void nextState(GameHandlingErrorMessage message) {
        switch (message.getErrorType()) {
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                clientController.viewState = new NotPlayer(clientController, clientInterface);
            }
            case Error.PLAYER_NOT_IN_GAME -> {
                clientController.viewState = new Disconnect(clientController, clientInterface);
            }
            default -> {
                clientController.viewState = new NotGame(clientController, clientInterface);
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
        if (clientController.isClientNetworkSet()) {
            reconnectScheduler = new Thread(this::reconnect);
            reconnectScheduler.start();
        }
    }

    public void reconnect() {
        int numReconnect = 0;

        while (numReconnect < ClientSettings.MAX_RECONNECTION_TRY_BEFORE_ABORTING && !Thread.currentThread().isInterrupted()) {
            try {
                clientInterface.reconnect();
            } catch (IllegalStateException e) {
                // Token file not found...
                clientController.viewState = new NotPlayer(clientController, clientInterface);
                reconnectScheduler.interrupt();
                return;
            } catch (RuntimeException e) {
                numReconnect++;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(ClientSettings.MAX_TRY_TIME_BEFORE_SIGNAL_DISCONNECTION * 1000);
            } catch (InterruptedException e) {
                reconnectScheduler.interrupt();
                return;
            }
        }

    }
}
