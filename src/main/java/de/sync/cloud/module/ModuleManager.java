package de.sync.cloud.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {

    private final Map<String, Module> loadedModules = new HashMap<>();

    public void registerModules(List<Module> modules) {
        for (Module module : modules) {
            loadedModules.put(module.getName(), module);
        }
    }

    public void enableAll() throws Exception {
        for (Module module : loadedModules.values()) {
            module.onEnable();
        }
    }

    public void disableAll() throws Exception {
        for (Module module : loadedModules.values()) {
            module.onDisable();
        }
    }

    public Map<String, Module> getModules() {
        return loadedModules;
    }
}
