package de.sync.cloud.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerManager {

    private final File templatesDir = new File("templates");
    private final File serversDir = new File("servers");
    public Map<String, Process> runningServers = new HashMap<>();

    public ServerManager() {
        if (!serversDir.exists()) serversDir.mkdirs();
    }

    // Server von Template starten
    public void startServer(String serverName, String templateName, int port) throws IOException {
        File template = new File(templatesDir, templateName);
        if (!template.exists()) throw new IOException("Template nicht gefunden: " + templateName);

        File serverFolder = new File(serversDir, serverName);
        if (!serverFolder.exists()) {
            copyFolder(template, serverFolder);  // Template kopieren
        }

        // server.properties anpassen (Port)
        File serverProps = new File(serverFolder, "server.properties");
        if (serverProps.exists()) {
            // Einfach port ersetzen (ohne libs, quick & dirty)
            List<String> lines = Files.readAllLines(serverProps.toPath());
            for (int i=0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("server-port=")) {
                    lines.set(i, "server-port=" + port);
                }
            }
            Files.write(serverProps.toPath(), lines);
        }

        // Spigot starten
        ProcessBuilder pb = new ProcessBuilder("java", "-Xmx1G", "-jar", "spigot.jar", "nogui");
        pb.directory(serverFolder);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();
        runningServers.put(serverName, process);

        System.out.println("Server " + serverName + " gestartet auf Port " + port);
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
        // rekursive Kopiermethode, z.B. mit java.nio.Files.copy oder Apache Commons IO
        if (source.isDirectory()) {
            if (!target.exists()) target.mkdirs();
            for (File file : source.listFiles()) {
                copyFolder(file, new File(target, file.getName()));
            }
        } else {
            Files.copy(source.toPath(), target.toPath());
        }
    }
}
