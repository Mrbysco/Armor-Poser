package com.mrbysco.armorposer.handlers;

import com.mrbysco.armorposer.data.SyncData;
import io.netty.buffer.Unpooled;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import net.minecraft.network.FriendlyByteBuf;

public class SpigotSyncHandler implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        final SyncData syncData = SyncData.decode(new FriendlyByteBuf(Unpooled.wrappedBuffer(message)));

        final ServerLevel world = ((CraftWorld) player.getWorld()).getHandle();
        final Entity entity = world.getEntity(syncData.entityUUID());
        if(entity instanceof ArmorStand armorStand){
            syncData.handleData(armorStand);
        }
    }
}
