package at.leisner.server.logging;


import at.leisner.server.FantaServer;
import at.leisner.server.gui.ServerManagerGUI;
import at.leisner.server.lang.Language;
import at.leisner.server.util.ColorCodes;
import at.leisner.server.logging.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LoggerSetup {
    private static final String LOG_FOLDER = "./logs";
    private static final String LOG_FILE = LOG_FOLDER + "/server.log";
    private static FileHandler fileHandler;
    private static GUIHandler guiHandler;
    private static Logger mainLogger;
    private static java.util.logging.Logger rootLogger;

    static {
        try {
            // Ensure the log directory exists
            Files.createDirectories(Paths.get(LOG_FOLDER));

            // Set up the file handler to write to a common log file
            fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new FileFormatter());

            guiHandler = new GUIHandler();
            guiHandler.setFormatter(new GuiFormatter());
            // Also add a console handler with color support
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new ConsoleFormatter());
            // Add the file handler to the root logger
            rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.CONFIG);
            Handler[] handlers = rootLogger.getHandlers();

            // Remove all existing handlers to prevent duplicate logging
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            rootLogger.setUseParentHandlers(false);
            rootLogger.addHandler(fileHandler);
            rootLogger.addHandler(guiHandler);
            rootLogger.addHandler(consoleHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger createLogger(String name, Language language) {
        Logger logger = new at.leisner.server.logging.Logger(name,null, language);
        logger.setParent(rootLogger);
        if (mainLogger == null) {
            mainLogger = logger;
        }
        return logger;
    }
    public static Logger createLogger(String name) {
        Logger logger = new at.leisner.server.logging.Logger(name,null);
        if (mainLogger == null) {
            mainLogger = logger;
        }
        return logger;
    }

    private static class ConsoleFormatter extends Formatter {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String timestamp = dateFormat.format(new Date(record.getMillis()));
            String threadName = Thread.currentThread().getName();
            String loggerName = record.getLoggerName();
            String level = record.getLevel().getName();
            String message = formatMessage(record);
            String color = getColor(record.getLevel());
            return String.format("%s[%s] [%s] [%s-%s]: %s%s%n", color, timestamp, threadName, loggerName,level, message, ColorCodes.RESET);
        }

        private String getColor(Level level) {
            if (level == Level.SEVERE) {
                return ColorCodes.RED.toString();
            } else if (level == Level.WARNING) {
                return ColorCodes.YELLOW.toString();
//            } else if (level == Level.INFO) {
//                return ColorCodes.GREEN;
            } else if (level == Level.CONFIG) {
                return ColorCodes.BLUE.toString();
            } else {
                return ColorCodes.WHITE.toString();
            }
        }
    }
    private static class GuiFormatter extends Formatter {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String timestamp = dateFormat.format(new Date(record.getMillis()));
            String threadName = Thread.currentThread().getName();
            String loggerName = record.getLoggerName();
            String level = record.getLevel().getName();
            String message = formatMessage(record);
            String color = getColor(record.getLevel());
            return String.format("%s[%s] [%s] [%s-%s]: %s%s%n", color, timestamp, threadName, loggerName,level, message, ColorCodes.RESET);
        }

        private String getColor(Level level) {
            if (level == Level.SEVERE) {
                return ColorCodes.RED.toString();
            } else if (level == Level.WARNING) {
                return ColorCodes.ORANGE.toString();
//            } else if (level == Level.INFO) {
//                return ColorCodes.GREEN;
            } else if (level == Level.CONFIG) {
                return ColorCodes.BLUE.toString();
            } else {
                return ColorCodes.BLACK.toString();
            }
        }
    }
    private static class GUIHandler extends Handler {
        private static ServerManagerGUI serverManagerGUI;


        @Override
        public void publish(LogRecord record) {
            if (serverManagerGUI == null) serverManagerGUI = FantaServer.getInstance().getServerManagerGUi();
            if (serverManagerGUI == null) return;
            if (isLoggable(record)) {
                String message = getFormatter().format(record);
                serverManagerGUI.appendOutput(message);
            }
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }

    private static class FileFormatter extends Formatter {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String timestamp = dateFormat.format(new Date(record.getMillis()));
            String threadName = Thread.currentThread().getName();
            String level = record.getLevel().getName();
            String loggerName = record.getLoggerName();
            String message = formatMessage(record);
            return String.format("[%s] [%s] [%s-%s]: %s%n", timestamp, threadName, loggerName, level, message);
        }
    }
    public static Logger getMainLogger() {
        return mainLogger;
    }
}