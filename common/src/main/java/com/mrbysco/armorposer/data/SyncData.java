package com.mrbysco.armorposer.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.UUID;

public record SyncData(UUID entityUUID, CompoundTag tag) {
	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(entityUUID);
		buf.writeNbt(tag);
	}

	public static SyncData decode(final FriendlyByteBuf packetBuffer) {
		return new SyncData(packetBuffer.readUUID(), packetBuffer.readNbt());
	}

	public void handleData(ArmorStand armorStand) {
		CompoundTag entityTag = armorStand.saveWithoutId(new CompoundTag());
		CompoundTag entityTagCopy = entityTag.copy();

		if (!tag.isEmpty()) {
			entityTagCopy.merge(tag);
			armorStand.load(entityTagCopy);
			armorStand.setUUID(entityUUID);
		}

		ListTag tagList = tag.getList("Move", Tag.TAG_DOUBLE);
		double x = tagList.getDouble(0);
		double y = tagList.getDouble(1);
		double z = tagList.getDouble(2);
		armorStand.setPosRaw(armorStand.getX() + x,
				armorStand.getY() + y,
				armorStand.getZ() + z);
	}
}
