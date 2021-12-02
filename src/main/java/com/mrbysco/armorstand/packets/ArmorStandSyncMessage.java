package com.mrbysco.armorstand.packets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.UUID;
import java.util.function.Supplier;


public class ArmorStandSyncMessage {
	private final UUID entityUUID;
	private final CompoundNBT data;

	public ArmorStandSyncMessage(UUID playerUUID, CompoundNBT tag) {
		this.entityUUID = playerUUID;
		this.data = tag;
	}

	public void encode(PacketBuffer buf) {
		buf.writeUUID(entityUUID);
		buf.writeNbt(data);
	}

	public static ArmorStandSyncMessage decode(final PacketBuffer packetBuffer) {
		return new ArmorStandSyncMessage(packetBuffer.readUUID(), packetBuffer.readNbt());
	}

	public void handle(Supplier<Context> context) {
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isServer() && ctx.getSender() != null) {
				final ServerWorld world = ctx.getSender().getLevel();
				Entity entity = world.getEntity(this.entityUUID);
				if (entity instanceof ArmorStandEntity) {
					ArmorStandEntity armorStandEntity = (ArmorStandEntity)entity;

					CompoundNBT entityTag = armorStandEntity.saveWithoutId(new CompoundNBT());
					CompoundNBT entityTagCopy = entityTag.copy();

					if(!this.data.isEmpty()) {
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
