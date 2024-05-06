package it.polimi.ingsw.gc19.Networking.Client;

public class ClientSettings {

    public static final long MAX_TIME_BETWEEN_SERVER_HEARTBEAT_BEFORE_SIGNALING_NETWORK_PROBLEMS = 30;
    public static final long WAIT_BETWEEN_RECONNECTION_TRY_IN_CASE_OF_EXPLICIT_NETWORK_ERROR = 5;
    public static final long MAX_RECONNECTION_TRY_BEFORE_ABORTING = 50;
    public static String DEFAULT_SERVER_IP = "127.0.0.1";
    public static int DEFAULT_TCP_SERVER_PORT = 25000;
    public static final String MAIN_SERVER_RMI_NAME = "RMIMainServer";
    public static final String CONFIG_FILE_PATH = "src/main/java/it/polimi/ingsw/gc19/Networking/Client/Configuration/Local Config/";
}
