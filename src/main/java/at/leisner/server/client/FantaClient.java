package at.leisner.server.client;

import at.leisner.packet.Packet;

import java.io.*;
import java.net.Socket;

public class FantaClient implements Client {
    private final Socket socket;
    private final ObjectOutputStream output;
    private final String clientType;
    private final FantaClientMetaData clientMetaData = new FantaClientMetaData();

    public FantaClient(Socket socket, String clientType) throws IOException {
        this.socket = socket;
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.clientType = clientType;
    }
    public FantaClient(Socket socket, ObjectOutputStream output, String clientType) {
        this.socket = socket;
        this.output = output;
        this.clientType = clientType;
    }

    @Override
    public void sendPacket(Packet packet) {
        try {
            output.writeObject(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public FantaClientMetaData getClientMetaData() {
        return clientMetaData;
    }
}
