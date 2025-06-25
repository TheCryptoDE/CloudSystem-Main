package de.sync.cloud.command;

import de.sync.cloud.logger.Loggers;
import de.sync.cloud.logger.LoggersType;
import de.sync.cloud.networking.PrintInfo;
import de.sync.cloud.task.Task;

import java.util.Map;

public class HelpTask implements Task {

    @Override
    public void execute(String[] args) {
        PrintInfo.printInfo("Verfügbare Befehle:");

        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Verfügbare Befehle:");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, " ");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`version` Aliase: [version, ver] - Zeigt die Version des CloudSystems an.");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`stop` Aliase: [stop, shutdown] - Stoppt das CloudSystem.");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`startserver` Aliase: [startserver, cstart] - Startet unter server");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`stopserver` Aliase: [stopserver, cstop] - Stoppt unter server");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`group` Aliase: [group] - Erstellt eine Gruppe");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "`service` Aliase: [service] - Erstellt einen Service");

        new Loggers(LoggersType.INFO, Loggers.useColorSystem, " ");

        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Threads: " + Runtime.getRuntime().availableProcessors());
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "OS System: " + System.getProperty("os.name"));
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Support: http://vortexhost.de");


    }
}
