package com.mrbysco.armorposer.data;

import com.mrbysco.armorposer.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record SyncData(UUID entityUUID, CompoundTag tag) {
	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(entityUUID);
		buf.writeNbt(tag);
	}

	public static SyncData decode(final FriendlyByteBuf packetBuffer) {
		return new SyncData(packetBuffer.readUUID(), packetBuffer.readNbt());
	}

	public void handleData(ArmorStand armorStand, Player player) {
		CompoundTag entityTag = armorStand.saveWithoutId(new CompoundTag());
		CompoundTag entityTagCopy = entityTag.copy();

		if (!tag.isEmpty()) {
			entityTagCopy.merge(tag);
			armorStand.load(entityTagCopy);
			armorStand.setUUID(entityUUID);

			ListTag tagList = tag.getList("Move", Tag.TAG_DOUBLE);
			double xOffset = tagList.getDouble(0);
			double yOffset = tagList.getDouble(1);
			double zOffset = tagList.getDouble(2);
			if (xOffset != 0 || yOffset != 0 || zOffset != 0)
				armorStand.setPosRaw(armorStand.getX() + xOffset,
						armorStand.getY() + yOffset,
						armorStand.getZ() + zOffset);

			if (Reference.canResize(player)) {
				double scale = tag.getDouble("Scale");
				if (scale > 0) {
					AttributeInstance attributeInstance = armorStand.getAttributes().getInstance(Attributes.SCALE);
					if (attributeInstance != null) {
						attributeInstance.setBaseValue(scale);
					}
				}
			}
		}
	}
}
