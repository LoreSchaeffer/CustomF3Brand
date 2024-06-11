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

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.BackendChannelInitializer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.PluginMessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.Method;
import java.util.List;

public class PacketListenerChannelInitializer extends BackendChannelInitializer {
    private final ChannelInitializer<Channel> channelInitializer;
    private final CustomF3Brand plugin;
    private Method initMethod;

    public PacketListenerChannelInitializer(VelocityServer server, ChannelInitializer<Channel> channelInitializer, CustomF3Brand plugin) {
        super(server);

        this.channelInitializer = channelInitializer;
        this.plugin = plugin;

        if (channelInitializer != null) {
            try {
                initMethod = channelInitializer.getClass().getDeclaredMethod("initChannel", Channel.class);
                initMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                plugin.logger().error("Cannot find initChannel method in {}", channelInitializer.getClass().getName());
            }
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        try {
            if (channelInitializer != null && initMethod != null) initMethod.invoke(channelInitializer, channel);
        } catch (Exception e) {
            plugin.logger().error("Cannot invoke initChannel method: {}", e.getMessage());
        } finally {
            if (!channel.pipeline().toMap().containsKey("frame-decoder")) super.initChannel(channel);
            channel.pipeline().addLast("brand_packet_listener", new BrandPacketListener());
        }
    }

    private class BrandPacketListener extends MessageToMessageDecoder<MinecraftPacket> {

        @Override
        protected void decode(ChannelHandlerContext ctx, MinecraftPacket minecraftPacket, List<Object> out) {
            if (minecraftPacket instanceof PluginMessagePacket packet) {
                if (packet.getChannel().equals("minecraft:brand")) {
                    ByteBuf buf = packet.content();
                    String brand = readString(buf);

                    plugin.brandUpdater().updateSpigotBrand(getServerPort(ctx.channel().remoteAddress().toString()), brand);
                    return;
                }
            }

            ReferenceCountUtil.retain(minecraftPacket);
            if (minecraftPacket != null) out.add(minecraftPacket);
        }

        private Integer getServerPort(String address) {
            if (!address.contains(":")) return null;

            try {
                return Integer.parseInt(address.split(":")[1]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        private String readString(ByteBuf buf) {
            buf.skipBytes(1);
            int len = buf.readableBytes();
            byte[] bytes = new byte[len];
            buf.readBytes(bytes);
            return new String(bytes);
        }
    }
}
