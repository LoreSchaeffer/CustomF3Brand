package it.multicoredev.cf3b.bukkit.test;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import it.multicoredev.cf3b.bukkit.CustomF3Brand;
import it.multicoredev.mbcore.spigot.Text;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginTest implements PluginMessageListener, Listener {
    private final CustomF3Brand plugin;
    private final Text text;
    private final ConsoleCommandSender console;
    private final List<String> servers = new ArrayList<>();

    public PluginTest(CustomF3Brand plugin) {
        this.plugin = plugin;
        this.text = Text.get();
        console = plugin.getServer().getConsoleSender();
    }

    public void init() {
        try {
            text.send("<rainbow>Running CustomF3Brand in debug mode.", console);

            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
            plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);

            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void disable() {
        try {
            plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, "BungeeCord");
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, "BungeeCord", this);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);

            text.send(String.format("<aqua>Received message from channel <yellow>%s</yellow> through player <yellow>%s</yellow>.</aqua>", channel, player.getName()), console);

            if (channel.equals("BungeeCord")) {
                String subChannel = in.readUTF();

                switch (subChannel) {
                    case "IP":
                        text.send(String.format("Sub-channel: <yellow>%s</yellow>. IP: <yellow>%s</yellow>. Port: <yellow>%d</yellow>.", subChannel, in.readUTF(), in.readInt()), console);
                        break;
                    case "IPOther":
                        text.send(String.format("Sub-channel: <yellow>%s</yellow>. Player: <yellow>%s</yellow>. IP: <yellow>%s</yellow>. Port: <yellow>%d</yellow>.", subChannel, in.readUTF(), in.readUTF(), in.readInt()), console);
                        break;
                    case "PlayerCount":
                        text.send(String.format("Sub-channel: <yellow>%s</yellow>. Server: <yellow>%s</yellow>. Player count: <yellow>%d</yellow>.", subChannel, in.readUTF(), in.readInt()), console);
                        break;
                    case "PlayerList":
                        text.send(String.format("Sub-channel: <yellow>%s</yellow>. Server: <yellow>%s</yellow>. Players: <yellow>%s</yellow>.", subChannel, in.readUTF(), in.readUTF()), console);
                        break;
                    case "GetServers":
                        String servers = in.readUTF();
                        this.servers.clear();
                        this.servers.addAll(Arrays.asList(servers.split(", ")));

                        text.send(String.format("Sub-channel: <yellow>%s</yellow>. Servers: <yellow>%s</yellow>.", subChannel, servers), console);
                        break;
                    case "GetServer":
                        text.send(String.format("Sub-channel: <yellow>%s</yellow>. Server: <yellow>%s</yellow>.", subChannel, in.readUTF()), console);
                        break;
                    case "Forward":
                    case "ForwardToPlayer":
                        String subCh = in.readUTF();
                        short len = in.readShort();
                        byte[] bytes = new byte[len];
                        in.readFully(bytes);
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

                        text.send(String.format("Sub-channel: <yellow>%s</yellow>. Forward-channel: <yellow>%s</yellow>. Length: <yellow>%d</yellow>. Stream: <yellow>%s</yellow>.", subChannel, subCh, len, dis), console);
                        break;
                }
            } else {
                text.send(String.format("<aqua>Received message from channel <yellow>%s</yellow>.</aqua>", channel), console);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        try {
            Player player = event.getPlayer();
            if (!player.hasPermission("cf3.debug")) return;

            String msg = event.getMessage();
            if (!msg.startsWith("!debug")) return;
            event.setCancelled(true);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("GetServer");
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

            out = ByteStreams.newDataOutput();
            out.writeUTF("IP");
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

            out = ByteStreams.newDataOutput();
            out.writeUTF("IPOther");
            out.writeUTF(player.getName());
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

            out = ByteStreams.newDataOutput();
            out.writeUTF("GetServers");
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

            out = ByteStreams.newDataOutput();
            out.writeUTF("PlayerCount");
            out.writeUTF(servers.get(0));
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

            out = ByteStreams.newDataOutput();
            out.writeUTF("PlayerList");
            out.writeUTF(servers.get(0));
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

            out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("Test");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            try {
                dos.writeUTF("Test message");
                dos.writeInt(1234);
            } catch (IOException e) {
                e.printStackTrace();
            }

            out.writeShort(baos.toByteArray().length);
            out.write(baos.toByteArray());
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

            text.send("<aqua>Test completed. Check console for results.</aqua>", player);
        } catch (Throwable t) {
            t.printStackTrace();
            text.send("<red>Test failed. Check console for errors.</red>", event.getPlayer());
        }
    }
}
