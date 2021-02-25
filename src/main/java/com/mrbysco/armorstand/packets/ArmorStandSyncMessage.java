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
	private UUID entityUUID;
	private CompoundNBT data;

	public ArmorStandSyncMessage(UUID playerUUID, CompoundNBT tag) {
		this.entityUUID = playerUUID;
		this.data = tag;
	}

	private ArmorStandSyncMessage(PacketBuffer buf) {
		this.entityUUID = buf.readUniqueId();
		this.data = buf.readCompoundTag();
	}

	public void encode(PacketBuffer buf) {
		buf.writeUniqueId(entityUUID);
		buf.writeCompoundTag(data);
	}

	public static ArmorStandSyncMessage decode(final PacketBuffer packetBuffer) {
		return new ArmorStandSyncMessage(packetBuffer.readUniqueId(), packetBuffer.readCompoundTag());
	}

	public void handle(Supplier<Context> context) {
		NetworkEvent.Context ctx = context.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection().getReceptionSide().isServer() && ctx.getSender() != null) {
				final ServerWorld world = ctx.getSender().getServerWorld();
				Entity entity = world.getEntityByUuid(this.entityUUID);
				if (entity instanceof ArmorStandEntity) {
					ArmorStandEntity armorStandEntity = (ArmorStandEntity)entity;

					CompoundNBT entityTag = armorStandEntity.writeWithoutTypeId(new CompoundNBT());
					CompoundNBT entityTagCopy = entityTag.copy();

					if(!this.data.isEmpty()) {
						entityTagCopy.merge(this.data);
						UUID uuid = armorStandEntity.getUniqueID();
						armorStandEntity.read(entityTagCopy);
						armorStandEntity.setUniqueId(uuid);
					}
				}
			}
		});
		ctx.setPacketHandled(true);
	}
}
