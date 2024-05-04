package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;

import java.util.ArrayList;

/**
 * The client is connected to the server, but it is in no game.
 * Methods to determine the available games and to join/create games are permitted.
 */
class NotGame extends ClientState {

    public NotGame(ClientController clientController, ClientInterface clientInterface) {
        super(clientController, clientInterface);
    }

    @Override
    public ViewState getState() {
        return ViewState.NOT_GAME;
    }

}
