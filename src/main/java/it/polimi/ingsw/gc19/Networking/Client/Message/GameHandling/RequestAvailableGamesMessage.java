package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class RequestAvailableGamesMessage extends GameHandlingMessage{
    protected RequestAvailableGamesMessage(String nickname) {
        super(nickname);
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {

    }
}
