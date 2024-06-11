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

import it.multicoredev.cf3b.Config;
import it.multicoredev.cf3b.Static;
import it.multicoredev.mbcore.spigot.Text;
import org.bstats.bukkit.Metrics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CustomF3Brand extends JavaPlugin implements Listener {
    private final Metrics metrics = new Metrics(this, Static.SPIGOT_PLUGIN_ID);
    private Config config;
    private BrandUpdater brandUpdater;
    public static boolean PAPI;

    @Override
    public void onEnable() {
        Text.create(this);

        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdirs()) {
                getLogger().severe(String.format("Cannot create %s folder.", getDescription().getName()));

                onDisable();
                return;
            }
        }

        try {
            File file = new File(getDataFolder(), "config.json5");
            if (!file.exists() || !file.isFile()) {
                config = new Config(file);
                config.init();
                config.save();
            } else {
                config = Config.load(file);
                if (config.init()) config.save();
            }
        } catch (IOException e) {
            getLogger().severe("Cannot load config file: " + e.getMessage());

            onDisable();
            return;
        }

        PAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (PAPI) Text.get().send("<dark_green>PlaceholderAPI support enabled.", getServer().getConsoleSender());

        try {
            brandUpdater = new BrandUpdater(this, config.f3Brand, config.updatePeriod);
        } catch (ClassNotFoundException e) {
            getLogger().severe(e.getMessage());
            if (config.debug) e.printStackTrace();

            onDisable();
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("f3reload")).setExecutor(new ReloadCmd(this));
    }

    @Override
    public void onDisable() {
        if (brandUpdater != null) brandUpdater.stop();
        HandlerList.unregisterAll((Plugin) this);
        Text.destroy();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        brandUpdater.send(event.getPlayer());
    }

    public Config config() {
        return config;
    }

    public BrandUpdater brandUpdater() {
        return brandUpdater;
    }
}
