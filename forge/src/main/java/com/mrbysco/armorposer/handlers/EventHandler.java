package com.mrbysco.armorposer.handlers;

import com.mrbysco.armorposer.ArmorPoser;
import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.packets.ArmorStandScreenMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	private static boolean cancelRightClick = false;

	@SubscribeEvent
	public static void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		if (event.getTarget() instanceof ArmorStand armorstand) {
			final Player player = event.getEntity();
			final Level level = event.getLevel();
			if (PoserConfig.COMMON.enableConfigGui.get() && player.isShiftKeyDown()) {
				if (event.getHand() == InteractionHand.MAIN_HAND && !level.isClientSide) {
					ArmorPoser.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new ArmorStandScreenMessage(armorstand.getId()));
				}
				event.setCanceled(true);
				return;
			}

			if (PoserConfig.COMMON.enableNameTags.get() && !player.isShiftKeyDown()) {
				ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
				if (!stack.isEmpty() && stack.getItem() == Items.NAME_TAG && stack.hasCustomHoverName()) {
					cancelRightClick = true;
					if (event.getHand() == InteractionHand.MAIN_HAND && !level.isClientSide) {
						armorstand.setCustomName(stack.getHoverName());
						armorstand.setCustomNameVisible(true);
					}
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (cancelRightClick) {
			cancelRightClick = false;
			event.setCanceled(true);
		}
	}
}
