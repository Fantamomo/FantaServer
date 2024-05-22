package at.leisner.server.client;

import at.leisner.packet.Packet;

import javax.net.ssl.SSLSocket;
import java.io.*;

public class FantaClient implements Client {
    private final SSLSocket socket;
    private final ObjectOutputStream output;

    public FantaClient(SSLSocket socket) throws IOException {
        this.socket = socket;
        this.output = new ObjectOutputStream(socket.getOutputStream());
    }
    public FantaClient(SSLSocket socket, ObjectOutputStream output) {
        this.socket = socket;
        this.output = output;
    }

    @Override
    public void sendPacket(Packet packet) throws IOException {
        output.writeObject(packet);
        output.flush();
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }
}
