package at.leisner.server.lang;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class FantaLanguage implements Language {
    private Map<String, Object> yamlData;

    public FantaLanguage(File file) throws IOException {
        Yaml yaml = new Yaml(new Constructor(Map.class, new LoaderOptions()));
        try (FileInputStream fis = new FileInputStream(file)) {
            yamlData = yaml.load(fis);
        }
    }

    public String get(String key) {
        return get(key, key);
    }

    public String get(String key, String defaultValue) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = yamlData;
        Object value = null;

        for (String k : keys) {
            if (currentMap == null) {
                return defaultValue;
            }
            value = currentMap.get(k);
            if (value instanceof Map) {
                currentMap = (Map<String, Object>) value;
            } else {
                currentMap = null;
            }
        }

        return value != null ? value.toString() : defaultValue;
    }

    @Override
    public String format(String key, Object... objects) {
        return get(key).formatted(objects);
    }

    @Override
    public String format(String key, String defaultValue, Object... objects) {
        return get(key, defaultValue).formatted(objects);
    }

    @Override
    public String getAndPrint(String key, Consumer<String> consumer) {
        String value = get(key);
        consumer.accept(value);
        return value;
    }

    @Override
    public String getAndPrint(String key, String defaultValue, Consumer<String> consumer) {
        String value = get(key, defaultValue);
        consumer.accept(value);
        return value;
    }

    @Override
    public String formatAndPrint(String key, Consumer<String> consumer, Object... objects) {
        String value = format(key, objects);
        consumer.accept(value);
        return value;
    }

    @Override
    public String formatAndPrint(String key, String defaultValue, Consumer<String> consumer, Object... objects) {
        String value = format(key, defaultValue, objects);
        consumer.accept(value);
        return value;
    }
}
