package at.leisner.server.command.commands;

import at.leisner.server.FantaServer;
import at.leisner.server.command.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class HelpCommand extends Command {
    private final FantaServer server;
    public HelpCommand(FantaServer server) {
        super("help");
        this.server = server;
    }

    @Override
    public void execute(String label, String[] args) {
        if (args.length > 0) {
            String commandName = args[0];
            Command command = server.getCommandManager().getCommand(commandName);
            if (command == null) {
                server.getLogger().lang("command.help.command_not_exist", Level.WARNING, commandName);
                return;
            }
            if (command.getHelp() == null) {
                server.getLogger().lang("command.help.not_help", Level.INFO, commandName);
                return;
            }
            server.getLogger().lang("command.help.help_for_command", Level.INFO, commandName, command.getHelp());
        }
    }

    @Override
    public List<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return server.getCommandManager().getCommandNameList();
        }
        return new ArrayList<>();
    }

    @Override
    public String getHelp() {
        return server.getLanguage().get("command.stop.help");
    }
}
