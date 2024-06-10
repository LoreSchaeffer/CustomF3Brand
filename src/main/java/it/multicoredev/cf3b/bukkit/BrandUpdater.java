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

package it.multicoredev.cf3b.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.multicoredev.cf3b.Static;
import it.multicoredev.mbcore.spigot.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BrandUpdater {
    private final CustomF3Brand plugin;
    private final List<String> brand;
    private final Class<?> pdscl;
    private final ProtocolManager manager;
    private int index = 0;
    private ScheduledFuture<?> task;
    private boolean packetDataSerializerError = false;
    private boolean writeStringError = false;

    public BrandUpdater(CustomF3Brand plugin, List<String> brand, long period) throws ClassNotFoundException {
        this.plugin = plugin;
        this.brand = brand;
        this.manager = ProtocolLibrary.getProtocolManager();

        this.pdscl = Class.forName("net.minecraft.network.PacketDataSerializer");

        if (brand.size() > 1) task = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new UpdateBrandTask(), period, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    public void send(Player player) {
        String str = brand.get(index)
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName());
        if (CustomF3Brand.PAPI) str = PlaceholderUtils.replacePlaceholders(str, player);

        ByteBuf buf = getPacketDataSerializer();
        if (buf == null) return;

        if (!writeString(buf, Static.BRAND)) return;
        if (!writeString(buf, Text.toLegacyAlternateColorCodes(Text.toLegacyText(Text.toMiniMessage(str))) + "§r")) return;

        byte[] data = new byte[buf.readableBytes()];
        for (int i = 0; i < data.length; i++) data[i] = buf.getByte(i);

        try {
            WirePacket customPacket = new WirePacket(PacketType.Play.Server.CUSTOM_PAYLOAD, data);
            manager.sendWirePacket(player, customPacket);
        } catch (Throwable ignored) {
        }
    }

    private ByteBuf getPacketDataSerializer() {
        try {
            Constructor<?> pdsco = pdscl.getConstructor(ByteBuf.class);
            return (ByteBuf) pdsco.newInstance(Unpooled.buffer());
        } catch (Throwable t) {
            if (!packetDataSerializerError) {
                packetDataSerializerError = true;
                Text.get().send("<red>Cannot create PacketDataSerializer ByteBuf: " + t.getMessage() + "</red>", plugin.getServer().getConsoleSender());
                if (plugin.config().debug) t.printStackTrace();
            }

            return null;
        }
    }

    private boolean writeString(Object buf, String data) {
        try {
            Method writeString = pdscl.getDeclaredMethod("a", String.class);
            writeString.invoke(buf, data);
            return true;
        } catch (Throwable t) {
            if (!writeStringError) {
                writeStringError = true;
                Text.get().send("<red>Cannot write string to PacketDataSerializer: " + t.getMessage() + "</red>", plugin.getServer().getConsoleSender());
                if (plugin.config().debug) t.printStackTrace();
            }

            return false;
        }
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
