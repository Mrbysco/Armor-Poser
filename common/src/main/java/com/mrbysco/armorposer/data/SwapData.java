package com.mrbysco.armorposer.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record SwapData(UUID entityUUID, Action action) {
	public void write(FriendlyByteBuf buf) {
		buf.writeUUID(entityUUID);
		buf.writeEnum(action);
	}

	public static SwapData read(final FriendlyByteBuf packetBuffer) {
		return new SwapData(packetBuffer.readUUID(), packetBuffer.readEnum(Action.class));
	}

	public void handleData(ArmorStand armorStand) {
		switch (action) {
			case SWAP_HANDS:
				ItemStack offStack = armorStand.getItemInHand(InteractionHand.OFF_HAND);
				armorStand.setItemInHand(InteractionHand.OFF_HAND, armorStand.getItemInHand(InteractionHand.MAIN_HAND));
				armorStand.setItemInHand(InteractionHand.MAIN_HAND, offStack);
				return;
			case SWAP_WITH_HEAD:
				ItemStack headStack = armorStand.getItemBySlot(EquipmentSlot.HEAD);
				armorStand.setItemSlot(EquipmentSlot.HEAD, armorStand.getItemBySlot(EquipmentSlot.MAINHAND));
				armorStand.setItemSlot(EquipmentSlot.MAINHAND, headStack);
				return;
			default:
				throw new IllegalArgumentException("Invalid Pose action");
		}
	}

	public static enum Action {
		SWAP_WITH_HEAD,
		SWAP_HANDS
	}
}
