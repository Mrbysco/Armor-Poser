package com.mrbysco.armorposer.packets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.UUID;
import java.util.function.Supplier;


public class ArmorStandSyncMessage {
	private final UUID entityUUID;
	private final CompoundTag data;

	public ArmorStandSyncMessage(UUID playerUUID, CompoundTag tag) {
		this.entityUUID = playerUUID;
		this.data = tag;
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(entityUUID);
		buf.writeNbt(data);
	}

	public static ArmorStandSyncMessage decode(final FriendlyByteBuf packetBuffer) {
		return new ArmorStandSyncMessage(packetBuffer.readUUID(), packetBuffer.readNbt());
	}

	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isServer() && ctx.getSender() != null) {
				final ServerLevel world = ctx.getSender().getLevel();
				Entity entity = world.getEntity(this.entityUUID);
				if (entity instanceof ArmorStand armorStandEntity) {
					CompoundTag entityTag = armorStandEntity.saveWithoutId(new CompoundTag());
					CompoundTag entityTagCopy = entityTag.copy();

					if (!this.data.isEmpty()) {
						entityTagCopy.merge(this.data);
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
