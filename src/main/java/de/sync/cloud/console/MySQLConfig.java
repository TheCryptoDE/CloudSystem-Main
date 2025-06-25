package de.sync.cloud.console;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

public class MySQLConfig {

    private String host;
    private int port;
    private String user;
    private String password;
    private String database;

    // Konstruktor
    public MySQLConfig(String host, int port, String user, String password, String database) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = (password == null) ? "" : password;
        this.database = database;
    }

    // Getter
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUser() { return user; }
    public String getPassword() { return (password == null) ? "" : password; }
    public String getDatabase() { return database; }

    // Setter
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setUser(String user) { this.user = user; }
    public void setPassword(String password) { this.password = (password == null) ? "" : password; }
    public void setDatabase(String database) { this.database = database; }

    // JSON laden
    public static MySQLConfig loadFromFile(File file) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(file)) {
            MySQLConfig config = gson.fromJson(reader, MySQLConfig.class);
            if (config.password == null) {
                config.password = "";
            }
            return config;
        }
    }

    // JSON speichern
    public void saveToFile(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(this, writer);
        }
    }

    // Validierung
    public boolean isValid() {
        return host != null && !host.isEmpty()
                && port > 0
                && user != null && !user.isEmpty()
                && database != null && !database.isEmpty();
    }
}
