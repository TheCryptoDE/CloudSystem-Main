package de.sync.cloud.module;

import de.sync.cloud.networking.PrintInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class ModuleLoader {

    private final File modulesDir;

    public ModuleLoader(File modulesDir) {
        this.modulesDir = modulesDir;
    }

    public List<Module> loadModules() throws IOException, ReflectiveOperationException {
        List<Module> modules = new ArrayList<>();

        if (!modulesDir.exists() || !modulesDir.isDirectory()) {
            //System.out.println("Module-Ordner existiert nicht oder ist kein Verzeichnis.");
            return modules;
        }

        File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            PrintInfo.printModule("Keine Module im modules-Ordner gefunden.");
            return modules;
        }

        for (File jarFile : jarFiles) {
            try (JarFile jar = new JarFile(jarFile)) {
                URL[] urls = { jarFile.toURI().toURL() };
                try (URLClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader())) {
                    jar.stream()
                        .filter(e -> e.getName().endsWith(".class"))
                        .forEach(entry -> {
                            String className = entry.getName()
                                .replace("/", ".")
                                .replace(".class", "");
                            try {
                                Class<?> clazz = cl.loadClass(className);
                                if (Module.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                                    Module module = (Module) clazz.getDeclaredConstructor().newInstance();
                                    modules.add(module);
                                    PrintInfo.printModule("Modul geladen: " + module.getName());
                                }
                            } catch (Exception ex) {
                                PrintInfo.printModule("Fehler beim Laden der Klasse " + className + ": " + ex.getMessage());
                            }
                        });
                }
            }
        }

        return modules;
    }
}
