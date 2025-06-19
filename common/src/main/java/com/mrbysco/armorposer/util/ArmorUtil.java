package com.mrbysco.armorposer.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorUtil {
	public static CompoundTag writeAllPoses(ArmorStand armorStand) {
		CompoundTag compoundTag = new CompoundTag();

		compoundTag.put("Head", armorStand.getHeadPose().save());
		compoundTag.put("Body", armorStand.getBodyPose().save());
		compoundTag.put("LeftArm", armorStand.getLeftArmPose().save());
		compoundTag.put("RightArm", armorStand.getRightArmPose().save());
		compoundTag.put("LeftLeg", armorStand.getLeftLegPose().save());
		compoundTag.put("RightLeg", armorStand.getRightLegPose().save());

		return compoundTag;
	}
}
