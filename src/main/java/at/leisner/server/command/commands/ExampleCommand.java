package at.leisner.server.command.commands;

import at.leisner.server.command.Command;

import java.util.List;

public class ExampleCommand extends Command {

    public ExampleCommand() {
        super("example");
    }

    @Override
    public void execute(String[] args) {
        System.out.println("Executing example command");
    }

    @Override
    public List<String> tabComplete(String[] args) {
        // Return a list of tab completions
        return List.of("arg1", "arg2", "arg3");
    }
}