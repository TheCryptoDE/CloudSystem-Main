package de.sync.cloud.services;

import de.sync.cloud.CloudSystem;
import de.sync.cloud.networkingd.PrintInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.sync.cloud.CloudSystem.*;
import static de.sync.cloud.networkingd.PrintInfo.printError;

public class ServiceStart {

    public static final List<StartedServer> startedServers = Collections.synchronizedList(new ArrayList<>());

    public static synchronized void startServer(String serverName, String templateName, int port, boolean isProxy, boolean silent) throws IOException {
        if (runningServers.containsKey(serverName)) throw new IOException("Server existiert bereits");

        File template = new File(templatesDir, templateName);
        if (!template.exists()) throw new IOException("Template nicht gefunden " + templateName);

        File serverFolder = new File(serversDir, serverName);
        if (!serverFolder.exists()) copyFolder(template.toPath(), serverFolder.toPath());

        if (isProxy) {
            File jarFile = new File(serverFolder, "BungeeCord.jar");
            if (!jarFile.exists()) {
                Files.copy(new File(template, "BungeeCord.jar").toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            File configFile = new File(serverFolder, "config.yml");
            if (!configFile.exists()) {
                Files.copy(new File(template, "config.yml").toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        if (!isProxy) {
            File propsFile = new File(serverFolder, "server.properties");
            if (propsFile.exists()) {
                List<String> lines = Files.readAllLines(propsFile.toPath());
                List<String> newLines = new ArrayList<>();
                for (String line : lines) {
                    newLines.add(line.startsWith("server-port=") ? "server-port=" + port : line);
                }
                Files.write(propsFile.toPath(), newLines);
            }
        }

        ProcessBuilder pb = isProxy
                ? new ProcessBuilder("java", "-Xmx512M", "-jar", "BungeeCord.jar")
                : new ProcessBuilder("java", "-Xmx512M", "-jar", "server.jar", "nogui");

        pb.directory(serverFolder);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        CloudSystem.ServerProcess serverProcess = new CloudSystem.ServerProcess(serverName, process);
        runningServers.put(serverName, serverProcess);

        startedServers.add(new StartedServer(serverName, templateName, port));

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!silent) System.out.println("[" + serverName + "] " + line);
                }
            } catch (IOException e) {
                printError("Fehler beim Lesen der Serverausgabe: " + e.getMessage());
            }
        }).start();

        PrintInfo.printSuccess("Server " + serverName + " gestartet auf Port " + port);
    }

    public static class StartedServer {
        private final String name;
        private final String template;
        private final int port;

        public StartedServer(String name, String template, int port) {
            this.name = name;
            this.template = template;
            this.port = port;
        }

        public String getName() {
            return name;
        }

        public String getTemplate() {
            return template;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return "StartedServer{name='%s', template='%s', port=%d}".formatted(name, template, port);
        }
    }
}
