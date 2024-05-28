package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The client is connected to the server, but it is in no game.
 * Methods to determine the available games and to join/create games are permitted.
 */
public class NotGame extends ClientState {

    private final ScheduledExecutorService gameSearcher;

    public NotGame(ClientController clientController) {
        super(clientController);
        this.gameSearcher = new ScheduledThreadPoolExecutor(1);
    }

    /**
     * This method starts a {@link ScheduledExecutorService} that periodically
     * asks server about available games.
     */
    public void startGameSearch(){
        if(!this.gameSearcher.isShutdown()){
            this.gameSearcher.scheduleAtFixedRate(() -> clientInterface.availableGames(), 0, ClientSettings.TIME_BETWEEN_CONSECUTIVE_AVAILABLE_GAMES_REQUESTS, TimeUnit.SECONDS);
        }
    }

    /**
     * This method shuts down {@link ScheduledExecutorService} that was
     * started by {@link NotGame#startGameSearch()}
     */
    public void stopGameSearch(){
        if(!this.gameSearcher.isShutdown()){
            this.gameSearcher.shutdownNow();
        }
    }

    /**
     * Getter for {@link ViewState} associated to this state
     * @return the {@link ViewState} associated to this state.
     */
    @Override
    public ViewState getState() {
        return ViewState.NOT_GAME;
    }

}