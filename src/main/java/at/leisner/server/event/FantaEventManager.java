package at.leisner.server.event;

import at.leisner.server.FantaServer;
import at.leisner.server.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantaEventManager implements EventManager {
    private final Map<Class<? extends Event>, List<RegisteredListener>> listeners = new HashMap<>();
    private final Map<Plugin, List<RegisteredListener>> pluginListeners = new HashMap<>();
    private final FantaServer server;

    public FantaEventManager(FantaServer server) {
        this.server = server;
    }

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0])) {
                    throw new IllegalArgumentException("Method " + method + " in listener " + listener + " has @EventHandler annotation but does not have a single argument of type Event.");
                }
                Class<? extends Event> eventType = parameterTypes[0].asSubclass(Event.class);
                method.setAccessible(true);

                RegisteredListener registeredListener = new FantaRegisteredListener(listener, method, plugin);

                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(registeredListener);
                pluginListeners.computeIfAbsent(plugin, k -> new ArrayList<>()).add(registeredListener);
            }
        }
    }
    public void registerEvents(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0])) {
                    throw new IllegalArgumentException("Method " + method + " in object " + object + " has @EventHandler annotation but does not have a single argument of type Event.");
                }
                Class<? extends Event> eventType = parameterTypes[0].asSubclass(Event.class);
                method.setAccessible(true);

                RegisteredListener registeredListener = new FantaRegisteredListener(object, method);

                listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(registeredListener);
            }
        }
    }
    @Override
    public void unregisterEvents(Listener listener) {
        for (List<RegisteredListener> registeredListeners : listeners.values()) {
            registeredListeners.removeIf(registeredListener -> registeredListener.getListener().equals(listener));
        }

        for (List<RegisteredListener> registeredListeners : pluginListeners.values()) {
            registeredListeners.removeIf(registeredListener -> registeredListener.getListener().equals(listener));
        }
    }
    @Override
    public void unregisterAllEvents(Plugin plugin) {
        List<RegisteredListener> registeredListeners = pluginListeners.remove(plugin);
        if (registeredListeners != null) {
            for (RegisteredListener registeredListener : registeredListeners) {
                listeners.get(registeredListener.getEventType()).remove(registeredListener);
            }
        }
    }
    @Override
    public void callEvent(Event event) {
        List<RegisteredListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (RegisteredListener registeredListener : eventListeners) {
                try {
                    registeredListener.getMethod().invoke(((FantaRegisteredListener) registeredListener).getObject(), event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (event instanceof Cancelable cancelable) {
            if (cancelable.isCancelled()) {
                cancelable.callIfCanceled();
            } else {
                event.callEvent();
            }
        } else {
            event.callEvent();
        }
    }
}
