package de.sync.cloud.command;

import de.sync.cloud.networking.PrintInfo;
import de.sync.cloud.task.Task;

import java.util.Map;

public class HelpTask implements Task {

    private final Map<String, Task> commandMap;

    public HelpTask(Map<String, Task> commandMap) {
        this.commandMap = commandMap;
    }

    @Override
    public void execute(String[] args) {
        PrintInfo.printInfo("Verfügbare Befehle:");
        for (String command : commandMap.keySet()) {
            PrintInfo.printInfo("- " + command);
        }
    }
}
