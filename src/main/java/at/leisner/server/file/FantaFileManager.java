package at.leisner.server.file;

import at.leisner.server.FantaServer;
import at.leisner.server.lang.FantaLanguage;
import at.leisner.server.lang.Language;
import at.leisner.server.plugin.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class FantaFileManager implements FileManager {
    private final File pluginDirectory;
    private FantaLanguage language;
    private final JavaPlugin javaPlugin;

    public FantaFileManager(File pluginDirectory, JavaPlugin javaPlugin) {
        this.pluginDirectory = pluginDirectory;
        this.javaPlugin = javaPlugin;
    }

    @Override
    public File getPluginDirectory() {
        return pluginDirectory;
    }

    @Override
    public File createPluginDirectory() {
        pluginDirectory.mkdir();
        return pluginDirectory;
    }

    @Override
    public File createFile(String newFile) {
        File file = new File(pluginDirectory, newFile);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    @Override
    public File getFile(String newFile) {
        return new File(pluginDirectory, newFile);
    }

    @Override
    public File createFile(String newFile, boolean ignoreIfExist) {
        File file = new File(pluginDirectory, newFile);
        if (!ignoreIfExist && file.exists()) {
            return file;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    @Override
    public FantaLanguage createLang() {
        return createLangFile("lang.yml");
    }

    public FantaLanguage createLangFile(String file) {
        if (language != null) return language;
        try {
            language = new FantaLanguage(createFile(file, false));
            if (javaPlugin != null) javaPlugin.getLogger().setLanguage(language);
            return language;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public File createConfigFile() {
        return createFile("config.yml", false);
    }
    public File createConfigFile(String file) {
        return createFile(file, false);
    }

    @Override
    public File getConfig() {
        return new File(pluginDirectory, "config.yml");
    }

    @Override
    public FantaLanguage getLang() {
        return language;
    }

    @Override
    public boolean exist(String file) {
        return new File(pluginDirectory, file).exists();
    }
}
