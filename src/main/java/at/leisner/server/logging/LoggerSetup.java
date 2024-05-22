package at.leisner.server.logging;


import at.leisner.server.util.ColorCodes;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LoggerSetup {
    private static final String LOG_FOLDER = "./logs";
    private static final String LOG_FILE = LOG_FOLDER + "/server.log";
    private static FileHandler fileHandler;
    private static Logger mainLogger;

    static {
        try {
            // Ensure the log directory exists
            Files.createDirectories(Paths.get(LOG_FOLDER));

            // Set up the file handler to write to a common log file
            fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new FileFormatter());

            // Add the file handler to the root logger
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();

            // Remove all existing handlers to prevent duplicate logging
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            rootLogger.setUseParentHandlers(false);
            rootLogger.addHandler(fileHandler);

            // Also add a console handler with color support
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new ConsoleFormatter());
            rootLogger.addHandler(consoleHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger createLogger(String name) {
        Logger logger = Logger.getLogger(name);
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
            String level = record.getLevel().getName();
            String message = formatMessage(record);
            String color = getColor(record.getLevel());
            return String.format("%s[%s] [%s] [%s]: %s%s%n", color, timestamp, threadName, level, message, ColorCodes.RESET);
        }

        private String getColor(Level level) {
            if (level == Level.SEVERE) {
                return ColorCodes.RED;
            } else if (level == Level.WARNING) {
                return ColorCodes.YELLOW;
//            } else if (level == Level.INFO) {
//                return ColorCodes.GREEN;
            } else if (level == Level.CONFIG) {
                return ColorCodes.BLUE;
            } else {
                return ColorCodes.WHITE;
            }
        }
    }

    private static class FileFormatter extends Formatter {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            String timestamp = dateFormat.format(new Date(record.getMillis()));
            String threadName = Thread.currentThread().getName();
            String level = record.getLevel().getName();
            String message = formatMessage(record);
            return String.format("[%s] [%s] [%s]: %s%n", timestamp, threadName, level, message);
        }
    }
    public static Logger getMainLogger() {
        return mainLogger;
    }
}