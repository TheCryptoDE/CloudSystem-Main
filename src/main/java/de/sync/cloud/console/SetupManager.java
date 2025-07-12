

package de.sync.cloud.console;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.sync.cloud.CloudSystem;
import de.sync.cloud.group.Group;
import de.sync.cloud.group.GroupManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import static de.sync.cloud.networking.PrintInfo.*;

public class SetupManager {

    public static int runSetup(Scanner scanner, GroupManager groupManager) {
        printInfo("Erstes Setup wird gestartet...");

        // Socket-Port abfragen
        int socketPort = frageSocketPort(scanner);


        // Server-Umgebung abfragen
        String environment = frageServerUmgebung(scanner);

        // Proxy erstellen?
        boolean erstelleProxy = frageProxyErstellung(scanner);

        // Lobby erstellen?
        boolean erstelleLobby = frageLobbyErstellung(scanner);
        frageMySQLDaten(scanner, erstelleLobby);


        // Templates-Verzeichnis anlegen
        CloudSystem.templatesDir.mkdirs();

        // Lobby einrichten (nur bei MINECRAFT_SERVER)
// Lobby einrichten (nur bei MINECRAFT_SERVER)
        if (erstelleLobby) {
            File lobbyTemplate = new File(CloudSystem.templatesDir, "lobby");
            lobbyTemplate.mkdirs();

            if (environment.equals("MINECRAFT_SERVER")) {
                printInfo("Download Spigot server.jar for Lobby...");
                try {
                    CloudSystem.downloadFileStatic("https://s3.mcjars.app/spigot/1.21.4/4458/server.jar", new File(lobbyTemplate, "server.jar"));
                    CloudSystem.createServerPropertiesStatic(new File(lobbyTemplate, "server.properties"), 25566, "Lobby Server", 20);

                    // eula.txt anlegen
                    File eulaFile = new File(lobbyTemplate, "eula.txt");
                    try (FileWriter writer = new FileWriter(eulaFile)) {
                        writer.write("eula=true\n");
                    }

                    // spigot.yml anlegen (Standardkonfiguration)
                    File spigotYml = new File(lobbyTemplate, "spigot.yml");
                    try (FileWriter writer = new FileWriter(spigotYml)) {
                        writer.write(
                                "# Spigot configuration\n" +
                                        "settings:\n" +
                                        "  bungeecord: true\n" +
                                        "  restart-on-crash: true\n" +
                                        "  restart-script: ./start.sh\n" +
                                        "commands:\n" +
                                        "  tab-complete: 0\n" +
                                        "players:\n" +
                                        "  disable-saving: false\n" +
                                        "world-settings:\n" +
                                        "  default:\n" +
                                        "    view-distance: default\n" +
                                        "    simulation-distance: default\n" +
                                        "messages:\n" +
                                        "  whitelist: You are not whitelisted on this server!\n" +
                                        "  unknown-command: Unknown command. Type \"/help\" for help.\n" +
                                        "  server-full: The server is full!\n" +
                                        "config-version: 12\n"
                        );
                    }

                    // CloudSignPlugin Ordner erstellen
                    File cloudSignPluginDir = new File(lobbyTemplate, "plugins/CloudBridge");
                    cloudSignPluginDir.mkdirs();

                    // Jetzt den CloudSign Plugin Download starten
                    printInfo("Download CloudSignPlugin...");
                    CloudSystem.downloadFileStatic(
                            "https://github.com/TheCryptoDE/CloudSystem-SignSpigot/releases/download/1.0.1/CloudSystem-CloudBridge-1.0-SNAPSHOT.jar",
                            new File(lobbyTemplate, "plugins/CloudBridge.jar")
                    );

                    printSuccess("CloudSignPlugin has been downloaded successfully.");

                    // MySQL wird später noch zusätzlich für CloudSign gespeichert

                    Group lobbyGroup = new Group("Lobby", "spigot", "static", 1, 20);
                    groupManager.addGroup(lobbyGroup);
                    printSuccess("Lobby template has been created.");
                } catch (IOException e) {
                    printError("Error when creating the lobby: " + e.getMessage());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                printInfo("Lobby template is created without Spigot server.jar because the environment is not MINECRAFT_SERVER.");
            }
        }


        // Proxy einrichten
        if (erstelleProxy) {
            File proxyTemplate = new File(CloudSystem.templatesDir, "bungeecord");
            proxyTemplate.mkdirs();

            printInfo("Download BungeeCord.jar...");
            try {
                CloudSystem.downloadFileStatic("https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar",
                        new File(proxyTemplate, "BungeeCord.jar"));

                // Plugins-Ordner und Beispiel-Plugin (optional)
                File pluginsDir = new File(proxyTemplate, "plugins");
                pluginsDir.mkdirs();
                CloudSystem.downloadFileStatic(
                        "https://github.com/TheCryptoDE/BungeeCord-Plugin/releases/download/1.0.1/BungeeCord-Plugin-1.0-SNAPSHOT.jar",
                        new File(pluginsDir, "BungeeCord-Plugin-1.0-SNAPSHOT.jar"));

                // config.yml mit Standardinhalt anlegen
                File configFile = new File(proxyTemplate, "config.yml");
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write(
                            "network_compression_threshold: 256\n" +
                                    "remote_ping_timeout: 5000\n" +
                                    "remote_ping_cache: -1\n" +
                                    "online_mode: true\n" +
                                    "forge_support: false\n" +
                                    "max_packets_per_second: 4096\n" +
                                    "listeners:\n" +
                                    "- query_port: 25577\n" +
                                    "  motd: '&6BungeeCord ProxyWatchdog'\n" +
                                    "  tab_list: GLOBAL_PING\n" +
                                    "  query_enabled: false\n" +
                                    "  host: 0.0.0.0:25577\n" +
                                    "  forced_hosts:\n" +
                                    "    lobby.example.com: lobby\n" +
                                    "  ping_passthrough: false\n" +
                                    "  priorities:\n" +
                                    "  - lobby\n" +
                                    "  max_players: 100\n" +
                                    "  force_default_server: false\n" +
                                    "  tab_size: 60\n" +
                                    "  bind_local_address: true\n" +
                                    "  proxy_protocol: false\n" +
                                    "disabled_commands:\n" +
                                    "- disabledcommandhere\n" +
                                    "max_packets_data_per_second: 33554432\n" +
                                    "player_limit: 100\n" +
                                    "reject_transfers: false\n" +
                                    "connection_throttle: 4000\n" +
                                    "connection_throttle_limit: 3\n" +
                                    "prevent_proxy_connections: false\n" +
                                    "log_commands: false\n" +
                                    "log_pings: true\n" +
                                    "stats: fe2faf04-f803-45ae-a236-8f7f4a6cc55a\n" +
                                    "groups:\n" +
                                    "  md_5:\n" +
                                    "  - admin\n" +
                                    "servers:\n" +
                                    "  lobby:\n" +
                                    "    motd: '&1Just another BungeeCord - Forced Host'\n" +
                                    "    address: localhost:25566\n" +
                                    "    restricted: false\n" +
                                    "permissions:\n" +
                                    "  default:\n" +
                                    "  - bungeecord.command.server\n" +
                                    "  - bungeecord.command.list\n" +
                                    "  - cloud.admin\n" +
                                    "ip_forward: true\n" +
                                    "enforce_secure_profile: false\n" +
                                    "server_connect_timeout: 5000\n" +
                                    "timeout: 30000\n"
                    );
                }

                saveMySQLConfigInBungeeCordTemplate(proxyTemplate);
                erstelleMySQLPermissionTabellen(MySQLConfig.loadFromFile(new File("mysql.json")));

                printSuccess("BungeeCord template was created.");
            } catch (IOException e) {
                printError("Error when creating the proxy: " + e.getMessage());
            }
        }

        printSuccess("Setup completed. Please restart the cloud.");
        return socketPort;
    }


