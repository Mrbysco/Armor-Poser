package com.mrbysco.armorposer.handlers;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.packets.v1.ArmorStandLockedPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandScreenPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSyncGroupsPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class EventHandler {
	private static boolean cancelRightClick = false;

	public static InteractionResult onPlayerEntityInteractSpecific(Player player, Entity target, InteractionHand hand) {
		if (target instanceof ArmorStand armorstand) {
			if (PoserConfig.COMMON.enableConfigGui.get() && player.isShiftKeyDown()) {
				if (hand == InteractionHand.MAIN_HAND && !player.level().isClientSide()) {
					if (ServerPlayNetworking.canSend((ServerPlayer) player, Reference.LOCKED_PACKET_ID_V1))
						ServerPlayNetworking.send((ServerPlayer) player, new ArmorStandLockedPayload(armorstand.getId(), armorstand.isInvulnerable()));
					if (ServerPlayNetworking.canSend((ServerPlayer) player, Reference.SCREEN_PACKET_ID_V1))
						ServerPlayNetworking.send((ServerPlayer) player, new ArmorStandScreenPayload(armorstand.getId(), Reference.getRestrictedFeatures(player)));
					if (ServerPlayNetworking.canSend((ServerPlayer) player, Reference.SYNC_GROUP_PACKET_ID_V1))
						ServerPlayNetworking.send((ServerPlayer) player, new ArmorStandSyncGroupsPayload(Reference.getNearbyGroups(player)));
				}
				return InteractionResult.SUCCESS;
			}

			if (PoserConfig.COMMON.enableNameTags.get() && !player.isShiftKeyDown()) {
				ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
				if (!stack.isEmpty() && stack.getItem() == Items.NAME_TAG && stack.has(DataComponents.CUSTOM_NAME)) {
					cancelRightClick = true;
					if (hand == InteractionHand.MAIN_HAND && !player.level().isClientSide()) {
						armorstand.setCustomName(stack.getHoverName());
						armorstand.setCustomNameVisible(true);
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.PASS;
	}

	public static InteractionResult onPlayerRightClickItem(Player player, InteractionHand hand) {
		if (cancelRightClick) {
			cancelRightClick = false;
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

}
