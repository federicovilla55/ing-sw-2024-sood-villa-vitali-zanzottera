package it.polimi.ingsw.gc19.Networking.Server;

public class Settings {
    private static Settings instance;
    public static final long MAX_DELTA_TIME_BETWEEN_HEARTBEATS = 10;
    public static final long TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = 6;
    public static final String mainRMIServerName = "RMIMainServer";

    private Settings(){

    }

    public static synchronized Settings getInstance(){
        if(instance == null){
            instance = new Settings();
        }
        return instance;
    }


}
