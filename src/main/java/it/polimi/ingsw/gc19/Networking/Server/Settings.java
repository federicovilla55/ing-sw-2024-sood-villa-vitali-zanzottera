package it.polimi.ingsw.gc19.Networking.Server;

public class Settings {
    private static Settings instance;
    public static final long MAX_DELTA_TIME_BETWEEN_HEARTBEATS = 1;
    public static final long TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = 3;
    public static final String mainRMIServerName = "RMIMainServer";
    public static final String DEFAULT_SERVER_IP = "127.0.0.1";
    public static final int DEFAULT_SERVER_PORT = 25005;

    private Settings(){

    }

    public static synchronized Settings getInstance(){
        if(instance == null){
            instance = new Settings();
        }
        return instance;
    }


}
