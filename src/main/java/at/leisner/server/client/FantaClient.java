package at.leisner.server.client;

import at.leisner.server.packet.Packet;

import java.io.*;
import java.net.*;

public class FantaClient implements Client {
    private final Socket socket;
    private final ObjectOutputStream output;

    public FantaClient(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new ObjectOutputStream(socket.getOutputStream());
    }
    public FantaClient(Socket socket, ObjectOutputStream output) {
        this.socket = socket;
        this.output = output;
    }

    @Override
    public void sendData(Packet packet) throws IOException {
        output.writeObject(packet);
        output.flush();
    }

    @Override
    public void disconnect() throws IOException {
        socket.close();
    }
}
