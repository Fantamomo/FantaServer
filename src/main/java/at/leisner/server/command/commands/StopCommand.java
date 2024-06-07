package at.leisner.server.command.commands;

import at.leisner.server.FantaServer;
import at.leisner.server.command.Command;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class StopCommand extends Command {
    private FantaServer server;
    public StopCommand(FantaServer server) {
        super("stop");
        this.server = server;
    }

    @Override
    public void execute(String label, String[] args) {
        if (args.length == 1 && args[0].equals("confirm")) {
            server.getLogger().lang("command.stop.successes", Level.INFO);
            server.shutdown();
            return;
        }
        server.getLogger().lang("command.stop.need_confirm", Level.WARNING);
    }

    @Override
    public List<String> tabComplete(String label, String[] args) {
        if (args.length == 1) return List.of("confirm");
        return new ArrayList<>();
    }

    @Override
    public String getHelp() {
        return server.getLanguage().get("command.stop.help");
    }
}
