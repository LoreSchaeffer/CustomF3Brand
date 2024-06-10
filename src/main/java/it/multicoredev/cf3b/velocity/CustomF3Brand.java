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

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import it.multicoredev.cf3b.Config;
import it.multicoredev.cf3b.Static;
import it.multicoredev.mbcore.velocity.Text;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "customf3brand",
        name = "CustomF3Brand",
        version = "1.2.1",
        description = "Edit your server brand in debug screen",
        authors = {"LoreSchaeffer"},
        url = "https://github.com/LoreSchaeffer/CustomF3Brand"
)
public class CustomF3Brand {
    public static final MinecraftChannelIdentifier BRAND_IDENTIFIER = MinecraftChannelIdentifier.from(Static.BRAND);

    private final ProxyServer proxy;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private Metrics metrics;
    private BrandUpdater brandUpdater;
    private Config config;
    private Path dataDirectory;

    @Inject
    public CustomF3Brand(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
        Text.create(proxy);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        metrics = metricsFactory.make(this, Static.VELOCITY_PLUGIN_ID);

        injectPacketListener();

        load();

        CommandManager commandManager = proxy.getCommandManager();
        CommandMeta cmdMeta = commandManager.metaBuilder("velocityf3reload")
                .aliases("vf3reload", "vf3r")
                .plugin(this)
                .build();
        commandManager.register(cmdMeta, new ReloadCmd(this));

        logger.info("CustomF3Brand has been enabled");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        unload();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        unload();
        load();
    }

    @Subscribe
    @SuppressWarnings("UnstableApiUsage")
    public void onPlayerConnect(ServerPostConnectEvent event) {
        brandUpdater.send(event.getPlayer());
    }

    public void load() {
        try {
            if (!Files.exists(dataDirectory)) Files.createDirectories(dataDirectory);

            File file = new File(dataDirectory.toFile(), "config.json5");
            if (!file.exists() || !file.isFile()) {
                config = new Config(file);
                config.init();
                config.save();
            } else {
                config = Config.load(file);
                if (config.init()) config.save();
            }
        } catch (IOException e) {
            logger.error("Cannot load config file: {}", e.getMessage());

            unload();
            return;
        }

        brandUpdater = new BrandUpdater(this, proxy, config.f3Brand, config.updatePeriod);
    }

    public void unload() {
        if (brandUpdater != null) brandUpdater.stop();
    }

    public Logger logger() {
        return logger;
    }

    public Config config() {
        return config;
    }

    public BrandUpdater brandUpdater() {
        return brandUpdater;
    }

    @SuppressWarnings("deprecation")
    private void injectPacketListener() {
        try {
            Field connectionManagerField = VelocityServer.class.getDeclaredField("cm");
            connectionManagerField.setAccessible(true);

            ConnectionManager connectionManager = (ConnectionManager) connectionManagerField.get(proxy);
            connectionManager.getBackendChannelInitializer().set(new PacketListenerChannelInitializer(
                    (VelocityServer) proxy,
                    connectionManager.getBackendChannelInitializer().get(),
                    this
            ));
        } catch (Throwable t) {
            logger.error("Cannot inject packet listener: {}", t.getMessage());
        }
    }
}
