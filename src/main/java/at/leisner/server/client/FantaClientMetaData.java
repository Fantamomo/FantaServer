package at.leisner.server.client;

import at.leisner.server.plugin.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FantaClientMetaData implements ClientMetaData {
    private Map<String, Meta> meta = new HashMap<>();

    public void addMeta(String key, Object object, JavaPlugin javaPlugin) {
        meta.put(key, new Meta(object, javaPlugin));
    }

    @Override
    public void setMeta(String key, Object object, JavaPlugin javaPlugin) {
        meta.put(key, new Meta(object, javaPlugin));
    }

    public boolean removeMeta(String key) {
        return meta.remove(key) != null;
    }
    public boolean containsKey(String key) {
        return meta.containsKey(key);
    }
    public boolean containsValue(Object object, JavaPlugin javaPlugin) {
        return meta.containsValue(new Meta(object, javaPlugin));
    }

    @Override
    public Object get(String key) {
        return meta.get(key);
    }

    @Override
    public Object getOrDefault(String key, Object defaultObject) {
        if (meta.containsKey(key)) return meta.get(key).getObject();
        return defaultObject;
    }

    public Map<String, Meta> getMeta() {
        return meta;
    }
}
