package de.sync.cloud.services;

import com.google.gson.JsonObject;
import java.io.*;
import java.net.Socket;

public class BungeeCloudConnector {

    private final String host = "127.0.0.1";
    private final int port = 9100;
    private final String authPassword = "supersecret";

    public void registerServer(String serverName, int serverPort) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("type", "START_SERVER");
        json.addProperty("serverName", serverName);
        json.addProperty("template", "default"); // optional
        json.addProperty("proxy", false);

        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write("AUTH " + authPassword + "\n");
            out.flush();

            String authResp = in.readLine();
            if (!"AUTH_OK".equals(authResp)) {
                throw new IOException("Auth failed");
            }

            out.write(json.toString() + "\n");
            out.flush();

            String response = in.readLine();
            if (response == null || !response.startsWith("SERVER_STARTED")) {
                throw new IOException("Server could not be registered: " + response);
            }
        }
    }

    public void unregisterServer(String serverName) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("type", "STOP_SERVER");
        json.addProperty("serverName", serverName);

        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write("AUTH " + authPassword + "\n");
            out.flush();

            String authResp = in.readLine();
            if (!"AUTH_OK".equals(authResp)) {
                throw new IOException("Auth failed");
            }

            out.write(json.toString() + "\n");
            out.flush();

            String response = in.readLine();
            if (response == null || !response.startsWith("SERVER_STOPPED")) {
                throw new IOException("Server could not be logged off: " + response);
            }
        }
    }
}
