package at.leisner.server.command.commands;

import at.leisner.server.FantaServer;
import at.leisner.server.command.Command;

import java.util.List;

public class PluginCommand extends Command {
    private final FantaServer server;
    public PluginCommand(FantaServer server) {
        super("plugin");
        this.server = server;
    }

    @Override
    public void execute(String s, String[] strings) {

    }

    @Override
    public List<String> tabComplete(String label, String[] args) {
        if (args.length == 0);
        return null;
    }
}
