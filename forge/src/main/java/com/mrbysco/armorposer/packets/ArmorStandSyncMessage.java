package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.network.NetworkEvent;


public class ArmorStandSyncMessage {
	private final SyncData data;

	public ArmorStandSyncMessage(SyncData syncData) {
		this.data = syncData;
	}

	public void encode(FriendlyByteBuf buf) {
		data.encode(buf);
	}

	public static ArmorStandSyncMessage decode(final FriendlyByteBuf packetBuffer) {
		return new ArmorStandSyncMessage(SyncData.decode(packetBuffer));
	}

	public void handle(NetworkEvent.Context ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isServer() && ctx.getSender() != null) {
				final ServerLevel serverLevel = ctx.getSender().serverLevel();
				Entity entity = serverLevel.getEntity(data.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					data.handleData(armorStandEntity);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
