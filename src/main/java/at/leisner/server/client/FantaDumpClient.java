package at.leisner.server.client;

import java.io.IOException;

public class FantaDumpClient implements DumpClient {
    private final String clientType;
    private final Client client;

    public FantaDumpClient(FantaClient client) {
        this.clientType = client.getType();
        this.client = client;
    }

    @Override
    public String getType() {
        return clientType;
    }

    @Override
    public ClientMetaData getClientMetaData() {
        return client.getClientMetaData();
    }

    @Override
    public void disconnect() throws IOException {
        client.disconnect();
    }
}
