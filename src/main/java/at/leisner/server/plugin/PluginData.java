package at.leisner.server.plugin;

import at.leisner.server.handler.Handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PluginData {
    private final String id;
    private final JavaPlugin javaPlugin;
    private final Plugin plugin;
    private final String[] types;
    private boolean state = false;
    private final Map<String, Handler> clientHandlerMap = new HashMap<>();
    private boolean shouldLoad = true;
    private boolean trust = false;

    public PluginData(String id, JavaPlugin javaPlugin, Plugin plugin, String[] types) {
        this.id = id;
        this.javaPlugin = javaPlugin;
        this.plugin = plugin;
        this.types = types;
    }

    public String getId() {
        return id;
    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String[] getTypes() {
        return types;
    }

    public boolean hasRegisteredType(String type) {
        return Arrays.asList(types).contains(type);
    }

    public boolean isEnable() {
        return state;
    }

    public void setEnable(boolean state) {
        this.state = state;
    }

    public Map<String, Handler> getClientHandlerMap() {
        return clientHandlerMap;
    }

    public boolean isShouldLoad() {
        return shouldLoad;
    }

    public void setShouldLoad(boolean shouldLoad) {
        this.shouldLoad = shouldLoad;
    }

    public boolean isTrust() {
        return trust;
    }

    public void setTrust(boolean trust) {
        this.trust = trust;
    }
}
