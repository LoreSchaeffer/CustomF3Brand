package it.multicoredev.cf3b.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.multicoredev.cf3b.Config;
import it.multicoredev.mbcore.bungeecord.Text;
import it.multicoredev.mclib.json.GsonHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;

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
public class CustomF3Brand extends Plugin implements Listener {
    public static final String BRAND = "minecraft:brand";
    private static final int PLUGIN_ID = 13360;
    private static final GsonHelper GSON = new GsonHelper();
    private final Metrics metrics = new Metrics(this, PLUGIN_ID);
    private Config config;
    private BrandUpdater brandUpdater;

    @Override
    public void onEnable() {
        Text.create(this);

        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                getLogger().severe(ChatColor.RED + String.format("Cannot create %s folder.", getDescription().getName()));
                onDisable();
                return;
            }
        }

        try {
            File configFile = new File(getDataFolder(), "config.json");
            config = GSON.autoload(configFile, new Config().init(), Config.class);
        } catch (IOException e) {
            getLogger().severe(ChatColor.RED + "Cannot load config file: " + e.getMessage());
            onDisable();
            return;
        }

        getProxy().getPluginManager().registerCommand(this, new ReloadCmd(this));
        getProxy().getPluginManager().registerListener(this, this);

        brandUpdater = new BrandUpdater(this, config.f3Brand, config.updatePeriod);

        if (brandUpdater.size() > 0) brandUpdater.broadcast();
        if (brandUpdater.size() > 1) brandUpdater.start();
    }

    @Override
    public void onDisable() {
        brandUpdater.stop();
        Text.destroy();
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(BRAND) && event.getReceiver() instanceof ProxiedPlayer) {
            if (event.isCancelled()) return;
            event.setCancelled(true);

            ByteBuf buf = Unpooled.wrappedBuffer(event.getData());
            String brand = DefinedPacket.readString(buf);
            buf.release();

            brandUpdater.send((ProxiedPlayer) event.getReceiver(), brand);
        }
    }

    public static byte[] createData(String str) {
        ByteBuf brand = Unpooled.buffer();
        DefinedPacket.writeString(Text.toLegacyAlternateColorCodes(str) + "§r", brand);
        byte[] data = DefinedPacket.toArray(brand);
        brand.release();

        return data;
    }
}
