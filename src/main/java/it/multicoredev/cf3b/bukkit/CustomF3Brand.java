package it.multicoredev.cf3b.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import it.multicoredev.cf3b.Config;
import it.multicoredev.cf3b.bukkit.test.PluginTest;
import it.multicoredev.mbcore.spigot.Text;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

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
public class CustomF3Brand extends JavaPlugin {
    public static final String BRAND = "minecraft:brand";
    private static final int PLUGIN_ID = 13359;
    private final Metrics metrics = new Metrics(this, PLUGIN_ID);
    private Config config;
    public static boolean PAPI;
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

        PAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (PAPI) Text.get().send("<dark_green>PlaceholderAPI found.", getServer().getConsoleSender());
        else Text.get().send("<yellow>PlaceholderAPI not found.", getServer().getConsoleSender());

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        try {
            brandUpdater = new BrandUpdater(
                    config.f3Brand,
                    config.updatePeriod,
                    manager);
        } catch (ClassNotFoundException e) {
            getLogger().severe(e.getMessage());
            onDisable();
            return;
        }

        manager.addPacketListener(new PacketListener(this, brandUpdater));

        getCommand("f3reload").setExecutor(new ReloadCmd(this));

        if (brandUpdater.size() > 0) brandUpdater.broadcast();
        if (brandUpdater.size() > 1) brandUpdater.start();

        if (config.debug) {
            new PluginTest(this).init();
        }
    }

    @Override
    public void onDisable() {
        brandUpdater.stop();
        Text.destroy();
    }
}
