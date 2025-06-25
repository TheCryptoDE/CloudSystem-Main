package de.sync.cloud.command;

import de.sync.cloud.CloudSystem;
import de.sync.cloud.group.GroupCreateTask;
import de.sync.cloud.group.GroupManager;
import de.sync.cloud.task.*;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private final Map<String, Task> commands = new HashMap<>();

    public CommandManager(CloudSystem cloud, GroupManager groupManager) {
        // Statt zwei Tasks jetzt nur ein ServiceCommandTask für "service"
        register("service", new ServiceCommandTask(cloud));
        register("group", new GroupCreateTask(groupManager));
        register("help", new HelpTask(commands));
    }

    public void register(String name, Task task) {
        commands.put(name.toLowerCase(), task);
    }

    public void executeCommand(String input) {
        String[] args = input.trim().split("\\s+");
        if (args.length == 0 || args[0].isEmpty()) {
            System.out.println("Ungültiger Befehl. Nutze 'help' für eine Liste.");
            return;
        }

        String commandKey = args[0].toLowerCase();

        Task task = commands.get(commandKey);
        if (task != null) {
            try {
                task.execute(args);
            } catch (Exception e) {
                System.out.println("Fehler bei der Ausführung: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Unbekannter Befehl: " + commandKey);
            System.out.println("Nutze 'help' für eine Liste aller Befehle.");
        }
    }
}
