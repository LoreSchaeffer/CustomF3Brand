/*
 * Copyright 2021 - 2024 Lorenzo Magni
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS”
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package it.multicoredev.cf3b.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.multicoredev.cf3b.Static;
import it.multicoredev.mbcore.bungeecord.Text;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.DefinedPacket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BrandUpdater implements Listener {
    private static final Map<String, String> serversBrand = new ConcurrentHashMap<>();
    private final CustomF3Brand plugin;
    private final List<String> brand;
    private int index = 0;
    private ScheduledTask task;

    public BrandUpdater(CustomF3Brand plugin, List<String> brand, long period) {
        this.plugin = plugin;
        this.brand = brand;

        if (brand.size() > 1) task = ProxyServer.getInstance().getScheduler().schedule(plugin, new UpdateBrandTask(), period, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void broadcast() {
        ProxyServer.getInstance().getPlayers().forEach(this::send);
    }

    public void send(ProxiedPlayer player) {
        if (player == null) return;

        String server = getServer(player);

        String str = brand.get(index)
                .replace("{server}", server != null ? server : "")
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName())
                .replace("{spigot}", Text.toMiniMessage(server != null && serversBrand.containsKey(server) ? serversBrand.get(server) : ""));

        player.sendData(Static.BRAND, createData(str));
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(Static.BRAND) && event.getReceiver() instanceof ProxiedPlayer player) {
            if (event.isCancelled()) return;
            event.setCancelled(true);

            ByteBuf buf = Unpooled.wrappedBuffer(event.getData());
            String server = getServer(player);
            if (server != null) serversBrand.put(server, DefinedPacket.readString(buf));
            buf.release();

            send(player);
        }
    }

    public static byte[] createData(String str) {
        ByteBuf brand = Unpooled.buffer();
        DefinedPacket.writeString(Text.toLegacyAlternateColorCodes(Text.toLegacyText(str)) + "§r", brand);
        byte[] data = DefinedPacket.toArray(brand);
        brand.release();

        return data;
    }

    private String getServer(ProxiedPlayer player) {
        if (player.getServer() == null) return null;
        if (player.getServer().getInfo() == null) return null;

        ServerInfo server = player.getServer().getInfo();
        if (server == null) return null;

        String name = server.getName();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private class UpdateBrandTask implements Runnable {
        @Override
        public void run() {
            broadcast();
            ++index;
            if (index >= brand.size()) index = 0;
        }
    }
}
