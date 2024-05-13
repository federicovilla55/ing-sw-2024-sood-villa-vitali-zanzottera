package it.polimi.ingsw.gc19.Utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utility class is used to check IP and ports
 */
public class IPChecker {

    /**
     * This method is used to check IP address
     * @param ip a {@link String} representing the IP
     * @return <code>true</code> if and only if IP is correct
     */
    public static boolean checkIPAddress(String ip){
        Pattern pattern = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
        Matcher matcher = pattern.matcher(ip);

        if(matcher.matches()){
            String[] ipFields = ip.split("\\.");
            return Arrays.stream(ipFields).noneMatch(s -> Integer.parseInt(s) < 0 || Integer.parseInt(s) > 255);
        }

        return false;
    }

    /**
     * This method is used to check port
     * @param port the port to check
     * @return <code>true</code> if and only if the specified port is correct
     * (e.g. is between 0 and 65353)
     */
    public static boolean checkPort(String port){
        int portToCheck;

        try{
            portToCheck = Integer.parseInt(port);
        }
        catch (NumberFormatException numberFormatException){
            return false;
        }

        return portToCheck >= 0 && portToCheck <= 65353;
    }

}
