package it.polimi.ingsw.gc19.Networking.Client;

public class ClientSettings {
    public static final long MAX_TRY_TIME_BEFORE_SIGNAL_DISCONNECTION = 2;
    public static String serverIP = "127.0.0.1";
    public static int serverTCPPort = 25000;
    public static final String serverRMIName = "RMIMainServer";
    public static final long MAX_RECONNECTION_TRY_BEFORE_ABORTING = 50;
    public static final String CONFIG_FILE_PATH = "src/main/java/it/polimi/ingsw/gc19/Networking/Client/Configuration/Local Config/";
}
