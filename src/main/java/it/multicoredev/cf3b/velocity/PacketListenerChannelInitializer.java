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
                plugin.getLogger().error("Cannot find initChannel method in " + channelInitializer.getClass().getName());
            }
        }
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        try {
            if (channelInitializer != null && initMethod != null) initMethod.invoke(channelInitializer, channel);
        } catch (Exception e) {
            plugin.getLogger().error("Cannot invoke initChannel method: " + e.getMessage());
        } finally {
            if (!channel.pipeline().toMap().containsKey("frame-decoder")) super.initChannel(channel);
            channel.pipeline().addLast("brand_packet_listener", new BrandPacketListener());
        }
    }

    private class BrandPacketListener extends MessageToMessageDecoder<MinecraftPacket> {

        @Override
        protected void decode(ChannelHandlerContext ctx, MinecraftPacket minecraftPacket, List<Object> out) {
            if (minecraftPacket instanceof PluginMessagePacket packet) {
                ByteBuf buf = packet.content();
                String brand = readString(buf);

                plugin.getBrandUpdater().updateSpigotBrand(brand);
                return;
            }

            ReferenceCountUtil.retain(minecraftPacket);
            if (minecraftPacket != null) out.add(minecraftPacket);
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
