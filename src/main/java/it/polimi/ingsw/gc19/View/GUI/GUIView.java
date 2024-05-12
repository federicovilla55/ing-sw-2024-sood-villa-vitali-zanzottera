package it.polimi.ingsw.gc19.View.GUI;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.Disconnect;
import it.polimi.ingsw.gc19.View.ClientController.NotPlayer;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.TUI.TUIView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class GUIView extends Application {

    private LocalModel localModel;
    private final CommandParser commandParser;
    private final ClientController clientController;

    private void nextSceneSelector()
    {
        Configuration config;
        Configuration.ConnectionType connectionType;
        String reconnectChoice;

        ClientInterface client;
        try {
            config = ConfigurationManager.retriveConfiguration();
            connectionType = config.getConnectionType();

            do {
                System.out.println("Configuration found. Printing infos about last interaction with server:");

                System.out.println("-> nickname: " + config.getNick());
                System.out.println("-> timestamp: " + config.getTimestamp());
                System.out.println("-> connection type: " + config.getConnectionType());

                System.out.println("do you want to try to reconnect? (s/n) ");

                Scanner scanner = new Scanner(System.in);
                reconnectChoice = scanner.nextLine();
            } while(!reconnectChoice.equalsIgnoreCase("s") && !reconnectChoice.equalsIgnoreCase("n"));

            if(reconnectChoice.equalsIgnoreCase("s")) {
                client = connectionType.getClientFactory().createClient(clientController);
                client.configure(config.getNick(), config.getToken());
                clientController.setNickname(config.getNick());
                clientController.setClientInterface(client);
                clientController.setNextState(new Disconnect(clientController));
            }
            else{
                ConfigurationManager.deleteConfiguration();
            }

        } catch (RuntimeException e) {
            System.out.println("No valid configuration found... creating new client");
            reconnectChoice = "n";
        } catch (IOException e) {
            System.out.println("Error while creating client... aborting");
            System.exit(1);
            return;
        }

        if(reconnectChoice.equalsIgnoreCase("n")) {
            /*connectionType = chooseClientType();
            try {
                client = connectionType.getClientFactory().createClient(clientController);
            } catch (IOException e) {
                System.out.println("Error while creating client... aborting");
                System.exit(1);
                return;
            }
            clientController.setClientInterface(client);
            System.out.println("Successfully connected to the server!");
            System.out.print("> ");
            clientController.setNextState(new NotPlayer(clientController));
        }

        Scanner scanner = new Scanner(System.in);

        while (!Thread.currentThread().isInterrupted()){
            String command = scanner.nextLine();

            this.parseCommand(command);*/
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

        /*stage.setScene(new Scene(createContent(), 300, 300));
        File url = new File("src/main/resources/fxml/PROVA.fxml");
        Parent root = FXMLLoader.load(url.toURL());
        stage.setScene(new Scene(root, 300, 275));
        stage.show();*/
    }

    public GUIView(CommandParser commandParser) {
        this.commandParser = commandParser;
        this.clientController = commandParser.clientController();
        //this.clientController.getListenersManager().attachListener(this);
        //this.clientController.setView(this);

        this.localModel = null;
        //this.showState = TUIView.ShowState.NOT_PLAYING;
        //this.currentViewPlayer = "";*/
        launch();
    }


}
