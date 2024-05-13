package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The client is connected to the server, but it is in no game.
 * Methods to determine the available games and to join/create games are permitted.
 */
class NotGame extends ClientState {

    private final ScheduledExecutorService gameSearcher;

    public NotGame(ClientController clientController) {
        super(clientController);
        this.gameSearcher = new ScheduledThreadPoolExecutor(1);
    }

    public void startGameSearch(){
        if(!this.gameSearcher.isShutdown()){
            this.gameSearcher.scheduleAtFixedRate(() -> clientInterface.availableGames(), 0, ClientSettings.TIME_BETWEEN_CONSECUTIVE_AVAILABLE_GAMES_REQUESTS, TimeUnit.SECONDS);
        }
    }

    public void stopGameSearch(){
        if(!this.gameSearcher.isShutdown()){
            this.gameSearcher.shutdownNow();
        }
    }

    @Override
    public ViewState getState() {
        return ViewState.NOT_GAME;
    }

}
