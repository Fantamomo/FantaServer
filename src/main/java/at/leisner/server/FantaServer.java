package at.leisner.server;

import at.leisner.packet.ClientTypePacket;
import at.leisner.server.client.FantaClient;
import at.leisner.server.client.FantaDumpClient;
import at.leisner.server.command.FantaCommandManager;
import at.leisner.server.command.commands.HelpCommand;
import at.leisner.server.command.commands.StopCommand;
import at.leisner.server.event.FantaEventManager;
import at.leisner.server.event.client.ClientConnectEvent;
import at.leisner.server.file.FantaFileManager;
import at.leisner.server.gui.ServerManagerGUI;
import at.leisner.server.handler.ClientHandler;
import at.leisner.server.handler.Handler;
import at.leisner.server.lang.FantaLanguage;
import at.leisner.server.logging.Logger;
import at.leisner.server.logging.LoggerSetup;
import at.leisner.packet.Packet;
import at.leisner.packet.PacketType;
import at.leisner.server.plugin.FantaPluginManager;
import at.leisner.server.plugin.JavaPlugin;
import at.leisner.server.stream.CustomObjectInputStream;
import at.leisner.server.user.FantaPermissionManager;
import at.leisner.server.util.ExecuteIfNotExist;
import at.leisner.server.util.ServerUtil;
import at.leisner.server.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.logging.Level.*;


public class FantaServer implements Server {
    private static final int PORT = 29001;
    private final FantaPluginManager pluginManager;
    private final FantaEventManager eventManager;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private final FantaCommandManager commandManager;
    private static FantaServer instance;
    private final Logger logger;
    private final FantaFileManager fileManager;
    private final FantaLanguage language;
    private int connectedClientNumberSinceStart = 0;
    private FantaPermissionManager permissionManager;
    private final File mainPath;
    private ServerManagerGUI serverManagerGUi;


