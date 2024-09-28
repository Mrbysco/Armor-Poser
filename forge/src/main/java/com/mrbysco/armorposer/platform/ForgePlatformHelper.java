package com.mrbysco.armorposer.platform;

import com.mrbysco.armorposer.ArmorPoser;
import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSwapMessage;
import com.mrbysco.armorposer.packets.ArmorStandSyncMessage;
import com.mrbysco.armorposer.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.nio.file.Path;

public class ForgePlatformHelper implements IPlatformHelper {
	@Override
	public void updateEntity(ArmorStand armorStand, CompoundTag compound) {
		CompoundTag CompoundNBT = armorStand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		armorStand.load(CompoundNBT);

		ArmorPoser.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorStandSyncMessage(new SyncData(armorStand.getUUID(), compound)));
	}

	@Override
	public void swapSlots(ArmorStand armorStand, SwapData.Action action) {
		ArmorPoser.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ArmorStandSwapMessage(new SwapData(armorStand.getUUID(), action)));
	}

	@Override
	public boolean allowScrolling() {
		return PoserConfig.COMMON.allowScrolling.get();
	}

	@Override
	public Path getUserPresetFolder() {
		return FMLPaths.CONFIGDIR.get();
	}
}
