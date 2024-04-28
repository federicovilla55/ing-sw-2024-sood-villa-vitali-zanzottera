package it.polimi.ingsw.gc19.Networking.Server;

public class Settings {
    private static Settings instance;
    public static final long MAX_DELTA_TIME_BETWEEN_HEARTBEATS = 1;
    public static final long TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = 3;
    public static long TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL =  20 * 60;
    public static final String mainRMIServerName = "RMIMainServer";
    public static final String DEFAULT_SERVER_IP = "127.0.0.1";
    public static final int DEFAULT_TCP_SERVER_PORT = 25000;
    public static final int DEFAULT_RMI_SERVER_PORT = 1099;

}
