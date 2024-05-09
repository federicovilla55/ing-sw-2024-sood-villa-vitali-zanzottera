package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GUI.GUIView;
import it.polimi.ingsw.gc19.View.TUI.TUIView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Scanner;

public class ClientApp {

    public static void main(String[] args) {

        System.out.println("Welcome to Codex Naturalis!");

        String uiType;

        do {
            System.out.println("Please enter what type of user interface to use: ");
            System.out.println("- TUI");
            System.out.println("- GUI");
            Scanner scanner = new Scanner(System.in);
            uiType = scanner.nextLine();
        } while (!uiType.equalsIgnoreCase("tui") && !uiType.equalsIgnoreCase("gui"));


        switch (uiType.toLowerCase()) {
            case "tui" -> {
                new TUIView(new CommandParser(new ClientController())).start();
            }
            case "gui" -> Application.launch(GUIView.class);
        }
    }
}
