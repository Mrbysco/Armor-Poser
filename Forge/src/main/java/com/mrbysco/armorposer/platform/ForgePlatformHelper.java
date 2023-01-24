package com.mrbysco.armorposer.platform;

import com.mrbysco.armorposer.ArmorPoser;
import com.mrbysco.armorposer.packets.ArmorStandSyncMessage;
import com.mrbysco.armorposer.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ForgePlatformHelper implements IPlatformHelper {
	@Override
	public void updateEntity(ArmorStand armorStand, CompoundTag compound) {
		CompoundTag CompoundNBT = armorStand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		armorStand.load(CompoundNBT);

		ArmorPoser.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorStandSyncMessage(armorStand.getUUID(), compound));
	}
}
