package com.mrbysco.armorposer.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;

public class ArmorStandData {
	public boolean invisible = false;
	public boolean noBasePlate = false;
	public boolean noGravity = false;
	public boolean showArms = false;
	public boolean small = false;

	public float rotation = 0F;

	public float[] pose = new float[18];


	public boolean getBooleanValue(int index) {
		return switch (index) {
			case 0 -> this.invisible;
			case 1 -> this.noBasePlate;
			case 2 -> this.noGravity;
			case 3 -> this.showArms;
			case 4 -> this.small;
			default -> false;
		};
	}


	public void readFromNBT(CompoundTag compound) {
		this.invisible = compound.getBoolean("Invisible");
		this.noBasePlate = compound.getBoolean("NoBasePlate");
		this.noGravity = compound.getBoolean("NoGravity");
		this.showArms = compound.getBoolean("ShowArms");
		this.small = compound.getBoolean("Small");

		if (compound.contains("Rotation")) {
			this.rotation = compound.getList("Rotation", CompoundTag.TAG_FLOAT).getFloat(0);
		}
		if (compound.contains("Pose")) {
			CompoundTag poseTag = (CompoundTag) compound.get("Pose");

			String[] keys = new String[]{"Head", "Body", "LeftLeg", "RightLeg", "LeftArm", "RightArm"};
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				if (poseTag != null && poseTag.contains(key)) {
					ListTag tagList = poseTag.getList(key, CompoundTag.TAG_FLOAT);
					for (int j = 0; j <= 2; j++) {
						int k = (i * 3) + j;
						this.pose[k] = tagList.getFloat(j);
					}
				}
			}
		}
	}

	public CompoundTag writeToNBT() {
		CompoundTag compound = new CompoundTag();
		compound.putBoolean("Invisible", this.invisible);
		compound.putBoolean("NoBasePlate", this.noBasePlate);
		compound.putBoolean("NoGravity", this.noGravity);
		compound.putBoolean("ShowArms", this.showArms);
		compound.putBoolean("Small", this.small);

		ListTag rotationTag = new ListTag();
		rotationTag.add(FloatTag.valueOf(this.rotation));
		compound.put("Rotation", rotationTag);

		CompoundTag poseTag = new CompoundTag();

		ListTag poseHeadTag = new ListTag();
		poseHeadTag.add(FloatTag.valueOf(this.pose[0]));
		poseHeadTag.add(FloatTag.valueOf(this.pose[1]));
		poseHeadTag.add(FloatTag.valueOf(this.pose[2]));
		poseTag.put("Head", poseHeadTag);

		ListTag poseBodyTag = new ListTag();
		poseBodyTag.add(FloatTag.valueOf(this.pose[3]));
		poseBodyTag.add(FloatTag.valueOf(this.pose[4]));
		poseBodyTag.add(FloatTag.valueOf(this.pose[5]));
		poseTag.put("Body", poseBodyTag);

		ListTag poseLeftLegTag = new ListTag();
		poseLeftLegTag.add(FloatTag.valueOf(this.pose[6]));
		poseLeftLegTag.add(FloatTag.valueOf(this.pose[7]));
		poseLeftLegTag.add(FloatTag.valueOf(this.pose[8]));
		poseTag.put("LeftLeg", poseLeftLegTag);

		ListTag poseRightLegTag = new ListTag();
		poseRightLegTag.add(FloatTag.valueOf(this.pose[9]));
		poseRightLegTag.add(FloatTag.valueOf(this.pose[10]));
		poseRightLegTag.add(FloatTag.valueOf(this.pose[11]));
		poseTag.put("RightLeg", poseRightLegTag);

		ListTag poseLeftArmTag = new ListTag();
		poseLeftArmTag.add(FloatTag.valueOf(this.pose[12]));
		poseLeftArmTag.add(FloatTag.valueOf(this.pose[13]));
		poseLeftArmTag.add(FloatTag.valueOf(this.pose[14]));
		poseTag.put("LeftArm", poseLeftArmTag);

		ListTag poseRightArmTag = new ListTag();
		poseRightArmTag.add(FloatTag.valueOf(this.pose[15]));
		poseRightArmTag.add(FloatTag.valueOf(this.pose[16]));
		poseRightArmTag.add(FloatTag.valueOf(this.pose[17]));
		poseTag.put("RightArm", poseRightArmTag);

		compound.put("Pose", poseTag);
		return compound;
	}

}
