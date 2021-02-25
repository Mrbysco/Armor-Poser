package com.mrbysco.armorstand.handlers;

import com.mrbysco.armorstand.ArmorPoser;
import com.mrbysco.armorstand.config.ModConfiguration;
import com.mrbysco.armorstand.packets.ArmorStandScreenMessage;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ArmorPoser.MOD_ID)
public class EventHandler {

    private static boolean cancelRightClick = false;

    @SubscribeEvent
    public static void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof ArmorStandEntity) {
            ArmorStandEntity armorstand = (ArmorStandEntity) event.getTarget();

            if (ModConfiguration.COMMON.enableConfigGui.get() && event.getPlayer().isSneaking()) {
                if (event.getHand() == Hand.MAIN_HAND && !event.getWorld().isRemote) {
                    ArmorPoser.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()), new ArmorStandScreenMessage(armorstand.getEntityId()));
                }
                event.setCanceled(true);
                return;
            }

            if (ModConfiguration.COMMON.enableNameTags.get() && !event.getPlayer().isSneaking()) {
                ItemStack stack = event.getPlayer().getHeldItem(Hand.MAIN_HAND);
                if (!stack.isEmpty() && stack.getItem() == Items.NAME_TAG && stack.hasDisplayName()) {
                    cancelRightClick = true;
                    if (event.getHand() == Hand.MAIN_HAND && !event.getWorld().isRemote) {
                        armorstand.setCustomName(stack.getDisplayName());
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
