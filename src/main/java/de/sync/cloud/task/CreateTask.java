package de.sync.cloud.task;

import de.sync.cloud.CloudSystem;
import de.sync.cloud.services.config.ProxyConfigCreator;

import java.io.File;
import java.lang.reflect.Proxy;

public class CreateTask implements Task {

    private final CloudSystem cloud;

    public CreateTask(CloudSystem cloud) {
        this.cloud = cloud;
    }

    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 6) {
            System.out.println("Usage: tasks create <ServerName> <ServerType> <static|notstatic> <maxplayers>");
            return;
        }

        String serverName = args[2];
        String serverType = args[3].toLowerCase();
        String staticFlag = args[4].toLowerCase();
        int maxPlayers;

        try {
            maxPlayers = Integer.parseInt(args[5]);
        } catch (NumberFormatException e) {
            System.out.println("maxplayers muss eine Zahl sein.");
            return;
        }

        if (!serverType.equals("spigot") && !serverType.equals("bungeecord")) {
            System.out.println("ServerType muss 'spigot' oder 'bungeecord' sein.");
            return;
        }

        if (!staticFlag.equals("static") && !staticFlag.equals("notstatic")) {
            System.out.println("Flag muss 'static' oder 'notstatic' sein.");
            return;
        }

        // Template-Ordner anlegen
        File templateDir = new File("templates", serverName);
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }

        System.out.println("Erstelle Template für " + serverName + " (" + serverType + ")...");

        if (serverType.equals("spigot")) {
            File jarFile = new File(templateDir, "server.jar");
            if (!jarFile.exists()) {
                System.out.println("Lade Spigot herunter...");
                cloud.downloadFile("https://s3.mcjars.app/spigot/1.21.4/4458/server.jar", jarFile);
            }
            cloud.createServerProperties(new File(templateDir, "server.properties"), 25565, serverName, maxPlayers);
        } else {
            File jarFile = new File(templateDir, "BungeeCord.jar");
            if (!jarFile.exists()) {
                System.out.println("Lade BungeeCord herunter...");
                cloud.downloadFile("https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar", jarFile);
            }
            ProxyConfigCreator.createBungeeConfig(templateDir, false, maxPlayers);
        }

        // Hier könntest du Metadaten speichern, z.B. JSON-Datei mit den Daten

        System.out.println("Task für " + serverName + " erstellt: Typ=" + serverType + ", Static=" + staticFlag + ", MaxPlayers=" + maxPlayers);
    }
}
