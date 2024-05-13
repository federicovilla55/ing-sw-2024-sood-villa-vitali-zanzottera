package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;

public class ClientSettings {

    public static final long MAX_TIME_BETWEEN_SERVER_HEARTBEAT_BEFORE_SIGNALING_NETWORK_PROBLEMS = 30;
    public static final long WAIT_BETWEEN_RECONNECTION_TRY_IN_CASE_OF_EXPLICIT_NETWORK_ERROR = 20;
    public static final long MAX_RECONNECTION_TRY_BEFORE_ABORTING = 50;
    public static final long DELTA_TIME_BETWEEN_DISCONNECTION_TRY_IN_CASE_OF_ERROR = 250;
    public static final long DELTA_TIME_BETWEEN_LOGOUT_TRY_IN_CASE_OF_ERROR = 250;
    public static final long MAX_LOGOUT_TRY_IN_CASE_OF_ERROR_BEFORE_ABORTING = 50;
    public static final long MAX_DISCONNECTION_TRY_IN_CASE_OF_ERROR_BEFORE_ABORTING = 50;

    public static String TCP_SERVER_IP = ServerSettings.DEFAULT_SERVER_IP;
    public static String RMI_SERVER_IP = ServerSettings.DEFAULT_SERVER_IP;
    public static int SERVER_RMI_PORT = ServerSettings.DEFAULT_RMI_SERVER_PORT;
    public static int SERVER_TCP_PORT = ServerSettings.DEFAULT_TCP_SERVER_PORT;

    public static String DEFAULT_RMI_SERVER_NAME = ServerSettings.DEFAULT_RMI_SERVER_NAME;
    public static String MAIN_SERVER_RMI_NAME = ClientSettings.DEFAULT_RMI_SERVER_NAME;

    public static final String CONFIG_FILE_PATH = "src/main/java/it/polimi/ingsw/gc19/Networking/Client/Configuration/Local Config/";

    public static final String CODEX_NATURALIS_LOGO = "\n" +
       " ██████╗ ██████╗ ██████╗ ███████╗██╗  ██╗    ███╗   ██╗ █████╗ ████████╗██╗   ██╗██████╗  █████╗ ██╗     ██╗███████╗\n" +
       "██╔════╝██╔═══██╗██╔══██╗██╔════╝╚██╗██╔╝    ████╗  ██║██╔══██╗╚══██╔══╝██║   ██║██╔══██╗██╔══██╗██║     ██║██╔════╝\n" +
       "██║     ██║   ██║██║  ██║█████╗   ╚███╔╝     ██╔██╗ ██║███████║   ██║   ██║   ██║██████╔╝███████║██║     ██║███████╗\n" +
       "██║     ██║   ██║██║  ██║██╔══╝   ██╔██╗     ██║╚██╗██║██╔══██║   ██║   ██║   ██║██╔══██╗██╔══██║██║     ██║╚════██║\n" +
       "╚██████╗╚██████╔╝██████╔╝███████╗██╔╝ ██╗    ██║ ╚████║██║  ██║   ██║   ╚██████╔╝██║  ██║██║  ██║███████╗██║███████║\n" +
       " ╚═════╝ ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝    ╚═╝  ╚═══╝╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚═╝╚══════╝\n" +
       "                                                                                                                    \n";

    public static final long TIME_BETWEEN_CONSECUTIVE_AVAILABLE_GAMES_REQUESTS = 10;

}
