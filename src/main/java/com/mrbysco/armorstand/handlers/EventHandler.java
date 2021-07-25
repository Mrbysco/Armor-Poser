package com.mrbysco.armorstand.handlers;

import com.mrbysco.armorstand.ArmorPoser;
import com.mrbysco.armorstand.config.ModConfiguration;
import com.mrbysco.armorstand.packets.ArmorStandScreenMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ArmorPoser.MOD_ID)
public class EventHandler {
    private static boolean cancelRightClick = false;

    @SubscribeEvent
    public static void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof ArmorStand) {
            ArmorStand armorstand = (ArmorStand) event.getTarget();

            if (ModConfiguration.COMMON.enableConfigGui.get() && event.getPlayer().isShiftKeyDown()) {
                if (event.getHand() == InteractionHand.MAIN_HAND && !event.getWorld().isClientSide) {
                    ArmorPoser.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()), new ArmorStandScreenMessage(armorstand.getId()));
                }
                event.setCanceled(true);
                return;
            }

            if (ModConfiguration.COMMON.enableNameTags.get() && !event.getPlayer().isShiftKeyDown()) {
                ItemStack stack = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
                if (!stack.isEmpty() && stack.getItem() == Items.NAME_TAG && stack.hasCustomHoverName()) {
                    cancelRightClick = true;
                    if (event.getHand() == InteractionHand.MAIN_HAND && !event.getWorld().isClientSide) {
                        armorstand.setCustomName(stack.getHoverName());
                        armorstand.setCustomNameVisible(true);
                    }
                    event.setCanceled(true);
                    return;
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
