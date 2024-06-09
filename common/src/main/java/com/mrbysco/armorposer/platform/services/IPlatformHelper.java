package com.mrbysco.armorposer.platform.services;

import com.mrbysco.armorposer.data.SwapData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.nio.file.Path;

public interface IPlatformHelper {
	/**
	 * Update Armor Stand Entity
	 */
	void updateEntity(ArmorStand armorStand, CompoundTag compound);

	/**
	 * Update Armor Stand Entity
	 */
	void swapSlots(ArmorStand armorStand, SwapData.Action action);

	/**
	 * Allow scrolling to increase/decrease the angle of text fields
	 */
	boolean allowScrolling();

	/**
	 * Get the user preset folder
	 * @return The user preset folder
	 */
	Path getUserPresetFolder();
}
