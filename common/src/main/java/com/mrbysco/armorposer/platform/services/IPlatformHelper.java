package com.mrbysco.armorposer.platform.services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;

public interface IPlatformHelper {
	/**
	 * Update Armor Stand Entity
	 */
	void updateEntity(ArmorStand armorStand, CompoundTag compound);

	/**
	 * Allow scrolling to increase/decrease the angle of text fields
	 */
	boolean allowScrolling();
}
