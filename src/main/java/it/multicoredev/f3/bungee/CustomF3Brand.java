package it.multicoredev.f3.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.multicoredev.mbcore.bungeecord.Chat;
import it.multicoredev.mclib.yaml.Configuration;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
public class CustomF3Brand extends Plugin implements Listener {
    public static final String BRAND = "minecraft:brand";
    private static final int PLUGIN_ID = 13360;
    private final Metrics metrics = new Metrics(this, PLUGIN_ID);
    private final Configuration config = new Configuration(new File(getDataFolder(), "config.yml"), getResourceAsStream("config.yml"));
    private BrandUpdater brandUpdater;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                Chat.severe("&cCannot create " + getDescription().getName() + " folder.");
                onDisable();
                return;
            }
        }

        try {
            config.autoload();
        } catch (IOException e) {
            Chat.severe(e.getMessage());
            onDisable();
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new ReloadCmd(this));
        getProxy().getPluginManager().registerListener(this, this);

        brandUpdater = new BrandUpdater(this, config.getStringList("f3-brand"), config.getLong("update-period"));

        if (brandUpdater.size() > 0) brandUpdater.broadcast();
        if (brandUpdater.size() > 1) brandUpdater.start();
    }

    @Override
    public void onDisable() {
        brandUpdater.stop();
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(BRAND) && event.getReceiver() instanceof ProxiedPlayer) {
            if (event.isCancelled()) return;
            event.setCancelled(true);

            ByteBuf buf = Unpooled.wrappedBuffer(event.getData());
            buf.readByte();
            String spigotBrand = buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8).toString();

            brandUpdater.send((ProxiedPlayer) event.getReceiver(), spigotBrand);
        }
    }

    public static byte[] createData(String str) {
        ByteBuf buf = Unpooled.buffer();
        DefinedPacket.writeString(Chat.getTranslated(str + "&r"), buf);
        return buf.array();
    }
}
