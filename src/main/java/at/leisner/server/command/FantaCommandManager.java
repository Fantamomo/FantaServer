package at.leisner.server.command;

import at.leisner.server.FantaServer;
import at.leisner.server.plugin.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class FantaCommandManager implements CommandManager {
    private final FantaServer server;
    private Set<CommandInfo> commands = new HashSet<>();
    public FantaCommandManager(FantaServer server) {
        this.server = server;
    }

    @Override
    public void registerCommand(JavaPlugin javaPlugin, Command command) {
        registerCommand(javaPlugin, command.getName(), command);
    }

    @Override
    public void registerCommand(JavaPlugin javaPlugin, String label, Command command) {
        if (javaPlugin == null || command == null) return;
        commands.add(new CommandInfo(command, javaPlugin));
    }
    public void registerGlobalCommand(Command command) {
        commands.add(new CommandInfo(command, command.getName(), null));
    }

    @Override
    public void unregisterCommand(JavaPlugin javaPlugin, String commandName) {
        if (javaPlugin == null || commandName == null) return;
        try {
            CommandInfo commandInfo = getCommandInfo(commandName);
            if (commandInfo.getJavaPlugin() == javaPlugin) {
                commands.remove(commandInfo);
            }
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public Command getCommand(String s) {
        for (CommandInfo command : commands) {
            if (command.getCommand().getName().equals(s)) return command.getCommand();
        }
        return null;
    }
    public CommandInfo getCommandInfo(String command) {
        for (CommandInfo commandInfo : commands) {
            if (commandInfo.getCommand().getName().equals(command)) return commandInfo;
        }
        return null;
    }
    @Override
    public boolean isCommandRegistered(Command command) {
        return getCommandInfo(command.getName()) != null;
    }
    @Override
    public boolean isCommandRegistered(String command) {
        return getCommand(command) != null;
    }
    public List<String> getTabCompletionSuggestions(String label, String[] args) {
        Command command = getCommand(label);
        if (command == null) return new ArrayList<>();
        return command.tabComplete(label, args);
    }
    public List<String> getCommandNameList() {
        return commands.stream().map((command) -> command.getCommand().getName()).toList();
    }
    public void executeCommand(String input) {
        String[] temp = input.split("\\s+", 2);
        String label = temp[0];
        String[] args = temp.length > 1 ? temp[1].split("\\s+") : new String[0];
        this.executeCommand(label, args);
    }
    public void executeCommand(String label, String[] args) {
        Command command = getCommand(label);
        if (command == null) {
            server.getLogger().lang("Command does not exist!", Level.INFO);
            return;
        }
        command.execute(label, args);
    }
}
