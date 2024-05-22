package at.leisner.server.util;

import at.leisner.server.logging.LoggerSetup;
import at.leisner.server.plugin.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

public class Util {
    public static void updatePrivateVariable(JavaPlugin plugin,String name, Object object) {
        // Verwenden Sie Reflektion, um auf die private Variable 'server' zuzugreifen und sie zu setzen
        try {
            Field serverField = JavaPlugin.class.getDeclaredField(name);
            serverField.setAccessible(true);
            serverField.set(plugin, object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LoggerSetup.getMainLogger().log(Level.SEVERE,"",e);
        }
    }
    public static void createDirectory(String path, String success, String error) {
        File pluginDir = new File(path);
        if (!pluginDir.exists()) {
            if (pluginDir.mkdirs()) {
                LoggerSetup.getMainLogger().info(success);
            } else {
                LoggerSetup.getMainLogger().warning(error);
            }
        }
    }
}
