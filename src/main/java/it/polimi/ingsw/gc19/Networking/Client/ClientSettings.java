package it.polimi.ingsw.gc19.Networking.Client;

public class ClientSettings {
    public static String serverIP = "127.0.0.1";
    public static int serverTCPPort = 25000;
    public static int serveRMIPort = 1099;
    public static final String serverRMIName = "RMIMainServer";
    public static final long MAX_TRY_TIME_BEFORE_SIGNAL_DISCONNECTION = 10;
    public static final long MAX_RECONNECTION_TRY_BEFORE_ABORTING = 50;
    public static final String CONFIG_FILE_NAME = "Client_config";
    public static final long TIME_BETWEEN_RECONNECTIONS = 10;
}