    {
        try {
            File mainPath1 = new File(FantaServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            mainPath = mainPath1.getParentFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public FantaServer() throws IOException {
        instance = this;
        fileManager = new FantaFileManager(mainPath, null);
        language = fileManager.createLangFile("lang.yml");
        logger = LoggerSetup.createLogger("SERVER", language);
        pluginManager = new FantaPluginManager(this);
        eventManager = new FantaEventManager(this);
        commandManager = new FantaCommandManager(this);
        serverSocket = new ServerSocket(PORT);
        threadPool = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void start() {
        logger.lang("server.started", INFO, PORT);
        Util.createDirectory("./plugins", "", "");
        Util.createDirectory("./lib", "", "");
        Util.createDirectory("./data", "", "");
        loadPlugins();
        threadPool.submit(this::acceptClients);


//        Console console = new Console(commandManager);
//        threadPool.submit(console::start);
        registerCommands();
        permissionManager = ServerUtil.loadDefaultFile(new File("./data/userdata.ser"), (ExecuteIfNotExist<? extends FantaPermissionManager>) () -> {
            FantaPermissionManager fantaPermissionManager = new FantaPermissionManager();
            fantaPermissionManager.addUser("Fantamomo", "password");
            return fantaPermissionManager;
        },this.getClass().getClassLoader());
        serverManagerGUi = new ServerManagerGUI(this, commandManager);
        serverManagerGUi.start();
    }

    private void registerCommands() {
//        commandManager.registerGlobalCommand(new ExampleCommand());
        commandManager.registerGlobalCommand(new StopCommand(this));
        commandManager.registerGlobalCommand(new HelpCommand(this));
    }

    public void shutdown() {
        pluginManager.disableAllPlugins();
        ServerUtil.saveObject(new File("./data/userdata.ser"), permissionManager);
//        acceptClientsThread.cancel(true);
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverManagerGUi.dispose();
        threadPool.shutdown();
        new Thread(() -> System.exit(0)).start();
    }
    private void acceptClients() {
        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (!serverSocket.isClosed()) e.printStackTrace();
            }
        }
    }

    private void loadPlugins() {
        File pluginDir = new File("./plugins");
        logger.lang("plugins.start_loading", INFO);
        long time = System.currentTimeMillis();
        for (File file : Objects.requireNonNull(pluginDir.listFiles((dir, name) -> name.endsWith(".jar")))) {
            pluginManager.loadPlugin(file);
        }
        logger.lang("plugins.finish_loading", INFO, pluginManager.countPlugins(), (System.currentTimeMillis() - time));
        pluginManager.initializePlugins();
    }

    private void handleClient(@NotNull Socket clientSocket) {
        try {
            Handler handler;
            ClientTypePacket clientTypePacket;
            ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
            CustomObjectInputStream input = new CustomObjectInputStream(clientSocket.getInputStream(), this.getClass().getClassLoader());
            Packet typePacket = (Packet) input.readObject();

            if (typePacket.packetType() == PacketType.CLIENT_TYPE) {
                clientTypePacket = (ClientTypePacket) typePacket.objects()[0];
                handler = pluginManager.getPluginHandler(clientTypePacket.getObject());
            } else {
                return;
            }
//            Thread.currentThread().setName(((Plugin) pluginManager.getPluginByType(clientTypePacket.getObject())).id() + "-" + clientTypePacket.getObject() + "-" + connectedClientNumberSinceStart);

            ClientHandler clientHandler = handler.getClientHandler();
            if (clientHandler != null || !handler.isEnable()) {
                logger.lang("client.connected_successful", INFO, clientTypePacket.getObject());
                FantaClient client = new FantaClient(clientSocket, output, clientTypePacket.getType());
                JavaPlugin javaPlugin = pluginManager.getPluginByType(clientTypePacket.getObject());
                ClientConnectEvent clientConnectEvent = new ClientConnectEvent(new FantaDumpClient(client), clientTypePacket.getObject(), javaPlugin);
                input.setClassLoader(pluginManager.getPluginByType(clientTypePacket.getObject()).getClassLoader());
                eventManager.callEvent(clientConnectEvent);
                while (handler.isEnable() && !clientConnectEvent.isCancelled()) {
                    try {
                        Packet packet = (Packet) input.readObject();
                        if (javaPlugin.getAllowedPackets() == null) {
                            if (handler.getFilter().filter(client, packet)) {
                                clientHandler.handle(client, packet);
                            }
                        } else if (Arrays.asList(javaPlugin.getAllowedPackets()).contains(packet.messagePacket())) {
                            if (handler.getFilter().filter(client, packet)) {
                                clientHandler.handle(client, packet);
                            }
                        }

                    } catch (SocketException se) {
                        logger.lang("client.disconnect_error", WARNING);
                        se.printStackTrace();
                        clientSocket.close();
                        return;
                    } catch (ClassNotFoundException e) {
                        logger.lang("client.parsed_packet_error", SEVERE);
//                        logger.severe("""
//                                A packet receive with a class with is not in the Server-API or in the Java standard bibliothek. If you want to send custom classes in Packets please override the "getPacket(PacketInputStream packetInputStream)" methode in you Plugin and give it the following code: "
//                                try {
//                                    return (Packet) objectInputStream.readObject();
//                                } catch (IOException | ClassNotFoundException e) {
//                                    throw new RuntimeException(e);
//                                }
//                                and add die custom class in your Plugin""");
                    } catch (StreamCorruptedException e) {
                        logger.lang("client.wrong_packets_error", SEVERE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                language.getAndPrint("client.disconnect", logger::info);
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

    public static FantaServer getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public FantaCommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public int getConnectedClientNumberSinceStart() {
        return connectedClientNumberSinceStart;
    }

    @Override
    public FantaPermissionManager getUserManager(JavaPlugin javaPlugin) {
        if (!pluginManager.trust(javaPlugin)) return null;
        return permissionManager;
    }
    public FantaLanguage getLanguage() {
        return language;
    }

    public ServerManagerGUI getServerManagerGUi() {
        return serverManagerGUi;
    }
}
