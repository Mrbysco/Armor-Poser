package com.mrbysco.armorposer.platform;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import com.mrbysco.armorposer.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements IPlatformHelper {
	@Override
	public void updateEntity(ArmorStand armorStand, CompoundTag compound) {
		CompoundTag CompoundNBT = armorStand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		armorStand.load(CompoundNBT);

		PacketDistributor.sendToServer(new ArmorStandSyncPayload(new SyncData(armorStand.getUUID(), compound)));
	}

	@Override
	public void swapSlots(ArmorStand armorStand, SwapData.Action action) {
		PacketDistributor.sendToServer(new ArmorStandSwapPayload(new SwapData(armorStand.getUUID(), action)));
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
