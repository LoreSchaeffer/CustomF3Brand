package it.multicoredev.cf3b.velocity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * BSD 3-Clause License
 * <p>
 * Copyright (c) 2021 - 2024, Lorenzo Magni
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class BrandUpdater {
    private final CustomF3Brand plugin;
    private final ProxyServer proxy;
    private final List<String> brand;
    private final long period;
    private int index = 0;
    private ScheduledTask task;
    private String spigotBrand = null;

    public BrandUpdater(CustomF3Brand plugin, ProxyServer proxy, List<String> brand, long period) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.brand = brand;
        this.period = period;
    }

    public void start() {
        task = proxy.getScheduler().buildTask(plugin, new UpdateBrandTask())
                .repeat(period, TimeUnit.MILLISECONDS)
                .schedule();
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    public int size() {
        return brand.size();
    }

    public void broadcast() {
        proxy.getAllPlayers()
                .parallelStream()
                .forEach(this::send);
    }

    public void send(Player player, String spigotBrand) {
        if (player == null) return;
        if (spigotBrand != null) this.spigotBrand = spigotBrand;

        String str = brand.get(index)
                .replace("{server}", getServer(player))
                .replace("{name}", player.getUsername())
                .replace("{displayname}", player.getUsername())
                .replace("{spigot}", spigotBrand != null ? Text.toMiniMessage(spigotBrand) : "");

        try {
            MinecraftConnection connection = ((ConnectedPlayer) player).getConnection();
            if (connection.getState() != StateRegistry.PLAY) return;

            ProtocolVersion protocol = player.getProtocolVersion();
            if (protocol.compareTo(ProtocolVersion.MINECRAFT_1_13) < 0) return;


            ByteBuf buf = Unpooled.buffer();
            ProtocolUtils.writeString(buf, Text.toLegacyAlternateColorCodes(Text.toLegacyText(Text.toMiniMessage(str))) + "§r");
            connection.write(new PluginMessagePacket(CustomF3Brand.BRAND.getId(), buf));
        } catch (Throwable ignored) {
        }
    }

    public void updateSpigotBrand(String spigotBrand) {
        String oldSpigotBrand = this.spigotBrand;
        this.spigotBrand = spigotBrand;

        if (!Objects.equals(oldSpigotBrand, spigotBrand)) broadcast();
    }

    private void send(Player player) {
        send(player, spigotBrand);
    }

    private String getServer(Player player) {
        if (player.getCurrentServer().isPresent()) return player.getCurrentServer().get().getServerInfo().getName();
        return "";
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
