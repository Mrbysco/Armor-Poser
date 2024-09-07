package com.mrbysco.armorposer.platform;

import com.mrbysco.armorposer.ArmorPoser;
import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import com.mrbysco.armorposer.platform.services.IPlatformHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.List;

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

	@Override
	public boolean isResizeRestrictedToOPS() {
		return PoserConfig.COMMON.restrictResizeToOP.get();
	}

	@Override
	public List<? extends String> getResizeWhitelist() {
		return PoserConfig.COMMON.resizeWhitelist.get();
	}

	@Override
	public String getModVersion() {
		return ModList.get().getModFileById(Reference.MOD_ID).versionString();
	}

	@Override
	public KeyMapping registerKeyMapping(KeyMapping mapping) {
		ArmorPoser.KEY_MAPPINGS.add(mapping);
		return mapping;
	}
}
