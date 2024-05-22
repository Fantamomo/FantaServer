package at.leisner.server.console;

import at.leisner.server.command.Command;
import at.leisner.server.command.FantaCommandManager;

import java.util.Scanner;
import java.util.List;

public class Console {
    private final FantaCommandManager commandManager;

    public Console(FantaCommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            if (!input.isEmpty()) {
                String[] args = input.split("\\s+");
                String commandName = args[0];
                Command command = commandManager.getCommand(commandName);
                if (command != null) {
                    command.execute(args.length > 1 ? args : new String[0]);
                } else {
                    System.out.println("Unknown command: " + commandName);
                }
            }
        }
    }

    public List<String> tabComplete(String input) {
        String[] args = input.split("\\s+");
        if (args.length == 1) {
            String commandNamePrefix = args[0];
            // Collect possible commands that start with commandNamePrefix
            // For simplicity, returning an empty list here
        } else {
            // Call tabComplete on the command if available
            Command command = commandManager.getCommand(args[0]);
            if (command != null) {
                return command.tabComplete(args);
            }
        }
        return null;
    }
}
