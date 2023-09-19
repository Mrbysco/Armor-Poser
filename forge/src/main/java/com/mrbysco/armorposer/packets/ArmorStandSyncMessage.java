package com.mrbysco.armorposer.packets;

import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.UUID;
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
				final ServerLevel serverLevel = ctx.getSender().serverLevel();
				Entity entity = serverLevel.getEntity(data.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					CompoundTag entityTag = armorStandEntity.saveWithoutId(new CompoundTag());
					CompoundTag entityTagCopy = entityTag.copy();

					CompoundTag tag = data.tag();
					if (!tag.isEmpty()) {
						entityTagCopy.merge(tag);
						UUID uuid = armorStandEntity.getUUID();
						armorStandEntity.load(entityTagCopy);
						armorStandEntity.setUUID(uuid);
					}
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
