package at.leisner.server;

import at.leisner.packet.ClientTypePacket;
import at.leisner.server.client.FantaClient;
import at.leisner.server.command.FantaCommandManager;
import at.leisner.server.command.commands.ExampleCommand;
import at.leisner.server.console.Console;
import at.leisner.server.event.FantaEventManager;
import at.leisner.server.event.client.ClientConnectEvent;
import at.leisner.server.handler.ClientHandler;
import at.leisner.server.handler.Handler;
import at.leisner.server.logging.LoggerSetup;
import at.leisner.packet.Packet;
import at.leisner.packet.PacketType;
import at.leisner.server.plugin.FantaPluginManager;
import at.leisner.server.plugin.JavaPlugin;
import at.leisner.server.util.Util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class FantaServer implements Server {
    private static final int PORT = 29001;
    private final FantaPluginManager pluginManager;
    private final FantaEventManager eventManager;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final FantaCommandManager commandManager;
    private static FantaServer instance;
    private final Logger logger;

    public FantaServer() throws IOException {
        instance = this;
        logger = LoggerSetup.createLogger("server");
        pluginManager = new FantaPluginManager(this);
        eventManager = new FantaEventManager(this);
        commandManager = new FantaCommandManager();
        serverSocket = new ServerSocket(PORT);
        threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        logger.info("FantaServer started on Port "+PORT+"!");
        Util.createDirectory("./plugins","","");
        Util.createDirectory("./lib","","");
        loadPlugins();
        acceptClients();
        Console console = new Console(commandManager);
        threadPool.submit(console::start);
        commandManager.registerCommand(new ExampleCommand());
    }

    
    private void acceptClients() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadPlugins() {
        File pluginDir = new File("./plugins");
        logger.info("Loading Plugins!");
        long time = System.nanoTime();
        for (File file : Objects.requireNonNull(pluginDir.listFiles((dir, name) -> name.endsWith(".jar")))) {
            pluginManager.loadPlugin(file);
        }
        logger.info("Loading "+pluginManager.countPlugins()+" Plugins in "+ (System.nanoTime() - time) +"ms!");
        pluginManager.initializePlugins();
    }

    private void handleClient(Socket clientSocket) {
        try {
            Handler handler;
            ClientTypePacket clientTypePacket;
            logger.info("1");
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
            logger.info("2");
            ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
            logger.info("3");
            Packet typePacket = (Packet) input.readObject();

            logger.info("4");
            if (typePacket.packetType() == PacketType.CLIENT_TYPE) {
                clientTypePacket = (ClientTypePacket) typePacket.objects()[0];
                handler = pluginManager.getPluginHandler(clientTypePacket.getObject());
            } else {
                return;
            }

            ClientHandler clientHandler = handler.getClientHandler();
            if (clientHandler != null || !handler.isEnable()) {
                FantaClient client = new FantaClient(clientSocket, output);
                ClientConnectEvent clientConnectEvent = new ClientConnectEvent(client, clientTypePacket.getObject(),pluginManager.getPluginByType(clientTypePacket.getObject()));
                eventManager.callEvent(clientConnectEvent);
                while (handler.isEnable() && !clientConnectEvent.isCancelled()) {
                    Packet packet = (Packet) input.readObject();
                    if (handler.getFilter().filter(client, packet)) {
                        clientHandler.handle(client, packet);
                    }
                }
            } else {
                logger.info("Client disconnected");
                clientSocket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new FantaServer().start();
    }

    @Override
    public FantaPluginManager getPluginmanager() {
        return pluginManager;
    }

    @Override
    public FantaEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public JavaPlugin getPlugin(String id) {
        return pluginManager.getPlugin(id);
    }

    public static Server getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }
    @Override
    public FantaCommandManager getCommandManager() {
        return commandManager;
    }
}
