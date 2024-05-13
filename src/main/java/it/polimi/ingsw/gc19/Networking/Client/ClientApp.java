package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Utils.IPChecker;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.GUIView;
import it.polimi.ingsw.gc19.View.TUI.TUIView;
import javafx.application.Application;

import java.util.Scanner;


/**
 * This is the main class for client. Using command line, it asks player
 * to insert IP for both TCP and RMI server and RMI server name.
 * Then, it asks the type of UI and starts the selected ones.
 */
public class ClientApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(ClientSettings.CODEX_NATURALIS_LOGO);

        System.out.println();

        System.out.println("Default RMI server name is: " + ClientSettings.DEFAULT_RMI_SERVER_NAME + ". Insert server RMI IP or 'default':");

        String serverRMIName = scanner.nextLine();
        if(!serverRMIName.equals("default")){
            ClientSettings.MAIN_SERVER_RMI_NAME = serverRMIName;
        }

        System.out.println("Default RMI server IP is: " + ClientSettings.RMI_SERVER_IP + ". Insert server RMI IP or 'default':");

        boolean valid = false;
        while(!valid){
            String ip = scanner.nextLine();
            if(ip.equals("default")){
                valid = true;
            }
            else{
                valid = IPChecker.checkIPAddress(ip);
                if(valid){
                    ClientSettings.RMI_SERVER_IP = ip;
                }
                else{
                    System.out.println("Error: invalid IP! Enter a valid IP or 'default':");
                }
            }
        }

        System.out.println("Default TCP server IP is: " + ClientSettings.TCP_SERVER_IP + ". Insert server RMI IP or 'default':");

        valid = false;
        while(!valid){
            String ip = scanner.nextLine();
            if(ip.equals("default")){
                valid = true;
            }
            else{
                valid = IPChecker.checkIPAddress(ip);
                if(valid){
                    ClientSettings.TCP_SERVER_IP = ip;
                }
                else{
                    System.out.println("Error: invalid IP! Enter a valid IP or 'default':");
                }
            }
        }

        String uiType;

        do {
            System.out.println("Please enter what type of user interface to use: ");
            System.out.println("- TUI");
            System.out.println("- GUI");
            uiType = scanner.nextLine();
        } while (!uiType.equalsIgnoreCase("tui") && !uiType.equalsIgnoreCase("gui"));


        switch (uiType.toLowerCase()) {
            case "tui" -> {
                new TUIView(new CommandParser(new ClientController()));
            }
            case "gui" -> Application.launch(GUIView.class);
        }
    }

}