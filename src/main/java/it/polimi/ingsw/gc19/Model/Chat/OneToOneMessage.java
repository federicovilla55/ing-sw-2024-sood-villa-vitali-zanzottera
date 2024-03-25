package it.polimi.ingsw.gc19.Model.Chat;

import it.polimi.ingsw.gc19.Model.Game.Player;

public class OneToOneMessage extends Message{
    private final String receiverPlayer;

    public OneToOneMessage(String message, String senderPlayer, String receiverPlayer){
        super(message, senderPlayer);
        this.receiverPlayer = receiverPlayer;
    }

    @Override
    public String getReceivers(){
        return this.receiverPlayer;
    }

}
