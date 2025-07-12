package de.sync.cloud;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.sync.cloud.command.CloudVersionCommand;
import de.sync.cloud.logger.Loggers;
import de.sync.cloud.logger.LoggersType;
import de.sync.cloud.networking.PrintInfo;
import de.sync.cloud.networking.SocketInfo;
import de.sync.cloud.command.HelpTask;
import de.sync.cloud.console.ConsoleInput;
import de.sync.cloud.console.MySQLConfig;
import de.sync.cloud.console.SetupManager;
import de.sync.cloud.group.Group;
import de.sync.cloud.group.GroupCreateTask;
import de.sync.cloud.group.GroupManager;
import de.sync.cloud.module.Module;
import de.sync.cloud.module.ModuleLoader;
import de.sync.cloud.module.ModuleManager;
import de.sync.cloud.services.ProxyWatchdog;
import de.sync.cloud.task.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static de.sync.cloud.networking.PrintInfo.*;
import static de.sync.cloud.networking.SocketInfo.AUTH_PASSWORD;
import static de.sync.cloud.networking.SocketInfo.SOCKET_PORT;
import static de.sync.cloud.networking.handler.Client.handleClient;
import static de.sync.cloud.services.ServiceStart.startServer;

public class CloudSystem {

    private static final File SERVICE_FILE = new File("service.json");
    public static final Gson gson = new Gson();
    public static final int PROXY_PORT = 25577;
    private ServerSocket serverSocket;

    private static final File baseDir = new File(".");
    public static final File templatesDir = new File(baseDir, "templates");
    public static final File serversDir = new File(baseDir, "servers");

    public static final Map<String, ServerProcess> runningServers = new ConcurrentHashMap<>();
    public static final Map<String, Task> commands = new HashMap<>();


    public static String getCurrentMotd() {
        if (!SERVICE_FILE.exists()) {
            createDefaultServiceFile();
        }

        try (FileReader reader = new FileReader(SERVICE_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.has("motd") ? json.get("motd").getAsString() : "§cKeine MOTD in service.json gefunden!";
        } catch (IOException e) {
            e.printStackTrace();
            return "§cFehler beim Lesen der service.json";
        }
    }

    private static void createDefaultServiceFile() {
        JsonObject defaultJson = new JsonObject();
        defaultJson.addProperty("motd", "§8➔ §bSyncCloud §8• §7Ready for the Future §8• §f1.21.1-1.21.✘          §8➔ §7Download this §fCloudsystem §7on §bSPIGOTMC");

        try (FileWriter writer = new FileWriter(SERVICE_FILE)) {
            gson.toJson(defaultJson, writer);
            new Loggers(LoggersType.SUCCESS, Loggers.useColorSystem, "service.json wurde erstellt mit Standard-MOTD.");
        } catch (IOException e) {
            e.printStackTrace();
            new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Fehler beim Erstellen der service.json");
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        CloudSystem cloud = new CloudSystem();
        cloud.run();
    }

    public void run() throws IOException, SQLException {
        printHeader();
        Scanner scanner = new Scanner(System.in);

        // Prüfen ob mysql.json existiert
        File mysqlFile = new File("mysql.json");
        if (!mysqlFile.exists()) {

            new Loggers(LoggersType.INFO, Loggers.useColorSystem, "MySQL-Konfiguration nicht gefunden. Starte Setup...");

            SetupManager.frageMySQLDaten(scanner, false);
        }

        // MySQLConfig laden
        MySQLConfig config;
        try {
            config = MySQLConfig.loadFromFile(mysqlFile);
        } catch (IOException e) {
            new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Fehler beim Laden der MySQL-Konfiguration: " + e.getMessage());
            return;
        }

        // MySQL Verbindung herstellen
        String url = "jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
        Connection connection = DriverManager.getConnection(url, config.getUser(), config.getPassword() != null ? config.getPassword() : "");

        GroupManager groupManager = new GroupManager(connection);

        // Setup für Templates, falls noch nicht vorhanden
        if (!templatesDir.exists() || !templatesDir.isDirectory() || Objects.requireNonNull(templatesDir.listFiles()).length == 0) {
            int socketPort = SetupManager.runSetup(scanner, groupManager);
            SocketInfo.SOCKET_PORT = socketPort;
            System.exit(0);
        }



    initCommands();

        if (new File(templatesDir, "bungeecord").exists()) {
            try {
                startServer("BungeeCordProxy", "bungeecord", PROXY_PORT, true, true);
                new Loggers(LoggersType.SUCCESS, Loggers.useColorSystem, "BungeeCord ProxyWatchdog gestartet auf Port " + PROXY_PORT);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ProxyWatchdog.startBungeeWatchdog();
                    }
                }, 10000);
                createServiceJson("BungeeCordProxy", PROXY_PORT);
            } catch (Exception e) {
                new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Fehler beim Starten von BungeeCord: " + e.getMessage());
            }
        }

