package at.leisner.server.plugin;

import at.leisner.server.FantaServer;
import at.leisner.server.event.plugin.PluginRegisterHandlerEvent;
import at.leisner.server.handler.FantaHandler;
import at.leisner.server.handler.Handler;
import at.leisner.server.logging.LoggerSetup;
import at.leisner.server.error.PluginNotEnableException;
import at.leisner.server.error.PluginNotExistException;
import at.leisner.server.error.PluginWasEnableException;
import at.leisner.server.handler.ClientHandler;
import at.leisner.server.util.Util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FantaPluginManager implements PluginManager {
    private Map<String, PluginData> plugins = new HashMap<>();
    private Map<String, Handler> handlers = new HashMap<>();
    private final FantaServer server;

    public FantaPluginManager(FantaServer server) {
        this.server = server;
    }

    @Override
    public void loadPlugin(File file) {
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
            URLClassLoader loader = URLClassLoader.newInstance(urls);
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    Class<?> clazz = loader.loadClass(className);
                    if (clazz.isAnnotationPresent(Plugin.class)) {
                        JavaPlugin plugin = (JavaPlugin) clazz.getDeclaredConstructor().newInstance();
                        Plugin annotation = clazz.getAnnotation(Plugin.class);
                        plugins.put(annotation.id(), new PluginData(annotation.id(), plugin, annotation, annotation.types()));
                        Util.updatePrivateVariable(plugin,"fantaServerServer", FantaServer.getInstance());
                        Util.updatePrivateVariable(plugin, "fantaServerLogger", LoggerSetup.createLogger(annotation.name()));
                        server.getEventManager().registerEvents(plugin);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ClientHandler[] getTypeHandlers(String s) {
        return new ClientHandler[0];
    }

    @Override
    public Map<String, Handler> getPluginHandlerForPlugin(JavaPlugin javaPlugin) throws PluginNotEnableException {
        if (!getPluginData(javaPlugin).isEnable()) throw new PluginNotEnableException("The Plugin "+javaPlugin.getClass()+" wasn't enable", javaPlugin);
        return getPluginData(javaPlugin).getClientHandlerMap();
    }

    @Override
    public void enablePlugin(JavaPlugin javaPlugin) throws PluginWasEnableException {
        if (!getPluginData(javaPlugin).isEnable()) throw new PluginWasEnableException("The Plugin "+javaPlugin.getClass()+" was enable", javaPlugin);
        javaPlugin.onEnable();
    }

    @Override
    public JavaPlugin getPlugin(String id) {
        if (!plugins.containsKey(id)) return null;
        return plugins.get(id).getJavaPlugin();
    }
    public JavaPlugin getPluginByType(String type) {
        for (PluginData plugin : plugins.values()) {
            for (String type0 : plugin.getTypes()) {
                if (type.equals(type0)) return plugin.getJavaPlugin();
            }
        }
        return null;
    }

    @Override
    public void disablePlugin(JavaPlugin javaPlugin) throws PluginNotEnableException {
        if (getPluginData(javaPlugin).isEnable()) throw new PluginNotEnableException("The Plugin "+javaPlugin.getClass()+" wasn't enable", javaPlugin);
        javaPlugin.onDisable();
    }

    @Override
    public Plugin getPluginInfo(JavaPlugin javaPlugin) {
        for (PluginData plugin : plugins.values()) {
            if (plugin.getJavaPlugin() == javaPlugin) return plugin.getPlugin();
        }
        return null;
    }

    @Override
    public boolean isPluginEnable(JavaPlugin javaPlugin) {
        return getPluginData(javaPlugin).isEnable();
    }

    @Override
    public boolean pluginExist(String id) throws PluginNotExistException {
        return plugins.containsKey(id);
    }

    @Override
    public void registerClientHandler(JavaPlugin javaPlugin, String type, ClientHandler clientHandler) {
//        if (!getPluginData(javaPlugin).isEnable()) return;
//        if (!Arrays.asList(getPluginData(javaPlugin).getTypes()).contains(type)) return;
//        handlers.put(type,clientHandler);
//        getPluginHandlerForPlugin(javaPlugin).put(type,clientHandler);
    }

    @Override
    public void unregisterClientHandler(JavaPlugin javaPlugin, String type) {
        if (getPluginHandlerForPlugin(javaPlugin).containsKey(type)) {
            getPluginHandlerForPlugin(javaPlugin).remove(type);
            handlers.remove(type);
        }
    }

    @Override
    @Deprecated
    public void unregisterClientHandler(JavaPlugin javaPlugin, ClientHandler clientHandler) {
    }

    @Override
    public void unregisterAllClientHandlers(JavaPlugin javaPlugin) {
        for (String type : handlers.keySet()) {
            unregisterClientHandler(javaPlugin,type);
        }
    }

    @Override
    public Handler getHandler(JavaPlugin javaPlugin, String type) {
        if (!getPluginData(javaPlugin).isEnable()) return null;
        if (!Arrays.asList(getPluginData(javaPlugin).getTypes()).contains(type)) return null;
        if (handlers.containsKey(type)) return handlers.get(type);
        FantaHandler handler = new FantaHandler();
        PluginRegisterHandlerEvent pluginRegisterHandlerEvent = new PluginRegisterHandlerEvent(javaPlugin, handler);
        server.getEventManager().callEvent(pluginRegisterHandlerEvent);
        if (pluginRegisterHandlerEvent.isCancelled()) return new FantaHandler();
        handlers.put(type, handler);
        getPluginHandlerForPlugin(javaPlugin).put(type,handler);
        return handler;
    }

    @Override
    public int countEnablePlugins() {
        int count = 0;
        for (PluginData plugin : plugins.values()) {
            if (plugin.isEnable()) count += 1;
        }
        return count;
    }

    @Override
    public int countPlugins() {
        return plugins.size();
    }

    public void initializePlugins() {
        for (PluginData pluginData : plugins.values()) {
            JavaPlugin plugin = pluginData.getJavaPlugin();
            getPluginData(plugin).setEnable(true);
            plugin.onLoad();
            plugin.onEnable();
        }
    }

    public ClientHandler getPluginClientHandler(String type) {
        return handlers.get(type).getClientHandler();
    }
    public Handler getPluginHandler(String type) {
        return handlers.get(type);
    }
    public PluginData getPluginData(JavaPlugin javaPlugin) {
        for (PluginData plugin : plugins.values()) {
            if (plugin.getJavaPlugin() == javaPlugin) return plugin;
        }
        return null;
    }

}
