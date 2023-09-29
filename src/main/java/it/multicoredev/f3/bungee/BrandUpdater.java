package it.multicoredev.f3.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static it.multicoredev.f3.bungee.CustomF3Brand.BRAND;
import static it.multicoredev.f3.bungee.CustomF3Brand.createData;

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
    private final Plugin plugin;
    private final List<String> brand;
    private final long period;
    private int index = 0;
    private ScheduledTask task;
    private String spigotBrand = null;

    public BrandUpdater(Plugin plugin, List<String> brand, long period) {
        this.plugin = plugin;
        this.brand = brand;
        this.period = period;
    }

    public void start() {
        task = ProxyServer.getInstance().getScheduler().schedule(plugin, new UpdateBrandTask(), period, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    public int size() {
        return brand.size();
    }

    public void broadcast() {
        ProxyServer.getInstance().getPlayers().forEach(this::send);
    }

    public void send(ProxiedPlayer player, String spigotBrand) {
        if (player == null) return;
        if (spigotBrand != null) this.spigotBrand = spigotBrand;

        String str = brand.get(index)
                .replace("{server}", getServer(player))
                .replace("{name}", player.getName())
                .replace("{displayname}", player.getDisplayName())
                .replace("{spigot}", spigotBrand != null ? spigotBrand : "");

        player.sendData(BRAND, createData(str));
    }

    private void send(ProxiedPlayer player) {
        send(player, spigotBrand);
    }

    private String getServer(ProxiedPlayer player) {
        if (player.getServer() == null) return "";
        if (player.getServer().getInfo() == null) return "";
        String server = player.getServer().getInfo().getName();
        return server.substring(0, 1).toUpperCase() + server.substring(1);
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
