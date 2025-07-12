package de.sync.cloud.networking.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.sync.cloud.CloudSystem;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static de.sync.cloud.CloudSystem.*;
import static de.sync.cloud.networking.PrintInfo.printError;
import static de.sync.cloud.networking.SocketInfo.AUTH_PASSWORD;
import static de.sync.cloud.services.ServiceStart.startServer;

public class Client {

    private static final Gson gson = new Gson();

    public static void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String auth = in.readLine();
            if (auth == null || !auth.equals("AUTH " + AUTH_PASSWORD)) {
                out.write("ERROR Auth Failed\n");
                out.flush();
                socket.close();
                return;
            }
            out.write("AUTH_OK\n");
            out.flush();

            String line = in.readLine();
            if (line == null) return;

            // Versuche JSON zu parsen, falls das fehlschlägt, prüfe ob es ein Bungee-Command ist
            JsonObject json = null;
            try {
                json = gson.fromJson(line, JsonObject.class);
            } catch (JsonSyntaxException ex) {
                // kein JSON, ignorieren hier und weiter unten prüfen
            }

            if (json != null && json.has("type")) {
                String type = json.get("type").getAsString();

                switch (type) {
                    case "START_SERVER":
                        String name = json.get("serverName").getAsString();
                        String template = json.get("template").getAsString();
                        boolean isProxy = json.has("ProxyWatchdog") && json.get("ProxyWatchdog").getAsBoolean();
                        boolean silent = true;
                        int port = isProxy ? PROXY_PORT : getFreePort();
                        startServer(name, template, port, isProxy, silent);
                        out.write("SERVER_STARTED " + name + " PORT " + port + "\n");
                        out.flush();

                        // Service JSON beim Serverstart erstellen
                        createServiceJson(name, port);
                        break;

                    case "STOP_SERVER":
                        stopServer(json.get("serverName").getAsString());
                        out.write("SERVER_STOPPED\n");
                        out.flush();
                        break;

                    case "LIST_SERVERS":
                        out.write("SERVERS " + String.join(",", runningServers.keySet()) + "\n");
                        out.flush();
                        break;

                    case "SERVER_STATUS":
                        String serverName = json.get("serverName").getAsString();
                        CloudSystem.ServerProcess sp = runningServers.get(serverName);
                        if (sp == null) {
                            out.write("ERROR Server not Found\n");
                        } else {
                            out.write("STATUS " + serverName + " ONLINE\n");
                        }
                        out.flush();
                        break;

                    case "GET_MOTD":
                        String motd = getCurrentMotd();
                        out.write(motd + "\n");
                        out.flush();
                        break;

                    case "STOP_ALL_SERVERS":
                        for (String server : new ArrayList<>(runningServers.keySet())) {
                            try {
                                stopServer(server);
                            } catch (IOException e) {
                                printError("Error when stopping " + server + ": " + e.getMessage());
                            }
                        }
                        out.write("ALL_SERVERS_STOPPED\n");
                        out.flush();
                        break;

                    default:
                        out.write("ERROR unknown command\n");
                        out.flush();
                }
            } else if (line.startsWith("cloud startserver")) {
                // BungeeCord Command empfangen
                System.out.println("BungeeCord Command erhalten: " + line);
                out.write("COMMAND_RECEIVED\n");
                out.flush();
            } else {
                out.write("ERROR unknown command\n");
                out.flush();
            }

        } catch (IOException e) {
            printError("ERROR in handleClient: " + e.getMessage());
        }
    }



}
