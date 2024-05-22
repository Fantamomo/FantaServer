package at.leisner.server.handler;

import at.leisner.server.client.Client;

import java.util.ArrayList;
import java.util.List;

public class FantaHandler implements Handler {
    private ClientHandler clientHandler;
    private Filter filter = (client, inputStream) -> true;
    private Settings settings;
    private boolean enable = true;
    private List<Client> currentClients = new ArrayList<>();

    public FantaHandler() {
    }

    @Override
    public ClientHandler getClientHandler() {
        return clientHandler;
    }
    @Override
    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }
    @Override
    public Filter getFilter() {
        return filter;
    }
    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    @Override
    public Settings getSettings() {
        return settings;
    }
    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
    }
    @Override
    public void disableHandler() {
        enable = false;
    }
    @Override
    public void enableHandler() {
        enable = true;
    }
    @Override
    public List<Client> getCurrentClients() {
        return currentClients;
    }
    public void addClient(Client client) {
        currentClients.add(client);
    }
    public void removeClient(Client client) {
        currentClients.remove(client);
    }
    @Override
    public boolean isEnable() {
        return enable;
    }
}
