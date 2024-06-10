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

package it.multicoredev.cf3b.velocity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.multicoredev.mbcore.velocity.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BrandUpdater {
    private static final Map<String, String> serversBrand = new ConcurrentHashMap<>();
    private final CustomF3Brand plugin;
    private final ProxyServer proxy;
    private final List<String> brand;
    private int index = 0;
    private ScheduledTask task;
    private boolean protocolError = false;

    public BrandUpdater(CustomF3Brand plugin, ProxyServer proxy, List<String> brand, long period) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.brand = brand;

        if (brand.size() > 1) task = proxy.getScheduler().buildTask(plugin, new UpdateBrandTask())
                .repeat(period, TimeUnit.MILLISECONDS)
                .schedule();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void broadcast() {
        proxy.getAllPlayers()
                .parallelStream()
                .forEach(this::send);
    }

    public void send(Player player) {
        if (player == null) return;

        String server = getServer(player);

        String str = brand.get(index)
                .replace("{server}", server != null ? server : "")
                .replace("{name}", player.getUsername())
                .replace("{displayname}", player.getUsername())
                .replace("{spigot}", Text.toMiniMessage(server != null && serversBrand.containsKey(server) ? serversBrand.get(server) : ""));

        try {
            MinecraftConnection connection = ((ConnectedPlayer) player).getConnection();
            if (connection.getState() != StateRegistry.PLAY) return;

            ProtocolVersion protocol = player.getProtocolVersion();
            if (protocol.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) {
                if (!protocolError) {
                    plugin.logger().warn("Protocol version {} is not supported", protocol);
                    protocolError = true;
                }

                return;
            }


            ByteBuf buf = Unpooled.buffer();
            ProtocolUtils.writeString(buf, Text.toLegacyAlternateColorCodes(Text.toLegacyText(Text.toMiniMessage(str))) + "§r");
            connection.write(new PluginMessagePacket(CustomF3Brand.BRAND_IDENTIFIER.getId(), buf));
            connection.flush();
        } catch (Throwable t) {
            if (plugin.config().debug) t.printStackTrace();
        }
    }

    public void updateSpigotBrand(Integer port, String spigotBrand) {
        if (port == null) return;

        RegisteredServer server = proxy.getAllServers().parallelStream().filter(s -> s.getServerInfo().getAddress().getPort() == port).findFirst().orElse(null);
        if (server == null) return;

        String serverName = getServerName(server.getServerInfo());
        if (serverName != null) serversBrand.put(serverName, spigotBrand);

        server.getPlayersConnected()
                .parallelStream()
                .forEach(this::send);
    }

    private String getServer(Player player) {
        Optional<ServerConnection> server = player.getCurrentServer();
        return server.map(serverConnection -> getServerName(serverConnection.getServerInfo())).orElse(null);

    }

    private String getServerName(ServerInfo serverInfo) {
        if (serverInfo == null) return null;

        String name = serverInfo.getName();
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
