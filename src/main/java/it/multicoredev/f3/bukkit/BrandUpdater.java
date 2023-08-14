package it.multicoredev.f3.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.multicoredev.mbcore.spigot.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Copyright © 2021 by Lorenzo Magni
 * This file is part of CustomF3Brand.
 * CustomF3Brand is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class BrandUpdater {
    private final List<String> brand;
    private final long period;
    private final Class<?> pdscl;
    private final ProtocolManager manager;
    private int index = 0;
    private ScheduledFuture<?> task;

    public BrandUpdater(List<String> brand, long period, ProtocolManager manager) throws ClassNotFoundException {
        this.brand = brand;
        this.period = period;
        this.manager = manager;

        this.pdscl = Class.forName("net.minecraft.network.PacketDataSerializer");
    }

    public void start() {
        task = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new UpdateBrandTask(), period, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(true);
    }

    public int size() {
        return brand.size();
    }

    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    public void send(Player player) {
        String str = brand.get(index)
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName());
        if (CustomF3Brand.PAPI) str = PlaceholderUtils.replacePlaceholders(str, player);

        ByteBuf pds = (ByteBuf) getPacketDataSerializer();
        if (pds == null) return;
        writeString(pds, CustomF3Brand.BRAND);
        writeString(pds, Chat.getTranslated(str + "&r"));

        byte[] data = new byte[pds.readableBytes()];
        for (int i = 0; i < data.length; i++) data[i] = pds.getByte(i);

        WirePacket customPacket = new WirePacket(PacketType.Play.Server.CUSTOM_PAYLOAD, data);

        manager.sendWirePacket(player, customPacket);
    }

    private Object getPacketDataSerializer() {
        try {
            Constructor<?> pdsco = pdscl.getConstructor(ByteBuf.class);
            return pdsco.newInstance(Unpooled.buffer());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    private void writeString(Object buf, String data) {
        try {
            Method writeString = pdscl.getDeclaredMethod("a", String.class);
            writeString.invoke(buf, data);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
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
