package com.mrbysco.armorposer.platform.services;

import com.mrbysco.armorposer.data.SwapData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.nio.file.Path;
import java.util.List;

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

	/**
	 * Check if the resize is restricted to OPs
	 * @return If the resize is restricted to OPs
	 */
	boolean isResizeRestrictedToOPS();

	/**
	 * Gets a list of players that are allowed to resize the Armor Stand while restrictResizeToOP is enabled
	 * @return The resize whitelist
	 */
	List<? extends String> getResizeWhitelist();
}
