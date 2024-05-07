package it.polimi.ingsw.gc19.Networking.Client.Configuration;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class ConfigurationManager {

    public static void saveConfiguration(Configuration configuration) throws RuntimeException{
        File configFile, configFolder;

        try {
            configFolder = new File(ClientSettings.CONFIG_FILE_PATH);
            if(configFolder.exists()) configFolder.delete();
            configFolder.mkdir();

            configFile = new File(ClientSettings.CONFIG_FILE_PATH + configuration.getNick() + ".json");
            if(configFile.createNewFile()){
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(configFile, configuration);
            }
            else{
                throw new RuntimeException("Could not create file!");
            }
        }
        catch (IOException ioException){
            throw new RuntimeException("Could not create file!");
        };

    }

    public static Configuration retriveConfiguration(String nick) throws IllegalStateException, IOException {
        File configFile;
        Configuration configuration;

        try{
            configFile = new File(ClientSettings.CONFIG_FILE_PATH + nick + ".json");
            if(configFile.exists() && !configFile.isDirectory()){
                ObjectMapper objectMapper = new ObjectMapper();
                configuration = objectMapper.readValue(configFile, Configuration.class);
            }
            else{
                throw new IllegalStateException("Cannot retrieve configuration because configuration file specified does not exist!");
            }
        }
        catch (IOException e) {
            throw new IOException(e);
        }

        return configuration;
    }

    public static Configuration retriveConfiguration() throws RuntimeException{
        File configFile;
        Configuration configuration;

        try{
            configFile = new File(ClientSettings.CONFIG_FILE_PATH);
            if(Arrays.stream(Objects.requireNonNull(configFile.listFiles()))
                     .filter(File::isFile)
                     .filter(f -> f.getName().endsWith(".json"))
                     .count() != 1){
                throw new IllegalStateException();
            }
            else{
                ObjectMapper objectMapper = new ObjectMapper();
                configuration = objectMapper.readValue(Objects.requireNonNull(configFile.listFiles())[0], Configuration.class);
            }
        }
        catch (StreamReadException | DatabindException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new IllegalStateException();
        }

        return configuration;
    }

    public static void deleteConfiguration() throws RuntimeException{
        File configFile;

        configFile = new File(ClientSettings.CONFIG_FILE_PATH);
        if(Arrays.stream(Objects.requireNonNull(configFile.listFiles()))
                 .filter(File::isFile)
                 .filter(f -> f.getName().endsWith(".json"))
                 .count() != 1){
            throw new IllegalStateException();
        }
        else{
            if(configFile.delete()){
                System.err.println("[CONFIG]: config file correctly deleted.");
            }
        }
    }

    public static void deleteConfiguration(String nick){
        File configFile;

        configFile = new File(ClientSettings.CONFIG_FILE_PATH + nick + ".json");
        if(configFile.delete()){
            System.err.println("[CONFIG]: config file correctly created");
        }
    }


}