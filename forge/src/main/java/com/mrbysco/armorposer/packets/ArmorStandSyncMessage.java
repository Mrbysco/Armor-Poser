package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.function.Supplier;


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

	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isServer() && ctx.getSender() != null) {
				final ServerLevel serverLevel = ctx.getSender().getLevel();
				Entity entity = serverLevel.getEntity(data.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					data.handleData(armorStandEntity);
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
