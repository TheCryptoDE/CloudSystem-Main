package de.sync.cloud.networking;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintInfo {

    public static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_LIGHT_BLUE = "\u001B[94m";
    private static final String ANSI_GREEN = "\u001B[92m";
    private static final String ANSI_RED = "\u001B[91m";
    public static final String ANSI_YELLOW = "\u001B[93m";
    private static final String ANSI_CYAN = "\u001B[96m";
    private static final String ANSI_MAGENTA = "\u001B[95m";


    public static void printInfo(String msg) {
        System.out.println(ANSI_CYAN + "[INFO] " + ANSI_RESET + msg);
    }
    public static void printModule(String msg) {
        System.out.println(ANSI_LIGHT_BLUE + "[MODULE] " + ANSI_RESET + msg);
    }
    public static void printCMD(String msg) {
        System.out.println(ANSI_LIGHT_BLUE + "[CMD] " + ANSI_RESET + msg);
    }

    public static void printWarn(String msg) {
        System.out.println(ANSI_YELLOW + "[WARN] " + ANSI_RESET + msg);
    }

    public static void printError(String msg) {
        System.out.println(ANSI_RED + "[ERROR] " + ANSI_RESET + msg);
    }

    public static void printSuccess(String msg) {
        System.out.println(ANSI_GREEN + "[SUCCESS] " + ANSI_RESET + msg);
    }

    public static void printPrompt() {
        String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.print(ANSI_LIGHT_BLUE + "[" + timeStamp + "] > " + ANSI_RESET);
    }


    public static void printHeader() {
        String line = "═══════════════════════════════════════════════════════════════════════";
             System.out.println(ANSI_CYAN + "\n" +
                     "   _____                   _____ _                 _ \n" +
                     "  / ____|                 / ____| |               | |\n" +
                     " | (___  _   _ _ __   ___| |    | | ___  _   _  __| |\n" +
                     "  \\___ \\| | | | '_ \\ / __| |    | |/ _ \\| | | |/ _` |\n" +
                     "  ____) | |_| | | | | (__| |____| | (_) | |_| | (_| |\n" +
                     " |_____/ \\__, |_| |_|\\___|\\_____|_|\\___/ \\__,_|\\__,_|  (EARTHQUAKE-1.0.1)\n" +
                     "          __/ |                                      \n" +
                     "         |___/                                       \n" + ANSI_RESET);

        System.out.println(ANSI_CYAN + "<!> SyncCloudService - ready for the future?");
        System.out.println(ANSI_CYAN + "<!> support: https://synccloudservice.eu/" + ANSI_RESET);
        System.out.println(ANSI_CYAN );
        System.out.println(ANSI_CYAN + line);
        System.out.println(ANSI_RESET);
    }
}
