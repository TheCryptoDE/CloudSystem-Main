package de.sync.cloud.services;

import com.google.gson.JsonObject;
import de.sync.cloud.CloudSystem;
import de.sync.cloud.Networking.SocketInfo;

import java.io.*;
import java.net.Socket;

import static de.sync.cloud.CloudSystem.*;
import static de.sync.cloud.Networking.PrintInfo.*;
import static de.sync.cloud.services.ServiceStart.startServer;

public class ProxyWatchdog {


    public static void startBungeeWatchdog() {
        new Thread(() -> {
            while (true) {
                try (Socket socket = new Socket("127.0.0.1", SocketInfo.SOCKET_PORT);
                     BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Authentifizieren
                    out.write("AUTH " + SocketInfo.AUTH_PASSWORD + "\n");
                    out.flush();

                    String authResponse = in.readLine();
                    if (!"AUTH_OK".equals(authResponse)) {
                        printWarn("Watchdog: Authentifizierung fehlgeschlagen.");
                        continue;
                    }

                    // Statusabfrage
                    JsonObject request = new JsonObject();
                    request.addProperty("type", "SERVER_STATUS");
                    request.addProperty("serverName", "BungeeCordProxy");

                    out.write(request.toString() + "\n");
                    out.flush();

                    String statusResponse = in.readLine();
                    if (statusResponse == null || statusResponse.startsWith("ERROR")) {
                        printWarn("Watchdog: BungeeCord nicht erreichbar. Versuche zu starten...");
                        restartBungeeCord();
                    }

                } catch (IOException e) {
                    printWarn("Watchdog: BungeeCord Verbindung fehlgeschlagen. Starte neu...");
                    restartBungeeCord();
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    printError("Watchdog unterbrochen: " + e.getMessage());
                    break;
                }
            }
        }, "Bungee-Watchdog").start();
    }


    private static void restartBungeeCord() {
        try {
            if (!runningServers.containsKey("BungeeCordProxy")) {
                startServer("BungeeCordProxy", "bungeecord", PROXY_PORT, true, true);
                printSuccess("Watchdog: BungeeCord wurde neu gestartet.");
            }
        } catch (Exception ex) {
            printError("Watchdog: Fehler beim Neustarten von BungeeCord: " + ex.getMessage());
        }
    }
}
