package de.sync.cloud.console;

import de.sync.cloud.CloudSystem;
import de.sync.cloud.task.Task;

import java.util.Scanner;

import static de.sync.cloud.networking.PrintInfo.*;

public class ConsoleInput {

    public static ConsoleInput consoleInputLoop() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) continue;

            String[] parts = line.trim().split("\\s+");
            String key = parts[0];
            if (key.equalsIgnoreCase("tasks") && parts.length > 1) key += ":" + parts[1];
            else if (key.equalsIgnoreCase("server") && parts.length > 1) key += ":" + parts[1];

            Task task = CloudSystem.commands.get(key.toLowerCase());
            if (task != null) {
                try {
                    task.execute(parts);
                } catch (Exception e) {
                    printError("Error during command execution: " + e.getMessage());
                }
            } else {
                printWarn("Unknown command: " + line);
            }
        }
    }

}
