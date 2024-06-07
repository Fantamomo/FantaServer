package at.leisner.server.command;

import at.leisner.server.plugin.JavaPlugin;
import at.leisner.server.util.Util;

public class CommandInfo {
    private final Command command;
    private final JavaPlugin javaPlugin;
    private final String label;

    public CommandInfo(Command command, JavaPlugin javaPlugin) {
        this.command = command;
        this.javaPlugin = javaPlugin;
        label = command.getName();
        if (javaPlugin != null) {
            Util.updatePrivateVariable(command, "javaPlugin", javaPlugin);
        }
    }
    public CommandInfo(Command command, String label, JavaPlugin javaPlugin) {
        this.command = command;
        this.javaPlugin = javaPlugin;
        this.label = label;
    }

    public Command getCommand() {
        return command;
    }

    public JavaPlugin getJavaPlugin() {
        return javaPlugin;
    }

    public String getLabel() {
        return label;
    }
}
