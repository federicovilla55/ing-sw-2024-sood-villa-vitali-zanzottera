package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static it.polimi.ingsw.gc19.Networking.Client.Configuration.ConnectionType.RMI;
import static it.polimi.ingsw.gc19.Networking.Client.Configuration.ConnectionType.SOCKET;

public class ClientApp {

    public static void main(String[] args) {

        System.out.println("Welcome to Codex Naturalis!");

        Configuration config;
        ClientController clientController = new ClientController();
        ClientInterface client;
        try {
            config = ConfigurationManager.retriveConfiguration();

            System.out.println("Configuration found for player " + config.getNick());
            System.out.println("Do you want to reconnect? (y/n)");
            Scanner scanner = new Scanner(System.in);
            boolean reconnect = scanner.next().equalsIgnoreCase("y");

            if (reconnect) {
                ClientFactory clientFactory = switch (config.getConnectionType()) {
                    case RMI -> new ClientRMIFactory();
                    case TCP -> new ClientTCPFactory();
                };

                try {
                    client = clientFactory.createClient();
                    clientController.setClientInterface(client);
                    client.configure(config.getNick(), config.getToken());
                    System.out.println("Config found");
                    client.reconnect();
                } catch (IOException e) {
                    System.err.println("Unable to create client! Error was:");
                    System.err.println(e.getMessage());
                }
            } else {
                client = chooseClient();
            }

        } catch (RuntimeException e) {
            System.out.println("No valid configuration found! Creating a new client...");
            client = chooseClient();

        }

        String uiType;
        do {
            System.out.println("Please enter what type of user interface to use: ");
            System.out.println("- TUI");
            System.out.println("- GUI");
            Scanner scanner = new Scanner(System.in);
            uiType = scanner.nextLine();
        } while (!uiType.equalsIgnoreCase("tui") && !uiType.equalsIgnoreCase("gui"));

//
//        UIFactory uiFactory = null;
//        switch (uiType.toLowerCase()) {
//            case "tui":
//                uiFactory = new TUIFactory();
//                break;
//            case "gui":
//                uiFactory = new GUIFactory();
//                break;
//        }


    }

    private static ClientInterface chooseClient() {
        String connectionType;
        do {
            System.out.println("Please enter what type of connection you want to use to connect to game server: ");
            System.out.println("- RMI");
            System.out.println("- Socket");
            Scanner scanner = new Scanner(System.in);
            connectionType = scanner.nextLine();
        } while (!connectionType.equalsIgnoreCase("rmi") && !connectionType.equalsIgnoreCase("socket"));

        ClientFactory clientFactory = switch (Configuration.ConnectionType.valueOf(connectionType.toUpperCase())) {
            case RMI -> new ClientRMIFactory();
            case TCP -> new ClientTCPFactory();
        };

        ClientInterface client = null;

        try {
            client = clientFactory.createClient();
        } catch (IOException e) {
            System.err.println("Unable to create client! Error was:");
            System.err.println(e.getMessage());

        }

        return client;
    }
}
