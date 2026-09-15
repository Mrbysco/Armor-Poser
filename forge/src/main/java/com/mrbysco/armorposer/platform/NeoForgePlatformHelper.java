package com.mrbysco.armorposer.platform;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.client.util.ChannelHelper;
import com.mrbysco.armorposer.data.GroupData;
import com.mrbysco.armorposer.data.RenameData;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.v1.ArmorStandRenamePayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSyncPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandUpdateGroupsPayload;
import com.mrbysco.armorposer.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.nio.file.Path;
import java.util.List;

public class NeoForgePlatformHelper implements IPlatformHelper {
	@Override
	public void updateEntity(ArmorStand armorStand, CompoundTag compound) {
		// Create a new TagValueOutput to save the armor stand's data
		try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(Reference.LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(problemreporter$scopedcollector, armorStand.registryAccess());

			// Save the armor stand's current state without an ID
			armorStand.saveWithoutId(output);
			// Build the result compound tag from the output
			CompoundTag outputCompound = output.buildResult();
			// Merge the provided compound data into the output compound
			outputCompound.merge(compound);

			// Load the armor stand with the updated compound data
			armorStand.load(TagValueInput.create(ProblemReporter.DISCARDING, armorStand.registryAccess(), outputCompound));

			SyncData data = new SyncData(armorStand.getUUID(), outputCompound);
			if (ChannelHelper.hasChannel(Reference.SYNC_PACKET_ID_V1))
				ClientPacketDistributor.sendToServer(new ArmorStandSyncPayload(data));
		}
	}

	@Override
	public void updateEntityInGroup(List<ArmorStand> armorStands, CompoundTag compound) {
		for (ArmorStand armorStand : armorStands) {
			updateEntity(armorStand, compound);
		}
	}

	@Override
	public void updateEntityGroups(ArmorStand armorStand, List<String> groups) {
		GroupData data = new GroupData(armorStand.getUUID(), groups);
		if (ChannelHelper.hasChannel(Reference.UPDATE_GROUP_PACKET_ID_V1))
			ClientPacketDistributor.sendToServer(new ArmorStandUpdateGroupsPayload(data));
	}

	@Override
	public void swapSlots(ArmorStand armorStand, SwapData.Action action) {
		if (ChannelHelper.hasChannel(Reference.SWAP_PACKET_ID_V1))
			ClientPacketDistributor.sendToServer(new ArmorStandSwapPayload(new SwapData(armorStand.getUUID(), action)));
	}

	@Override
	public void renameArmorStand(ArmorStand armorStand, String newName) {
		if (ChannelHelper.hasChannel(Reference.RENAME_PACKET_ID_V1))
			ClientPacketDistributor.sendToServer(new ArmorStandRenamePayload(new RenameData(armorStand.getUUID(), newName)));
	}

	@Override
	public Path getUserPresetFolder() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public String getModVersion() {
		return ModList.get().getModFileById(Reference.MOD_ID).versionString();
	}
}
