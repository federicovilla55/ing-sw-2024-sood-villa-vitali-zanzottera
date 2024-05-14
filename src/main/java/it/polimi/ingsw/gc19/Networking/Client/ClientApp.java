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

        System.out.println("Insert server RMI IP (" + ClientSettings.DEFAULT_RMI_SERVER_NAME +"):");

        String serverRMIName = scanner.nextLine();
        if(!serverRMIName.isEmpty()){
            ClientSettings.MAIN_SERVER_RMI_NAME = serverRMIName;
        }

        System.out.println("Insert server RMI IP (" + ClientSettings.RMI_SERVER_IP + "):");

        boolean valid = false;
        while(!valid){
            String ip = scanner.nextLine();
            if(ip.isEmpty()){
                valid = true;
            }
            else{
                valid = IPChecker.checkIPAddress(ip);
                if(valid){
                    ClientSettings.RMI_SERVER_IP = ip;
                }
                else{
                    System.out.println("Error: invalid IP! Enter a valid IP':");
                }
            }
        }

        System.out.println("Insert server Socket IP (" + ClientSettings.TCP_SERVER_IP + "):");

        valid = false;
        while(!valid){
            String ip = scanner.nextLine();
            if(ip.isEmpty()){
                valid = true;
            }
            else{
                valid = IPChecker.checkIPAddress(ip);
                if(valid){
                    ClientSettings.TCP_SERVER_IP = ip;
                }
                else{
                    System.out.println("Error: invalid IP! Enter a valid IP:");
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