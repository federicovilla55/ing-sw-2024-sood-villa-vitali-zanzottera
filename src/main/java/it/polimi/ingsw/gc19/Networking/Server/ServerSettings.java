package it.polimi.ingsw.gc19.Networking.Server;

/**
 * Server settings.
 */
public class ServerSettings {

    public static long MAX_DELTA_TIME_BETWEEN_HEARTBEATS = 10;
    public static long TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = 60;
    public static long TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL =  20 * 60;

    public static final String DEFAULT_RMI_SERVER_NAME = "RMIMainServer";
    public static String MAIN_RMI_SERVER_NAME = ServerSettings.DEFAULT_RMI_SERVER_NAME;

    public static final String DEFAULT_SERVER_IP = "127.0.0.1";
    public static String MAIN_TCP_SERVER_IP = ServerSettings.DEFAULT_SERVER_IP;
    public static String MAIN_RMI_SERVER_IP = ServerSettings.DEFAULT_SERVER_IP;

    public static int TCP_SERVER_PORT = ServerSettings.DEFAULT_TCP_SERVER_PORT;
    public static int RMI_SERVER_PORT = ServerSettings.DEFAULT_RMI_SERVER_PORT;
    public static final int DEFAULT_TCP_SERVER_PORT = 25000;
    public static final int DEFAULT_RMI_SERVER_PORT = 1099;

}