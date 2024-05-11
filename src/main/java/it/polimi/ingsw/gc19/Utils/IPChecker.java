package it.polimi.ingsw.gc19.Utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPChecker {

    public static boolean checkIPAddress(String ip){
        Pattern pattern = Pattern.compile("^([0-9]{1,3}\\.){3,3}[0-9]{1,3}$");
        Matcher matcher = pattern.matcher(ip);

        if(matcher.matches()){
            String[] ipFields = ip.split("\\.");
            return Arrays.stream(ipFields).noneMatch(s -> Integer.parseInt(s) < 0 || Integer.parseInt(s) > 255);
        }

        return false;

    }

    public static boolean checkPort(String port){
        int portToCheck;

        try{
            portToCheck = Integer.parseInt(port);
        }
        catch (NumberFormatException numberFormatException){
            return false;
        }

        if(portToCheck < 1 || portToCheck > 65353){
            return false;
        }

        return true;

    }

}
