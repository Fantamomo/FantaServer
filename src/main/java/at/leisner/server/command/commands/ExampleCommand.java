package at.leisner.server.command.commands;

import at.leisner.server.FantaServer;
import at.leisner.server.command.Command;

import java.util.List;
import java.util.logging.Level;

public class ExampleCommand extends Command {

    public ExampleCommand() {
        super("example");
    }

    @Override
    public void execute(String s, String[] strings) {
        FantaServer.getInstance().getLogger().lang("Executing example command", Level.SEVERE);
    }

    @Override
    public List<String> tabComplete(String label, String[] args) {
        // Return a list of tab completions
        return List.of("arg1", "arg2", "arg3");
    }
}