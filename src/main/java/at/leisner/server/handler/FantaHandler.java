package at.leisner.server.handler;

import at.leisner.packet.Packet;
import at.leisner.packet.PacketHandler;
import at.leisner.server.client.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantaHandler implements Handler {
    private Filter filter = (client, inputStream) -> true;
    private Settings settings;
    private List<Client> currentClients = new ArrayList<>();
    private final Map<Class<? extends Packet>, PacketHandler> packetHandlerMap = new HashMap<>();

    public FantaHandler() {}

    public void addClient(Client client) {
        currentClients.add(client);
    }
    public void removeClient(Client client) {
            currentClients.remove(client);
    }

    @Override
    public <T extends Packet> void setPacketHandler(Class<T> aClass, PacketHandler packetHandler) {
        packetHandlerMap.put(aClass, packetHandler);
    }

    @Override
    public void setPacketHandler(List<Class<? extends Packet>> list, PacketHandler packetHandler) {
        list.forEach((packet -> packetHandlerMap.put(packet, packetHandler)));
    }

    @Override
    public void setFilter(Filter filter) {
        if (filter != null) this.filter = filter;
    }

    @Override
    public void setSettings(Settings settings) {
        if (settings != null) this.settings = settings;
    }

    @Override
    public <T> PacketHandler getPacketHandler(Class<T> aClass) {
        return packetHandlerMap.get(aClass);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public <T extends Packet> void disableHandler(Class<T> aClass) {}

    @Override
    public <T extends Packet> void enableHandler(Class<T> aClass) {}

    @Override
    public List<Client> getCurrentClients() {
        return currentClients;
    }

    @Override
    public <T extends Packet> boolean isEnable(Class<T> aClass) {
        return true;
    }

    public Map<Class<? extends Packet>, PacketHandler> getPacketHandlerMap() {
        return packetHandlerMap;
    }
}
