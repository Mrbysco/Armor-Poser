package com.mrbysco.armorposer.handlers;

import com.mrbysco.armorposer.data.SwapData;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class SpigotSwapHandler implements PluginMessageListener {
    
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        final SwapData swapData = SwapData.decode(new FriendlyByteBuf(Unpooled.wrappedBuffer(message)));

        final ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();
        Entity entity = world.getEntity(swapData.entityUUID());
        if (entity instanceof ArmorStand armorStandEntity) {
            swapData.handleData(armorStandEntity);
        }
    }
}
