package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

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

    @Override
    public void nextState(JoinedGameMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.setNextState(new Wait(clientController));

        this.clientController.getView().notify("Network problems solved. You are reconnected to server!");
    }

    @Override
    public void nextState(AvailableGamesMessage message) {
        reconnectScheduler.interrupt();
        super.clientInterface.startSendingHeartbeat();
        clientController.setNextState(new NotGame(clientController));

        this.clientController.getView().notify("Network problems solved. You are reconnected to server!");

        super.nextState(message);
    }

    @Override
    public void nextState(GameHandlingErrorMessage message) {
        switch (message.getErrorType()) {
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                clientController.setNextState(new NotPlayer(clientController));

                this.listenersManager.notifyErrorPlayerCreationListener("Player name is already in use!");
            }
            case Error.PLAYER_NOT_IN_GAME, Error.GAME_NOT_FOUND -> {
                this.listenersManager.notifyErrorPlayerCreationListener("You are not in a game! ");
                clientController.setNextState(new NotGame(clientController));
            }
            /*default -> {
                clientController.setNextState(new NotGame(clientController));
            }*/
        }
    }

    @Override
    public void nextState(NetworkHandlingErrorMessage message) {
        reconnectScheduler.interrupt();
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
                clientController.getView().notifyGenericError("Could not reconnect, returning to the lobby");
                clientController.setNextState(new NotPlayer(clientController));
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