   //     if (new File(templatesDir, "lobby").exists()) {
   //         try {
   //             int lobbyPort = 25566;
   //             startServer("Lobby", "lobby", lobbyPort, false, true);
   //             PrintInfo.printSuccess("Lobby Server gestartet auf Port " + lobbyPort);
   //         } catch (Exception e) {
   //             printError("Fehler beim Starten der Lobby: " + e.getMessage());
   //         }
   //     }




        File modulesDir = new File("modules");
        if (!modulesDir.exists()) {
            boolean created = modulesDir.mkdirs();
            if (created) new Loggers(LoggersType.SUCCESS, Loggers.useColorSystem, "Module-Ordner erfolgreich erstellt!");
            else new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Module-Ordner konnte nicht erstellt werden!");
        }

        ModuleLoader loader = new ModuleLoader(modulesDir);
        ModuleManager moduleManager = new ModuleManager();

        try {
            List<Module> modules = loader.loadModules();
            moduleManager.registerModules(modules);
            moduleManager.enableAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(ConsoleInput::consoleInputLoop).start();
        startSocketListener();


    }





    public static void createServiceJson(String serverName, int port) {
        JsonObject serviceJson = new JsonObject();
        serviceJson.addProperty("serverName", serverName);
        serviceJson.addProperty("port", port);
        serviceJson.addProperty("status", "running");
        serviceJson.addProperty("timestamp", System.currentTimeMillis());

        File jsonFile = new File(serversDir, serverName + "_service.json");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile))) {
            writer.write(gson.toJson(serviceJson));
            new Loggers(LoggersType.SUCCESS, Loggers.useColorSystem, "Service JSON für " + serverName + " erstellt: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Fehler beim Erstellen von service.json: " + e.getMessage());
        }
    }

    public void downloadFile(String urlString, File output) throws IOException {
        URL url = new URL(urlString);
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(output)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }



    public void startSocketListener() throws IOException {
        serverSocket = new ServerSocket(SOCKET_PORT);
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Cloud socket listener on port " + SOCKET_PORT);
        while (true) {
            Socket client = serverSocket.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    public static synchronized void stopServer(String serverName) throws IOException {
        ServerProcess sp = runningServers.get(serverName);
        if (sp == null) throw new IOException("Server not found");
        sp.process.destroy();
        runningServers.remove(serverName);
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Server " + serverName + " stopped.");
    }
    private void startGroupServers(GroupManager groupManager) {
        for (Group group : groupManager.getGroups()) {
            String groupName = group.getGroupName();
            String type = group.getType();
            int minServer = group.getMinServer();

            for (int i = 1; i <= minServer; i++) {
                String serverName = groupName + "-" + i;
                try {
                    int port = CloudSystem.getFreePort();

                    // Starte lokalen Server (Minecraft-Server etc)
               //     startServer(serverName, groupName, port, false, true);
                    createServiceJson(serverName, port);
                    new Loggers(LoggersType.SUCCESS, Loggers.useColorSystem, "Server " + serverName + " started on port" + port);

                    // Informiere BungeeCord Proxy Plugin per Socket
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            notifyBungeeProxyWithCommand(groupName);
                        }
                    }, 8000);
                } catch (Exception e) {
                    new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Error when starting server " + serverName + ": " + e.getMessage());
                }
            }
        }
    }


    public void notifyBungeeProxyWithCommand(String groupName) {
        try (Socket socket = new Socket("127.0.0.1", SOCKET_PORT);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Auth
            out.write("AUTH " + AUTH_PASSWORD + "\n");
            out.flush();

            String authResponse = in.readLine();
            if (!"AUTH_OK".equals(authResponse)) {
                new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Auth failed at bungee proxy!");
                return;
            }

            // Kommando als String senden
            String command = "cloud startserver " + groupName + " " + groupName + " false";
            out.write(command + "\n");
            out.flush();

            String response = in.readLine();
            new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Antwort vom Proxy auf Command: " + response);

        } catch (IOException e) {
            new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Fehler beim Senden des Commands an BungeeCord: " + e.getMessage());
        }
    }




    public static void copyFolder(Path source, Path target) throws IOException {
        Files.walk(source).forEach(path -> {
            try {
                Path targetPath = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    if (!Files.exists(targetPath)) Files.createDirectories(targetPath);
                } else {
                    if (!Files.exists(targetPath)) Files.copy(path, targetPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static int getFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public void createServerProperties(File propsFile, int port, String serverName, int maxPlayers) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propsFile))) {
            writer.write("server-port=" + port + "\n");
            writer.write("max-players=" + maxPlayers + "\n");
            writer.write("motd=" + serverName + "\n");
            writer.write("online-mode=false\n");
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Konnte Datei nicht löschen: " + file.getAbsolutePath());
                    }
                }
            }
        }

        if (!directory.delete()) {
            throw new IOException("Konnte Verzeichnis nicht löschen: " + directory.getAbsolutePath());
        }
    }

    private void initCommands() {
        commands.put("stop", args -> {
            new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Stop all servers...");

            for (String server : new ArrayList<>(runningServers.keySet())) {
                try {
                    stopServer(server);
                } catch (IOException e) {
                    new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Error when stopping " + server + ": " + e.getMessage());
                }
            }

            // Versuche den 'servers'-Ordner zu löschen
            File serversFolder = new File("servers");
            if (serversFolder.exists()) {
                try {
                    deleteDirectory(serversFolder);
                    new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Ordner 'servers' wurde erfolgreich gelöscht.");
                } catch (IOException e) {
                    new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Fehler beim Löschen des 'servers'-Ordners: " + e.getMessage());
                }
            } else {
                new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Ordner 'servers' existiert nicht – übersprungen.");
            }

            PrintInfo.printSuccess("All servers stopped. Cloud is terminated.");
            System.exit(0);
        });


        try {
            // MySQL-Daten aus mysql.json laden
            File mysqlFile = new File("mysql.json");
            if (!mysqlFile.exists()) {
                printError("MySQL Konfiguration (mysql.json) nicht gefunden!");
                System.exit(1);
            }
            MySQLConfig config = MySQLConfig.loadFromFile(mysqlFile);

            // Verbindung herstellen
            String jdbcUrl = String.format(
                    "jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false",
                    config.getHost(), config.getPort(), config.getDatabase()
            );

            Connection connection;
            if (config.getPassword() == null || config.getPassword().isEmpty()) {
                connection = DriverManager.getConnection(jdbcUrl, config.getUser(), null);
            } else {
                connection = DriverManager.getConnection(jdbcUrl, config.getUser(), config.getPassword());
            }

            GroupManager groupManager = new GroupManager(connection);
            commands.put("service", new ServiceCommandTask(this));
            commands.put("group", new GroupCreateTask(groupManager));
            commands.put("help", new HelpTask());
            commands.put("version", new CloudVersionCommand());


            printSuccess("MySQL connection successfully established.");

        } catch (IOException e) {
            new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Error loading the MySQL configuration: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            new Loggers(LoggersType.ERROR, Loggers.useColorSystem, "Error with the MySQL connection: " + e.getMessage());
            System.exit(1);
        }
    }


    public static class ServerProcess {
        String name;
        Process process;

        public ServerProcess(String name, Process process) {
            this.name = name;
            this.process = process;
        }
    }

    public int getPROXY_PORT() {
        return PROXY_PORT;
    }

    public static Gson getGson() {
        return gson;
    }
    public static void downloadFileStatic(String urlString, File output) throws IOException {
        URL url = new URL(urlString);
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(output)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    public static void createServerPropertiesStatic(File propsFile, int port, String serverName, int maxPlayers) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propsFile))) {
            writer.write("server-port=" + port + "\n");
            writer.write("max-players=" + maxPlayers + "\n");
            writer.write("motd=" + serverName + "\n");
            writer.write("online-mode=false\n");
        }
    }

}
