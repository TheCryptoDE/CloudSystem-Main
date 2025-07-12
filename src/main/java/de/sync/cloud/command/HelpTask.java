package de.sync.cloud.command;

import de.sync.cloud.logger.Loggers;
import de.sync.cloud.logger.LoggersType;
import de.sync.cloud.networking.PrintInfo;
import de.sync.cloud.task.Task;

import java.util.Map;

public class HelpTask implements Task {

    @Override
    public void execute(String[] args) {
        PrintInfo.printInfo("Available commands:");

        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Available commands:");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, " ");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`version` Aliases: [version, ver] - Displays the version of the CloudSystem.");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`stop` Aliases: [stop, shutdown] - Stops the CloudSystem.");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`startserver` Aliases: [startserver, cstart] - Starts a server.");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`stopserver` Aliases: [stopserver, cstop] - Stops a server.");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`group` Aliases: [group] - Creates a group.");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`service` Aliases: [service] - Creates a service.");

        new Loggers(LoggersType.INFO, Loggers.useColorSystem, " ");

        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Threads: " + Runtime.getRuntime().availableProcessors());
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Operating System: " + System.getProperty("os.name"));
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Support: http://vortexhost.de");

    }
}
