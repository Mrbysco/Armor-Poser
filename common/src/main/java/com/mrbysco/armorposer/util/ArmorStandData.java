package com.mrbysco.armorposer.util;

import com.mrbysco.armorposer.Reference;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec2;

import java.util.Arrays;
import java.util.Optional;

public class ArmorStandData {
	public boolean invisible = false;
	public boolean noBasePlate = false;
	public boolean noGravity = false;
	public boolean showArms = false;
	public boolean small = false;
	public boolean nameVisible = true;
	public boolean locked = true;

	public float rotation = 0F;

	public final float[] pose = new float[3 * 7];


	public boolean getBooleanValue(int index) {
		return switch (index) {
			case 0 -> this.invisible;
			case 1 -> !this.noBasePlate;
			case 2 -> !this.noGravity;
			case 3 -> this.showArms;
			case 4 -> this.small;
			case 5 -> this.nameVisible;
			case 6 -> this.locked;
			default -> false;
		};
	}


	public void readFromNBT(CompoundTag compound) {
		Arrays.fill(this.pose, 0f);
		this.invisible = compound.getBooleanOr("Invisible", false);
		this.noBasePlate = compound.getBooleanOr("NoBasePlate", false);
		this.noGravity = compound.getBooleanOr("NoGravity", false);
		this.showArms = compound.getBooleanOr("ShowArms", false);
		this.small = compound.getBooleanOr("Small", false);
		this.nameVisible = compound.getBooleanOr("CustomNameVisible", false);
		this.locked = compound.getBooleanOr("Invulnerable", false);

		Optional<Vec2> rotation = compound.read("Rotation", Vec2.CODEC);
		if (rotation.isPresent()) {
			this.rotation = rotation.get().x;
		}
		if (compound.contains("Pose")) {
			CompoundTag poseTag = compound.getCompoundOrEmpty("Pose");
			if (poseTag.isEmpty()) {
				Reference.LOGGER.warn("Pose tag is empty, skipping pose data");
				return;
			}

			String[] keys = new String[]{"Head", "Body", "LeftLeg", "RightLeg", "LeftArm", "RightArm"};
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				if (poseTag.contains(key)) {
					Rotations rotations = poseTag.read(key, Rotations.CODEC).orElse(new Rotations(0, 0, 0));
					this.pose[i * 3] = rotations.x();
					this.pose[(i * 3) + 1] = rotations.y();
					this.pose[(i * 3) + 2] = rotations.z();
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
		compound.putBoolean("CustomNameVisible", this.nameVisible);
		compound.putBoolean("Invulnerable", this.locked);
		compound.putInt("DisabledSlots", this.locked ? 4144959 : 0);

		compound.store("Rotation", Vec2.CODEC, new Vec2(this.rotation, 0));

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

	public CompoundTag getDifference(ArmorStandData initial) {
		CompoundTag tag = new CompoundTag();

		if (this.invisible != initial.invisible)
			tag.putBoolean("Invisible", this.invisible);
		if (this.noBasePlate != initial.noBasePlate)
			tag.putBoolean("NoBasePlate", this.noBasePlate);
		if (this.noGravity != initial.noGravity)
			tag.putBoolean("NoGravity", this.noGravity);
		if (this.showArms != initial.showArms)
			tag.putBoolean("ShowArms", this.showArms);
		if (this.small != initial.small)
			tag.putBoolean("Small", this.small);
		if (this.nameVisible != initial.nameVisible)
			tag.putBoolean("CustomNameVisible", this.nameVisible);
		if (this.locked != initial.locked) {
			tag.putBoolean("Invulnerable", this.locked);
			tag.putInt("DisabledSlots", this.locked ? 4144959 : 0);
		}
		if (this.rotation != initial.rotation)
			tag.store("Rotation", Vec2.CODEC, new Vec2(this.rotation, 0));

		// Build pose tag — only include changed limbs
		CompoundTag poseTag = new CompoundTag();
		String[] keys = {"Head", "Body", "LeftLeg", "RightLeg", "LeftArm", "RightArm"};
		for (int i = 0; i < keys.length; i++) {
			int base = i * 3;
			if (this.pose[base] != initial.pose[base]
					|| this.pose[base + 1] != initial.pose[base + 1]
					|| this.pose[base + 2] != initial.pose[base + 2]) {
				poseTag.store(keys[i], Rotations.CODEC,
						new Rotations(this.pose[base], this.pose[base + 1], this.pose[base + 2]));
			}
		}
		if (!poseTag.isEmpty()) tag.put("Pose", poseTag);

		return tag;
	}
}
