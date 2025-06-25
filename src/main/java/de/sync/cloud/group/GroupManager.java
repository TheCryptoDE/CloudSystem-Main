package de.sync.cloud.group;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

public class GroupManager {

    private final Connection connection;

    public GroupManager(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS groups (" +
                            "group_name VARCHAR(255) PRIMARY KEY," +
                            "type VARCHAR(50)," +
                            "static_flag VARCHAR(50)," +
                            "min_server INT," +
                            "max_players INT)"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadGroups() {
        // optional, falls du beim Start alle laden willst
    }

    public void saveGroups() {
        // nicht mehr nötig, da direkt in DB geschrieben wird
    }

    public boolean groupExists(String groupName) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM groups WHERE group_name = ?")) {
            stmt.setString(1, groupName.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addGroup(Group group) throws SQLException {
        if (groupExists(group.getGroupName())) {
            throw new SQLException("Gruppe existiert bereits: " + group.getGroupName());
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO groups (group_name, type, static_flag, min_server, max_players) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, group.getGroupName().toLowerCase());
            stmt.setString(2, group.getType());
            stmt.setString(3, group.getStaticFlag());
            stmt.setInt(4, group.getMinServer());
            stmt.setInt(5, group.getMaxPlayers());
            stmt.executeUpdate();
        }

        // Optional: Template erstellen
        createTemplateForGroup(group);
    }

    private void createTemplateForGroup(Group group) {
        String templatesPath = "./templates/" + group.getGroupName();
        File groupFolder = new File(templatesPath);

        if (!groupFolder.exists()) {
            if (groupFolder.mkdirs()) {
                System.out.println("Template-Ordner für Gruppe '" + group.getGroupName() + "' erstellt.");

                // Spigot 1.21.4 herunterladen und direkt als server.jar speichern
                try {
                    downloadAndRenameSpigotJar(templatesPath);
                    System.out.println("Spigot 1.21.4 heruntergeladen und umbenannt zu server.jar.");
                } catch (IOException e) {
                    System.err.println("Fehler beim Herunterladen der Spigot.jar: " + e.getMessage());
                }

                // server.properties erstellen
                createServerProperties(templatesPath);
                // spigot.yml erstellen
                createSpigotYml(templatesPath);
                // eula.txt erstellen
                createEulaTxt(templatesPath);

            } else {
                System.err.println("Konnte Template-Ordner für Gruppe '" + group.getGroupName() + "' nicht erstellen.");
            }
        } else {
            System.out.println("Template für Gruppe '" + group.getGroupName() + "' existiert bereits.");
        }
    }

    private void downloadAndRenameSpigotJar(String templatesPath) throws IOException {
        String spigotUrl = "https://s3.mcjars.app/spigot/1.21.4/4458/server.jar";
        Path targetPath = Path.of(templatesPath, "server.jar");

        try (InputStream in = new URL(spigotUrl).openStream()) {
            Files.copy(in, targetPath);
        }
    }

    private void createServerProperties(String templatesPath) {
        File file = new File(templatesPath, "server.properties");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("server-port=25565");
            writer.println("max-players=20");
            writer.println("motd=Ein neuer Server von CloudSystem");
            writer.println("online-mode=false");
            writer.println("level-name=world");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createSpigotYml(String templatesPath) {
        File file = new File(templatesPath, "spigot.yml");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("settings:");
            writer.println("  bungeecord: true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createEulaTxt(String templatesPath) {
        File file = new File(templatesPath, "eula.txt");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("eula=true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Collection<Group> getGroups() {
        List<Group> groupList = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM groups");
            while (rs.next()) {
                Group group = new Group(
                        rs.getString("group_name"),
                        rs.getString("type"),
                        rs.getString("static_flag"),
                        rs.getInt("min_server"),
                        rs.getInt("max_players")
                );
                groupList.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupList;
    }


    public Group getGroup(String name) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM groups WHERE group_name = ?")) {
            stmt.setString(1, name.toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Group(
                        rs.getString("group_name"),
                        rs.getString("type"),
                        rs.getString("static_flag"),
                        rs.getInt("min_server"),
                        rs.getInt("max_players")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
