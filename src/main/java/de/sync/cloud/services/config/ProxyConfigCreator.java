package de.sync.cloud.services.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProxyConfigCreator {

    public static void createBungeeConfig(File folder, boolean hasLobby, int maxPlayers) throws IOException {
        File configFile = new File(folder, "config.yml");
        if (configFile.exists()) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write("listeners:\n");
            writer.write("- query_port: 25577\n");
            writer.write("  motd: '&6BungeeCord ProxyWatchdog'\n");
            writer.write("  tab_list: GLOBAL_PING\n");
            writer.write("  query_enabled: false\n");
            writer.write("  host: 0.0.0.0:25577\n");
            writer.write("  forced_hosts:\n");
            if (hasLobby) {
                writer.write("    lobby.example.com: lobby\n");
            }
            writer.write("  ping_passthrough: false\n");
            writer.write("  priorities:\n");
            writer.write("  - lobby\n");
            writer.write("  max_players: " + maxPlayers + "\n");
            writer.write("timeout: 30000\n");
            writer.write("player_limit: 100\n");
            writer.write("permissions:\n");
            writer.write("  default:\n");
            writer.write("  - bungeecord.command.server\n");
            writer.write("  - bungeecord.command.list\n");
            writer.write("groups:\n");
            writer.write("  md_5:\n");
            writer.write("  - admin\n");
            writer.write("log_commands: false\n");
            writer.write("connection_throttle: 4000\n");
        }
    }

}
