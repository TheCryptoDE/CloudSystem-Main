package de.sync.cloud.command;

import de.sync.cloud.managers.ServerManager;

import java.util.Scanner;

public class CloudCommandHandler implements Runnable {

    private ServerManager serverManager;

    public CloudCommandHandler(ServerManager manager) {
        this.serverManager = manager;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            String[] args = input.split(" ");

            switch (args[0].toLowerCase()) {
                case "list":
                    System.out.println("Laufende Server:");
                    serverManager.runningServers.keySet().forEach(System.out::println);
                    break;
                case "startserver":
                    if (args.length < 3) {
                        System.out.println("Usage: startserver <name> <template> [port]");
                        break;
                    }
                    int port = args.length > 3 ? Integer.parseInt(args[3]) : 25565;
                    try {
                        serverManager.startServer(args[1], args[2], port);
                    } catch (Exception e) {
                        System.out.println("Fehler: " + e.getMessage());
                    }
                    break;
                case "stopserver":
                    if (args.length < 2) {
                        System.out.println("Usage: stopserver <name>");
                        break;
                    }
                    serverManager.stopServer(args[1]);
                    break;
                default:
                    System.out.println("Unbekannter Befehl.");
            }
        }
    }
}
