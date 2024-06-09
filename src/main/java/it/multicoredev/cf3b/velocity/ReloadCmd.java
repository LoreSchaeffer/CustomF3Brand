package it.multicoredev.cf3b.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import it.multicoredev.mbcore.velocity.Text;

public class ReloadCmd implements SimpleCommand {
    private final CustomF3Brand plugin;

    public ReloadCmd(CustomF3Brand plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        plugin.unload();
        plugin.load();

        Text.get().send("<dark_green>CustomF3Brand Velocity reloaded!</dark_green>", invocation.source());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("cf3.reload");
    }
}
