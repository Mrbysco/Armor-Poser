package com.mrbysco.armorposer.mixin;

import com.mrbysco.armorposer.ArmorPoser;
import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import com.mrbysco.armorposer.packets.ArmorStandSyncMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ArmorStandScreen.class)
public class ArmorStandScreenMixin {

	/**
	 * @author Mrbysco
	 * @reason How else am I gonna abstract networking
	 */
	@Overwrite(remap = false)
	private void updateEntity(ArmorStand stand, CompoundTag compound) {
		CompoundTag CompoundNBT = stand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		stand.load(CompoundNBT);

		ArmorPoser.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorStandSyncMessage(stand.getUUID(), compound));
	}

}
