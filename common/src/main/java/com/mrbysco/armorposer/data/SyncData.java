package com.mrbysco.armorposer.data;

import com.mrbysco.armorposer.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

public record SyncData(UUID entityUUID, CompoundTag tag) {
	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(entityUUID);
		buf.writeNbt(tag);
	}

	public static SyncData decode(final FriendlyByteBuf packetBuffer) {
		return new SyncData(packetBuffer.readUUID(), packetBuffer.readNbt());
	}

	private static final List<String> allowedKeys = List.of(
			"Invisible", "NoBasePlate", "NoGravity", "ShowArms", "Small", "CustomNameVisible", "Invulnerable",
			"DisabledSlots", "Pose", "Scale", "Move", "Rotation"
	);
	private static final double MAX_MOVE_OFFSET = 8.0D;

	public void handleData(ArmorStand armorStand, Player player) {
		if (entityUUID == null || tag == null) {
			Reference.LOGGER.warn("Received SyncData with null fields from player {} - rejecting.", player.getName().getString());
			return;
		}

		// Check uuid match
		if (!armorStand.getUUID().equals(entityUUID)) {
			Reference.LOGGER.warn("Player {} attempted to sync armor stand with mismatched UUID: {} (armor stand UUID: {}).",
					player.getName().getString(),
					entityUUID, armorStand.getUUID());
			return;
		}
		// Dimension check
		var playerDim = player.level().dimension();
		var standDim = armorStand.level().dimension();
		if (!playerDim.equals(standDim)) {
			Reference.LOGGER.warn("Player {} attempted to sync armor stand {} from a different dimension (player dimension: {}, armor stand dimension: {}).",
					player.getName().getString(), entityUUID, player.level().dimension(), armorStand.level().dimension());
			return;
		}

		// Check distance
		final int maxDistance = Reference.getMaxDistance();
		final double maxDistanceSq = maxDistance * maxDistance;
		if (player.distanceToSqr(armorStand) > maxDistanceSq) {
			Reference.LOGGER.warn("Player {} attempted to sync armor stand {} that is too far away (>{} blocks).",
					player.getName().getString(), entityUUID, maxDistance);
			return;
		}

		// Reject empty tags
		if (tag.isEmpty()) {
			return;
		}

		// Remove any keys that aren't in the allowed list
		List<String> keysToRemove = tag.getAllKeys().stream()
				.filter(key -> !allowedKeys.contains(key))
				.toList();
		keysToRemove.forEach(tag::remove);

		CompoundTag entityTag = armorStand.saveWithoutId(new CompoundTag());
		CompoundTag entityTagCopy = entityTag.copy();

		if (!tag.isEmpty()) {
			// Validate Move early so invalid data is removed before we merge/load it
			if (tag.contains("Move")) {
				try {
					ListTag moveList = tag.getList("Move", Tag.TAG_DOUBLE);
					if (moveList.size() < 3) {
						Reference.LOGGER.warn("Rejecting Move from SyncData for {} because Move list has <3 elements", entityUUID);
						tag.remove("Move");
					} else {
						double x = moveList.getDouble(0);
						double y = moveList.getDouble(1);
						double z = moveList.getDouble(2);

						boolean finite = Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
						boolean withinBounds = Math.abs(x) <= MAX_MOVE_OFFSET &&
								Math.abs(y) <= MAX_MOVE_OFFSET &&
								Math.abs(z) <= MAX_MOVE_OFFSET;

						if (!finite) {
							Reference.LOGGER.warn("Rejecting Move from SyncData for {} because offset contains non-finite values: {},{},{}", entityUUID, x, y, z);
							tag.remove("Move");
						} else if (!withinBounds) {
							Reference.LOGGER.warn("Rejecting Move from SyncData for {} because offset exceeds max bounds (>{}): {},{},{}", entityUUID, MAX_MOVE_OFFSET, x, y, z);
							tag.remove("Move");
						}
					}
				} catch (Exception e) {
					Reference.LOGGER.warn("Failed to read Move from SyncData for {}: {}; removing Move key.", entityUUID, e.toString());
					tag.remove("Move");
				}
			}

			entityTagCopy.merge(tag);
			armorStand.load(entityTagCopy);
			armorStand.setUUID(entityUUID);

			if (tag.contains("Move")) {
				ListTag tagList = tag.getList("Move", Tag.TAG_DOUBLE);
				double x = tagList.getDouble(0);
				double y = tagList.getDouble(1);
				double z = tagList.getDouble(2);
				if (x != 0 || y != 0 || z != 0) {
					armorStand.setPosRaw(
							armorStand.getX() + x,
							armorStand.getY() + y,
							armorStand.getZ() + z
					);
				}
			}
		}
	}
}
