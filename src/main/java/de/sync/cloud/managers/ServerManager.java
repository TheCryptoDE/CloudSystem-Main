package de.sync.cloud.managers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
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


    private void sendMessageToBungee(String message) {
        try (Socket socket = new Socket("127.0.0.1", 9100);
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true)) {
            writer.println(message);
        } catch (IOException e) {
            System.err.println("Could not send message to Bungee: " + e.getMessage());
        }
    }

    // Server von Template starten
    public void startServer(String serverName, String templateName, int port) throws IOException {
        File template = new File(templatesDir, templateName);
        if (!template.exists()) throw new IOException("Template not found: " + templateName);

        File serverFolder = new File(serversDir, serverName);
        if (!serverFolder.exists()) {
            copyFolder(template, serverFolder);  // Template kopieren
        }

        // server.properties bearbeiten
        File serverProps = new File(serverFolder, "server.properties");
        if (serverProps.exists()) {
            List<String> lines = Files.readAllLines(serverProps.toPath());

            boolean foundPort = false;
            boolean foundResourcePack = false;

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("server-port=")) {
                    lines.set(i, "server-port=" + port);
                    foundPort = true;
                }
                if (lines.get(i).startsWith("resource-pack=")) {
                    lines.set(i, "resource-pack=" + serverName); // z. B. JumpIT-5
                    foundResourcePack = true;
                }
            }

            if (!foundPort) lines.add("server-port=" + port);
            if (!foundResourcePack) lines.add("resource-pack=" + serverName);

            Files.write(serverProps.toPath(), lines);
        }

        // Server starten
        ProcessBuilder pb = new ProcessBuilder("java", "-Xmx1G", "-jar", "spigot.jar", "nogui");
        pb.directory(serverFolder);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = pb.start();
        runningServers.put(serverName, process);

        System.out.println("Server " + serverName + " gestartet auf Port " + port);
        sendMessageToBungee("§8[§bCloud§8] §7The server §b" + serverName + " §7is now §a§lstarted.");
    }


    public void stopServer(String serverName) {
        Process process = runningServers.get(serverName);
        if (process != null) {
            process.destroy();
            runningServers.remove(serverName);
            System.out.println("Server " + serverName + " gestoppt.");
            sendMessageToBungee("§8[§bCloud§8] §7The server §b" + serverName +  " §7is now §c§lstopped.");
        } else {
            System.out.println("Server " + serverName + " is not running.");
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
