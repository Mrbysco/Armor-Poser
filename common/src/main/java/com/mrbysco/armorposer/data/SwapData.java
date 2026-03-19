package com.mrbysco.armorposer.data;

import com.mrbysco.armorposer.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record SwapData(UUID entityUUID, Action action) {
	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(entityUUID);
		buf.writeEnum(action);
	}

	public static SwapData decode(final FriendlyByteBuf packetBuffer) {
		return new SwapData(packetBuffer.readUUID(), packetBuffer.readEnum(Action.class));
	}

	public void handleData(ArmorStand armorStand, Player player) {
		if (entityUUID == null || action == null) {
			Reference.LOGGER.warn("Received SwapData with null fields from player {} - rejecting.", player.getName().getString());
			return;
		}

		// Check uuid match
		if (!armorStand.getUUID().equals(entityUUID)) {
			Reference.LOGGER.warn("Player {} attempted to swap armor stand with mismatched UUID: {} (armor stand UUID: {}).",
					player.getName().getString(),
					entityUUID, armorStand.getUUID());
			return;
		}

		// Dimension check
		var playerDim = player.level().dimension();
		var standDim = armorStand.level().dimension();
		if (!playerDim.equals(standDim)) {
			Reference.LOGGER.warn("Player {} attempted to swap armor stand {} from a different dimension (player dimension: {}, armor stand dimension: {}).",
					player.getName().getString(), entityUUID, player.level().dimension(), armorStand.level().dimension());
			return;
		}

		// Check distance
		final int maxDistance = Reference.getMaxDistance();
		final double maxDistanceSq = maxDistance * maxDistance;
		if (player.distanceToSqr(armorStand) > maxDistanceSq) {
			Reference.LOGGER.warn("Player {} attempted to swap armor stand {} that is too far away (>{} blocks).",
					player.getName().getString(), entityUUID, maxDistance);
			return;
		}

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
