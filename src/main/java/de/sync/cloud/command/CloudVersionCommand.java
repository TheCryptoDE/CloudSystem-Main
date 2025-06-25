package de.sync.cloud.command;

import de.sync.cloud.logger.Loggers;
import de.sync.cloud.logger.LoggersType;
import de.sync.cloud.task.Task;

public class CloudVersionCommand implements Task {
    @Override
    public void execute(String[] args) throws Exception {
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "TheSyncCloud das CloudSystem der zukunft");
        new Loggers(LoggersType.INFO, Loggers.useColorSystem, "Verwendete (EARTHQUAKE-1.0.1)");
    }
}
