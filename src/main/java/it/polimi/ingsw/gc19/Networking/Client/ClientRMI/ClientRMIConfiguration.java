package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Networking.Client.ClientConfiguration;

public class ClientRMIConfiguration implements ClientConfiguration{

    private final String nick;
    private final String token;

    public ClientRMIConfiguration(String nick, String token){
        this.nick = nick;
        this.token = token;
    }


}
