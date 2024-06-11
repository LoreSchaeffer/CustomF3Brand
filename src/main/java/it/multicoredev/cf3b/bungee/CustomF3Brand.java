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

import it.multicoredev.cf3b.Config;
import it.multicoredev.cf3b.Static;
import it.multicoredev.mbcore.bungeecord.Text;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;

public class CustomF3Brand extends Plugin implements Listener {
    private final Metrics metrics = new Metrics(this, Static.BUNGEECORD_PLUGIN_ID);
    private Config config;
    private BrandUpdater brandUpdater;

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

        brandUpdater = new BrandUpdater(this, config.f3Brand, config.updatePeriod);

        getProxy().getPluginManager().registerCommand(this, new ReloadCmd(this));
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerListener(this, brandUpdater);
    }

    @Override
    public void onDisable() {
        if (brandUpdater != null) brandUpdater.stop();
        getProxy().getPluginManager().unregisterListeners(this);
        Text.destroy();
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        brandUpdater.send(event.getPlayer());
    }

    public Config config() {
        return config;
    }

    public BrandUpdater brandUpdater() {
        return brandUpdater;
    }
}
