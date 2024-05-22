package at.leisner.server.event;

import at.leisner.server.plugin.Plugin;

import java.lang.reflect.Method;

public class FantaRegisteredListener extends RegisteredListener {
    private final Object listener;
    private final Method method;
    private Plugin plugin;
    private final Class<? extends Event> eventType;

    public FantaRegisteredListener(Listener listener, Method method, Plugin plugin) {
        this(listener, method);
        this.plugin = plugin;
    }
    public FantaRegisteredListener(Object listener, Method method) {
        this.listener = listener;
        this.method = method;
        this.eventType = (Class<? extends Event>) method.getParameterTypes()[0];
    }

    public Listener getListener() {
        return listener instanceof Listener ? (Listener) listener : null;
    }
    public Object getObject() {
        return listener;
    }

    public Method getMethod() {
        return method;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Class<? extends Event> getEventType() {
        return eventType;
    }
}
