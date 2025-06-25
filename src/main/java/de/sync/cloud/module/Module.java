package de.sync.cloud.module;

public interface Module {
    String getName();
    void onEnable() throws Exception;
    void onDisable() throws Exception;
}