    private static void erstelleMySQLPermissionTabellen(MySQLConfig config) {
        String jdbcUrl = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?useSSL=false&autoReconnect=true";
        try (java.sql.Connection connection = java.sql.DriverManager.getConnection(jdbcUrl, config.getUser(), config.getPassword())) {
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `permsgroups` (\n" +
                        "  `name` VARCHAR(50) NOT NULL,\n" +
                        "  PRIMARY KEY (`name`)\n" +
                        ");");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `group_permissions` (\n" +
                        "  `group_name` VARCHAR(50) NOT NULL,\n" +
                        "  `permission` VARCHAR(255) NOT NULL,\n" +
                        "  FOREIGN KEY (`group_name`) REFERENCES `permsgroups`(`name`) ON DELETE CASCADE\n" +
                        ");");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `player_groups` (\n" +
                        "  `uuid` VARCHAR(36) NOT NULL,\n" +
                        "  `group_name` VARCHAR(50) NOT NULL,\n" +
                        "  FOREIGN KEY (`group_name`) REFERENCES `permsgroups`(`name`) ON DELETE CASCADE\n" +
                        ");");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `permissions` (\n" +
                        "  `uuid` VARCHAR(36) NOT NULL,\n" +
                        "  `permission` VARCHAR(255) NOT NULL\n" +
                        ");");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `signs` (\n" +
                        "  `id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `world` VARCHAR(64) DEFAULT NULL,\n" +
                        "  `x` INT(11) DEFAULT NULL,\n" +
                        "  `y` INT(11) DEFAULT NULL,\n" +
                        "  `z` INT(11) DEFAULT NULL,\n" +
                        "  `server` VARCHAR(64) DEFAULT NULL,\n" +
                        "  PRIMARY KEY (`id`)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `started_servers` (" +
                        "`id` int(11) NOT NULL AUTO_INCREMENT," +
                        "`server_name` varchar(255) NOT NULL," +
                        "`template` varchar(255) DEFAULT NULL," +
                        "`started_at` timestamp NOT NULL DEFAULT current_timestamp()," +
                        "PRIMARY KEY (`id`)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");

                printSuccess("MySQL tables for authorizations successfully created.");
            }
        } catch (Exception e) {
            printError("Error when creating the tables: " + e.getMessage());
        }
    }


    private static void saveMySQLConfigInBungeeCordTemplate(File proxyTemplate) {
        // Die mysql.json aus dem Hauptverzeichnis laden und hierher kopieren
        File mainMySQL = new File("mysql.json");
        if (!mainMySQL.exists()) {
            printWarn("mysql.json not found in root directory, skip copying to BungeeCord template.");
            return;
        }

        File proxyMySQL = new File(proxyTemplate, "mysql.json");
        try (Scanner scanner = new Scanner(mainMySQL);
             FileWriter writer = new FileWriter(proxyMySQL)) {
            while (scanner.hasNextLine()) {
                writer.write(scanner.nextLine() + System.lineSeparator());
            }
            printSuccess("mysql.json saved in BungeeCord template: " + proxyMySQL.getPath());
        } catch (IOException e) {
            printError("Error copying mysql.json to BungeeCord template: " + e.getMessage());
        }
    }

    public static void frageMySQLDaten(Scanner scanner, boolean erstelleLobby) {
        System.out.println(ANSI_YELLOW + "MySQL Konfiguration:" + ANSI_RESET);

        System.out.print("MySQL host (default: localhost): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) host = "localhost";

        System.out.print("MySQL Port (default: 3306): ");
        String portInput = scanner.nextLine().trim();
        int port = 3306;
        if (!portInput.isEmpty()) {
            try {
                port = Integer.parseInt(portInput);
            } catch (NumberFormatException e) {
                printWarn("Invalid port. Use standard port 3306.");
            }
        }

        System.out.print("MySQL user (z.B. root): ");
        String user = scanner.nextLine().trim();
        while (user.isEmpty()) {
            printWarn("Der username darf nicht leer sein.");
            System.out.print("MySQL user: ");
            user = scanner.nextLine().trim();
        }

        System.out.print("MySQL password (leave blank for no password): ");
        String password = scanner.nextLine();
        if (password.isEmpty()) password = null;

        System.out.print("MySQL database name (e.g. cloudsystem): ");
        String database = scanner.nextLine().trim();
        while (database.isEmpty()) {
            printWarn("The database name must not be empty.");
            System.out.print("MySQL database name: ");
            database = scanner.nextLine().trim();
        }

        MySQLConfig config = new MySQLConfig(host, port, user, password, database);
        saveMySQLConfig(config, erstelleLobby);
        printSuccess("MySQL data saved.");
    }



    private static void saveMySQLConfig(MySQLConfig config, boolean erstelleLobby) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Sicherstellen, dass Passwort niemals null ist
        if (config.getPassword() == null) {
            config.setPassword("");
        }

        // Speichere zuerst immer mysql.json im Hauptordner
        File mainFile = new File("mysql.json");
        try (FileWriter writer = new FileWriter(mainFile)) {
            gson.toJson(config, writer);
            printSuccess("MySQL data saved in mysql.json.");
        } catch (IOException e) {
            printError("Error saving MySQL configuration: " + e.getMessage());
        }

        // Wenn Lobby erstellt werden soll, zusätzlich ins CloudSignPlugin schreiben
        if (erstelleLobby) {
            File cloudSignPluginDir = new File("templates/lobby/plugins/CloudBridge");
            if (!cloudSignPluginDir.exists()) {
                if (cloudSignPluginDir.mkdirs()) {
                    printInfo("Folder " + cloudSignPluginDir.getPath() + " has been created.");
                } else {
                    printError("Could not create the folder " + cloudSignPluginDir.getPath() + ".");
                    return;
                }
            }

            File pluginMySQLFile = new File(cloudSignPluginDir, "mysql.json");
            try (FileWriter writer = new FileWriter(pluginMySQLFile)) {
                gson.toJson(config, writer);
                printSuccess("MySQL data additionally saved in " + pluginMySQLFile.getPath() + ".");
            } catch (IOException e) {
                printError("Error saving MySQL configuration in plugin folder: " + e.getMessage());
            }
        }
    }




    public static int frageSocketPort(Scanner scanner) {
        System.out.println(ANSI_YELLOW + "Which port should the Cloud Socket connection use? (Default: 9100)" + ANSI_RESET);
        System.out.print("> ");
        String input = scanner.nextLine().trim();

        int port = 9100;
        if (!input.isEmpty()) {
            try {
                port = Integer.parseInt(input);
                if (port < 1000 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                printWarn("Invalid port. Using default port 9100.");
                port = 9100;
            }
        }
        printSuccess("Cloud socket port set to: " + port);
        return port;
    }

    public static String frageServerUmgebung(Scanner scanner) {
        System.out.println(ANSI_YELLOW + "Which environment should the servers use?" + ANSI_RESET);
        System.out.println(" > Possible answers: MINECRAFT_SERVER, GLOWSTONE, NUKKIT, GO_MINT");
        System.out.print("> ");
        String env = scanner.nextLine().trim().toUpperCase();

        while (!env.matches("MINECRAFT_SERVER|GLOWSTONE|NUKKIT|GO_MINT")) {
            printWarn("Invalid input. Please choose one of the listed environments.");
            System.out.print("> ");
            env = scanner.nextLine().trim().toUpperCase();
        }

        printSuccess("Server environment set to: " + env);
        return env;
    }

    public static boolean frageProxyErstellung(Scanner scanner) {
        System.out.println(ANSI_YELLOW + "Should a default proxy be created?" + ANSI_RESET);
        System.out.println(" > Possible answers: yes, no");
        System.out.print("> ");
        String input = scanner.nextLine().trim().toLowerCase();

        while (!input.equals("yes") && !input.equals("no")) {
            printWarn("Invalid input. Please enter \"yes\" or \"no\".");
            System.out.print("> ");
            input = scanner.nextLine().trim().toLowerCase();
        }

        return input.equals("yes");
    }

    public static boolean frageLobbyErstellung(Scanner scanner) {
        System.out.println(ANSI_YELLOW + "Should a default lobby be created?" + ANSI_RESET);
        System.out.println(" > Possible answers: yes, no");
        System.out.print("> ");
        String input = scanner.nextLine().trim().toLowerCase();

        while (!input.equals("yes") && !input.equals("no")) {
            printWarn("Invalid input. Please enter \"yes\" or \"no\".");
            System.out.print("> ");
            input = scanner.nextLine().trim().toLowerCase();
        }

        return input.equals("yes");
    }
}
