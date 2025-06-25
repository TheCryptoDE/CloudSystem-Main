package de.sync.cloud.task;

import de.sync.cloud.CloudSystem;
import de.sync.cloud.networking.PrintInfo;
import de.sync.cloud.services.ServiceStart;

import java.io.IOException;

public class ServiceCommandTask implements Task {

    private final CloudSystem cloud;

    public ServiceCommandTask(CloudSystem cloud) {
        this.cloud = cloud;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("service")) {
            PrintInfo.printCMD("Usage:");
            PrintInfo.printCMD("  service start <ServerName> <TemplateName> [count] [ProxyWatchdog] [silent]");
            PrintInfo.printCMD("  service stop <ServerName>");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "start":
                handleStart(args);
                break;
            case "stop":
                handleStop(args);
                break;
            default:
                PrintInfo.printCMD("Unbekannter Befehl: " + action);
                PrintInfo.printCMD("Verfügbare Befehle: start, stop");
                break;
        }
    }

    private void handleStart(String[] args) {
        if (args.length < 4) {
            PrintInfo.printCMD("Usage: service start <ServerName> <TemplateName> [count] [ProxyWatchdog] [silent]");
            return;
        }

        String serverName = args[2];
        String templateName = args[3];

        int count = 1;
        boolean proxy = false;
        boolean silent = false;

        for (int i = 4; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            switch (arg) {
                case "proxywatchdog":
                    proxy = true;
                    break;
                case "silent":
                    silent = true;
                    break;
                default:
                    try {
                        count = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        PrintInfo.printCMD("Ungültiger Parameter: " + args[i]);
                        return;
                    }
                    break;
            }
        }

        for (int i = 0; i < count; i++) {
            String instanceName = (count == 1) ? serverName : serverName + "-" + (i + 1);
            int port = 0;
            try {
                port = proxy ? cloud.getPROXY_PORT() : cloud.getFreePort();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                ServiceStart.startServer(instanceName, templateName, port, proxy, silent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        PrintInfo.printCMD("Gestartet: " + count + " Instanz(en) von '" + serverName + "' mit Template '" + templateName + "'"
                + (proxy ? " als ProxyWatchdog" : "") + (silent ? " (silent)" : ""));
    }

    private void handleStop(String[] args) {
        if (args.length < 3) {
            PrintInfo.printCMD("Usage: service stop <ServerName>");
            return;
        }

        String serverName = args[2];

        try {
            cloud.stopServer(serverName);
            PrintInfo.printCMD("Server '" + serverName + "' wurde gestoppt.");
        } catch (IOException e) {
            System.err.println("Fehler beim Stoppen von '" + serverName + "': " + e.getMessage());
        }
    }
}
