package com.mrbysco.armorposer.handlers;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mrbysco.armorposer.ArmorPoser;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SpigotEventHandler implements Listener {

    ArmorPoser pluginInstance;

    public SpigotEventHandler(ArmorPoser pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Entity target = event.getRightClicked();
        if (target instanceof ArmorStand armorStand && event.getHand().equals(EquipmentSlot.HAND))
        {
            FileConfiguration config = this.pluginInstance.getConfig();
            Player player = event.getPlayer();

            if (config.getBoolean("enableConfigGui") && player.isSneaking())
            {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeInt(armorStand.getEntityId());

                // channelid should be Reference.SCREEN_PACKET_ID.atoString(), but that causes
                // Cannot access net.minecraft.resources.ResourceLocation
                // and I don't know how to fix that in Spigot
                player.sendPluginMessage(this.pluginInstance, ArmorPoser.CHANNEL_ID_SCREEN, out.toByteArray());
            }
            else if (config.getBoolean("enableNameTags") && !player.isSneaking())
            {
                ItemStack stack = player.getInventory().getItemInMainHand();
                if (stack != null && stack.getType().equals(Material.NAME_TAG) && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
                {
                    armorStand.setCustomName(stack.getItemMeta().getDisplayName());
                    armorStand.setCustomNameVisible(true);
                }
            }
        }
    }

}
