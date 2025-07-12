package de.sync.cloud.services;

import de.sync.cloud.group.Group;
import de.sync.cloud.group.GroupManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager {

    private final File templatesDir = new File("templates");
    private final File serversDir = new File("servers");
    private final GroupManager groupManager;

    public Map<String, Process> runningServers = new HashMap<>();

    public ServerManager(GroupManager groupManager) {
        this.groupManager = groupManager;
        if (!serversDir.exists()) serversDir.mkdirs();
        if (!templatesDir.exists()) templatesDir.mkdirs();
    }

    public void startServer(String serverName, String groupName, int port) throws IOException {
        Group group = groupManager.getGroup(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Group does not exist: " + groupName);
        }

        File templateFolder = new File(templatesDir, groupName);
        if (!templateFolder.exists() || !templateFolder.isDirectory()) {
            throw new IOException("Template folder does not exist for group: " + groupName);
        }

        File serverFolder = new File(serversDir, serverName);
        if (!serverFolder.exists()) {
            copyFolder(templateFolder, serverFolder);
        }

        if (group.getType().equalsIgnoreCase("spigot")) {
            File serverProps = new File(serverFolder, "server.properties");
            if (serverProps.exists()) {
                updateServerProperties(serverProps, port, serverName);
            }
        } else if (group.getType().equalsIgnoreCase("bungeecord")) {
            File configYml = new File(serverFolder, "config.yml");
            if (configYml.exists()) {
                updateBungeePort(configYml, port);
            }
        }

        Process process = startJavaProcess(serverFolder, group.getType());
        runningServers.put(serverName, process);

        System.out.println("Server " + serverName + " (Group " + groupName + ") started on port " + port);
    }

    public void stopServer(String serverName) {
        Process process = runningServers.get(serverName);
        if (process != null) {
            process.destroy();
            runningServers.remove(serverName);
            System.out.println("Server " + serverName + " gestoppt.");
        } else {
            System.out.println("Server " + serverName + " läuft nicht.");
        }
    }

    private void copyFolder(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) target.mkdirs();
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyFolder(file, new File(target, file.getName()));
                }
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void updateServerProperties(File serverProps, int port, String serverName) throws IOException {
        List<String> lines = Files.readAllLines(serverProps.toPath());
        boolean portSet = false;
        boolean motdSet = false;

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("server-port=")) {
                lines.set(i, "server-port=" + port);
                portSet = true;
            } else if (lines.get(i).startsWith("motd=")) {
                lines.set(i, "motd=Server " + serverName);
                motdSet = true;
            }
        }
        if (!portSet) {
            lines.add("server-port=" + port);
        }
        if (!motdSet) {
            lines.add("motd=Server " + serverName);
        }
        Files.write(serverProps.toPath(), lines);
    }

    private void updateBungeePort(File config, int port) throws IOException {
        List<String> lines = Files.readAllLines(config.toPath());
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith("host:")) {
                lines.set(i, "  host: 0.0.0.0:" + port);
            }
        }
        Files.write(config.toPath(), lines);
    }

    private Process startJavaProcess(File serverFolder, String type) throws IOException {
        String jarName = type.equalsIgnoreCase("spigot") ? "server.jar" : "BungeeCord.jar";
        File jarFile = new File(serverFolder, jarName);
        if (!jarFile.exists()) {
            throw new IOException("Start-JAR nicht gefunden: " + jarFile.getAbsolutePath());
        }

        // javaw statt java = unsichtbarer Prozessstart unter Windows
        ProcessBuilder pb = new ProcessBuilder("javaw", "-Xmx3G", "-jar", jarFile.getName(), "nogui");
        pb.directory(serverFolder);

        // Output nicht an Konsole weiterleiten, komplett unterdrücken
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);

        return pb.start();
    }
}
