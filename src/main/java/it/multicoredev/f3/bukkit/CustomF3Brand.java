package it.multicoredev.f3.bukkit;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import it.multicoredev.mbcore.spigot.Chat;
import it.multicoredev.mclib.yaml.Configuration;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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
public class CustomF3Brand extends JavaPlugin {
    public static final String BRAND = "minecraft:brand";
    private static final int PLUGIN_ID = 13359;
    private final Metrics metrics = new Metrics(this, PLUGIN_ID);
    private final Configuration config = new Configuration(new File(getDataFolder(), "config.yml"), getResource("config.yml"));
    public static boolean PAPI;
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

        PAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (PAPI) Chat.info("&aPlaceholderAPI found.");
        else Chat.info("&ePlaceholderAPI not found.");

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        try {
            brandUpdater = new BrandUpdater(
                    config.getStringList("f3-brand"),
                    config.getLong("update-period"),
                    manager);
        } catch (ClassNotFoundException e) {
            Chat.severe(e.getMessage());
            onDisable();
            return;
        }

        manager.addPacketListener(new PacketListener(this, brandUpdater));

        getCommand("f3reload").setExecutor(new ReloadCmd(this));

        if (brandUpdater.size() > 0) brandUpdater.broadcast();
        if (brandUpdater.size() > 1) brandUpdater.start();
    }

    @Override
    public void onDisable() {
        brandUpdater.stop();
    }
}
