package com.mrbysco.armorposer.data;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public record BookCopyData(UUID entityUUID, CompoundTag tag) {
	public static final StreamCodec<FriendlyByteBuf, BookCopyData> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			BookCopyData::entityUUID,
			ByteBufCodecs.COMPOUND_TAG,
			BookCopyData::tag,
			BookCopyData::new);

	/**
	 * Handles applying the pose data to the player's offhand book if it's an armor poser book
	 *
	 * @param player The player to apply the data to
	 */
	public void handleData(Player player) {
		if (!tag.isEmpty()) {
			ItemStack offStack = player.getOffhandItem();
			// Check if the player is holding an armor poser book
			if (offStack.is(Items.WRITTEN_BOOK) && offStack.getCustomName() != null && offStack.getCustomName().getString().equals("Armor Poser")) {
				CustomData data = offStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
				CompoundTag tagCopy = data.copyTag();
				// Set the datapack to ArmorStatuesV2 to make it compatible with the Armor Statues Datapack
				tagCopy.putString("datapack", "ArmorStatuesV2");
				// Set the pose data to the book
				tagCopy.put("SavedPose", tag);
				offStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tagCopy));
			}
		}
	}

	/**
	 * Handles the pose data for the armor stand
	 *
	 * @param armorStand The armor stand to apply the pose to
	 */
	public void handleFrame(ArmorStand armorStand) {
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
