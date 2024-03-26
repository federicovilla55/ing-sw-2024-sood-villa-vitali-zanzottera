package it.polimi.ingsw.gc19.Model.Chat;

import it.polimi.ingsw.gc19.Model.Game.Player;

import java.util.ArrayList;
import java.util.List;

public class OneToMoreMessage extends Message{

    private final ArrayList<String> receiverPlayers;

    public OneToMoreMessage(String message, String senderPlayer, String ... players){
        super(message, senderPlayer);
        this.receiverPlayers = new ArrayList<>(List.of(players));
    }

    @Override
    public String getReceivers(){
        StringBuilder receivers = new StringBuilder();
        for(String receiversName : this.receiverPlayers){
            receivers.append(receiversName).append(", ");
        }
        return receivers.toString();
    }

}
