package it.multicoredev.cf3b.bukkit.test;

import it.multicoredev.cf3b.bukkit.CustomF3Brand;
import it.multicoredev.mbcore.spigot.Text;
import org.bukkit.command.ConsoleCommandSender;

public class PluginTest {
    private final CustomF3Brand plugin;
    private final Text text;
    private final ConsoleCommandSender console;

    public PluginTest(CustomF3Brand plugin) {
        this.plugin = plugin;
        this.text = Text.get();
        console = plugin.getServer().getConsoleSender();
    }

    public void init() {
        text.send("<rainbow>Running CustomF3Brand in debug mode.", console);
    }
}
