package com.mrbysco.armorstand.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class ArmorStandData {
	public boolean invisible = false;
	public boolean noBasePlate = false;
	public boolean noGravity = false;
	public boolean showArms = false;
	public boolean small = false;

	public float rotation = 0F;

	public float[] pose = new float[18];


	public boolean getBooleanValue(int index) {
		switch (index) {
			case 0:
				return this.invisible;
			case 1:
				return this.noBasePlate;
			case 2:
				return this.noGravity;
			case 3:
				return this.showArms;
			case 4:
				return this.small;
		}
		return false;
	}


	public void readFromNBT(CompoundNBT compound) {
		this.invisible = compound.getBoolean("Invisible");
		this.noBasePlate = compound.getBoolean("NoBasePlate");
		this.noGravity = compound.getBoolean("NoGravity");
		this.showArms = compound.getBoolean("ShowArms");
		this.small = compound.getBoolean("Small");

		if (compound.contains("Rotation")) {
			this.rotation = compound.getList("Rotation", NBT.TAG_FLOAT).getFloat(0);
		}
		if (compound.contains("Pose")) {
			CompoundNBT poseTag = (CompoundNBT)compound.get("Pose");

			String[] keys = new String[] { "Head", "Body", "LeftLeg", "RightLeg", "LeftArm", "RightArm" };
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				if (poseTag != null && poseTag.contains(key)) {
					ListNBT tagList = poseTag.getList(key, NBT.TAG_FLOAT);
					for (int j = 0; j <= 2; j++) {
						int k = (i * 3) + j;
						this.pose[k] = tagList.getFloat(j);
					}
				}
			}
		}
	}

	public CompoundNBT writeToNBT() {
		CompoundNBT compound = new CompoundNBT();
		compound.putBoolean("Invisible", this.invisible);
		compound.putBoolean("NoBasePlate", this.noBasePlate);
		compound.putBoolean("NoGravity", this.noGravity);
		compound.putBoolean("ShowArms", this.showArms);
		compound.putBoolean("Small", this.small);

		ListNBT rotationTag = new ListNBT();
		rotationTag.add(FloatNBT.valueOf(this.rotation));
		compound.put("Rotation", rotationTag);

		CompoundNBT poseTag = new CompoundNBT();

		ListNBT poseHeadTag = new ListNBT();
		poseHeadTag.add(FloatNBT.valueOf(this.pose[0]));
		poseHeadTag.add(FloatNBT.valueOf(this.pose[1]));
		poseHeadTag.add(FloatNBT.valueOf(this.pose[2]));
		poseTag.put("Head", poseHeadTag);

		ListNBT poseBodyTag = new ListNBT();
		poseBodyTag.add(FloatNBT.valueOf(this.pose[3]));
		poseBodyTag.add(FloatNBT.valueOf(this.pose[4]));
		poseBodyTag.add(FloatNBT.valueOf(this.pose[5]));
		poseTag.put("Body", poseBodyTag);

		ListNBT poseLeftLegTag = new ListNBT();
		poseLeftLegTag.add(FloatNBT.valueOf(this.pose[6]));
		poseLeftLegTag.add(FloatNBT.valueOf(this.pose[7]));
		poseLeftLegTag.add(FloatNBT.valueOf(this.pose[8]));
		poseTag.put("LeftLeg", poseLeftLegTag);

		ListNBT poseRightLegTag = new ListNBT();
		poseRightLegTag.add(FloatNBT.valueOf(this.pose[9]));
		poseRightLegTag.add(FloatNBT.valueOf(this.pose[10]));
		poseRightLegTag.add(FloatNBT.valueOf(this.pose[11]));
		poseTag.put("RightLeg", poseRightLegTag);

		ListNBT poseLeftArmTag = new ListNBT();
		poseLeftArmTag.add(FloatNBT.valueOf(this.pose[12]));
		poseLeftArmTag.add(FloatNBT.valueOf(this.pose[13]));
		poseLeftArmTag.add(FloatNBT.valueOf(this.pose[14]));
		poseTag.put("LeftArm", poseLeftArmTag);

		ListNBT poseRightArmTag = new ListNBT();
		poseRightArmTag.add(FloatNBT.valueOf(this.pose[15]));
		poseRightArmTag.add(FloatNBT.valueOf(this.pose[16]));
		poseRightArmTag.add(FloatNBT.valueOf(this.pose[17]));
		poseTag.put("RightArm", poseRightArmTag);

		compound.put("Pose", poseTag);
		return compound;
	}

}
