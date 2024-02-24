package com.mrbysco.armorposer;

import com.mojang.logging.LogUtils;
import com.mrbysco.armorposer.poses.UserPoseHandler;
import com.mrbysco.armorposer.util.PoseData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Reference {
	public static final String MOD_ID = "armorposer";
	public static final String MOD_NAME = "Armor Poser";
	public static final Logger LOGGER = LogUtils.getLogger();


	public static final ResourceLocation SYNC_PACKET_ID = new ResourceLocation(Reference.MOD_ID, "sync_packet");
	public static final ResourceLocation SWAP_PACKET_ID = new ResourceLocation(Reference.MOD_ID, "swap_packet");
	public static final ResourceLocation SCREEN_PACKET_ID = new ResourceLocation(Reference.MOD_ID, "screen_packet");

	public static final Map<String, String> defaultPoseMap = initializePoseMap();

	private static Map<String, String> initializePoseMap() {
		Map<String, String> poseMap = new LinkedHashMap<>();
		poseMap.put("attention", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,0.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[0.0f,0.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("walking", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[-20.0f,0.0f,-10.0f],LeftLeg:[20.0f,0.0f,0.0f],RightArm:[20.0f,0.0f,10.0f],RightLeg:[-20.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("running", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[40.0f,0.0f,-10.0f],LeftLeg:[-40.0f,0.0f,0.0f],RightArm:[-40.0f,0.0f,10.0f],RightLeg:[40.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("pointing", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,20.0f,0.0f],LeftArm:[0.0f,0.0f,-10.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[-90.0f,18.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("blocking", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[-50.0f,50.0f,0.0f],LeftLeg:[20.0f,0.0f,0.0f],RightArm:[-20.0f,-20.0f,0.0f],RightLeg:[-20.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("lunging", "{Pose:{Body:[15.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[10.0f,0.0f,-10.0f],LeftLeg:[30.0f,0.0f,0.0f],RightArm:[-60.0f,-10.0f,0.0f],RightLeg:[-15.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("winning", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[-15.0f,0.0f,0.0f],LeftArm:[10.0f,0.0f,-10.0f],LeftLeg:[15.0f,0.0f,0.0f],RightArm:[-120.0f,-10.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("sitting", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[-80.0f,-20.0f,0.0f],LeftLeg:[-90.0f,-10.0f,0.0f],RightArm:[-80.0f,20.0f,0.0f],RightLeg:[-90.0f,10.0f,0.0f]},ShowArms:1b}");
		poseMap.put("arabesque", "{Pose:{Body:[10.0f,0.0f,0.0f],Head:[-15.0f,0.0f,0.0f],LeftArm:[70.0f,0.0f,-10.0f],LeftLeg:[75.0f,0.0f,0.0f],RightArm:[-140.0f,-10.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("cupid", "{Pose:{Body:[10.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[-75.0f,0.0f,10.0f],LeftLeg:[75.0f,0.0f,0.0f],RightArm:[-90.0f,-10.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("point_and_laugh", "{Pose:{Body:[10.0f,7.0f,8.0f],Head:[25.0f,17.0f,-8.0f],LeftArm:[-90.0f,0.0f,20.0f],LeftLeg:[20.0f,30.0f,-10.0f],RightArm:[-8.0f,0.0f,-77.0f],RightLeg:[20.0f,-10.0f,20.0f]},ShowArms:1b}");
		poseMap.put("confident", "{Pose:{Body:[-2.0f,0.0f,0.0f],Head:[-10.0f,20.0f,0.0f],LeftArm:[5.0f,0.0f,0.0f],LeftLeg:[0.0f,-10.0f,-4.0f],RightArm:[5.0f,0.0f,0.0f],RightLeg:[16.0f,2.0f,10.0f]},ShowArms:1b}");
		poseMap.put("salute", "{Pose:{Body:[5.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[29.0f,0.0f,25.0f],LeftLeg:[0.0f,4.0f,2.0f],RightArm:[-124.0f,-51.0f,-35.0f],RightLeg:[0.0f,-4.0f,-2.0f]},ShowArms:1b}");
		poseMap.put("death", "{Pose:{Body:[-90.0f,0.0f,0.0f],Head:[-85.0f,0.0f,0.0f],LeftArm:[-90.0f,-10.0f,0.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[-90.0f,10.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("facepalm", "{Pose:{Body:[10.0f,0.0f,0.0f],Head:[45.0f,-4.0f,1.0f],LeftArm:[-72.0f,24.0f,47.0f],LeftLeg:[-4.0f,-6.0f,-2.0f],RightArm:[18.0f,-14.0f,0.0f],RightLeg:[25.0f,-2.0f,0.0f]},ShowArms:1b}");
		poseMap.put("lazing", "{Pose:{Body:[5.0f,0.0f,0.0f],Head:[14.0f,-12.0f,6.0f],LeftArm:[-4.0f,-20.0f,-10.0f],LeftLeg:[-88.0f,46.0f,0.0f],RightArm:[-40.0f,20.0f,0.0f],RightLeg:[-88.0f,71.0f,0.0f]},ShowArms:1b}");
		poseMap.put("confused", "{Pose:{Body:[0.0f,13.0f,0.0f],Head:[0.0f,30.0f,0.0f],LeftArm:[145.0f,22.0f,-49.0f],LeftLeg:[-6.0f,0.0f,0.0f],RightArm:[-22.0f,31.0f,10.0f],RightLeg:[6.0f,-20.0f,0.0f]},ShowArms:1b}");
		poseMap.put("formal", "{Pose:{Body:[4.0f,0.0f,0.0f],Head:[4.0f,0.0f,0.0f],LeftArm:[30.0f,-20.0f,21.0f],LeftLeg:[0.0f,0.0f,-5.0f],RightArm:[30.0f,22.0f,-20.0f],RightLeg:[0.0f,0.0f,5.0f]},ShowArms:1b}");
		poseMap.put("sad", "{Pose:{Body:[10.0f,0.0f,0.0f],Head:[63.0f,0.0f,0.0f],LeftArm:[-5.0f,0.0f,-5.0f],LeftLeg:[-5.0f,16.0f,-5.0f],RightArm:[-5.0f,0.0f,5.0f],RightLeg:[-5.0f,-10.0f,5.0f]},ShowArms:1b}");
		poseMap.put("joyous", "{Pose:{Body:[-4.0f,0.0f,0.0f],Head:[-11.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,-100.0f],LeftLeg:[-8.0f,0.0f,-60.0f],RightArm:[0.0f,0.0f,100.0f],RightLeg:[-8.0f,0.0f,60.0f]},ShowArms:1b}");
		poseMap.put("stargazing", "{Pose:{Body:[-4.0f,10.0f,0.0f],Head:[-22.0f,25.0f,0.0f],LeftArm:[4.0f,18.0f,0.0f],LeftLeg:[6.0f,24.0f,0.0f],RightArm:[-153.0f,34.0f,-3.0f],RightLeg:[-4.0f,17.0f,2.0f]},ShowArms:1b}");
		poseMap.put("block", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,0.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[-15.0f,-45.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("item", "{Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,0.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[-90.0f,0.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},ShowArms:1b}");
		poseMap.put("random", "{Pose:{Body:[0.0f,90.0f,0.0f],Head:[25.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,-50.0f],LeftLeg:[0.0f,0.0f,-50.0f],RightArm:[0.0f,0.0f,50.0f],RightLeg:[0.0f,0.0f,50.0f]},ShowArms:1b}");
		return poseMap;
	}

	public static final List<PoseData> userPoses = new ArrayList<>();

	public static void savePose(String poseName, CompoundTag tag) {
		String tagString = tag.toString();
		userPoses.add(new PoseData(poseName, tagString));
		UserPoseHandler.saveUserPoses();
	}

	public static void removePose(String poseName) {
		userPoses.removeIf(pose -> pose.name().equalsIgnoreCase(poseName));
		UserPoseHandler.saveUserPoses();
	}

	public static final String alignedBlockPose = "{CustomNameVisible:0b,DisabledSlots:0,Invisible:0b,Invulnerable:0b,Move:[0.0d,0.0d,0.0d],NoBasePlate:0b,NoGravity:1b,Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,0.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[-15.0f,135.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},Rotation:[0.0f],ShowArms:1b,Small:0b}";
	public static final String alignedUprightItemPose = "{CustomNameVisible:0b,DisabledSlots:0,Invisible:1b,Invulnerable:0b,Move:[0.0d,0.0d,0.0d],NoBasePlate:0b,NoGravity:1b,Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,0.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[-90.0f,0.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},Rotation:[0.0f],ShowArms:1b,Small:0b}";
	public static final String flatItemPose = "{CustomNameVisible:0b,DisabledSlots:0,Invisible:1b,Invulnerable:0b,Move:[0.0d,0.0d,0.0d],NoBasePlate:0b,NoGravity:1b,Pose:{Body:[0.0f,0.0f,0.0f],Head:[0.0f,0.0f,0.0f],LeftArm:[0.0f,0.0f,0.0f],LeftLeg:[0.0f,0.0f,0.0f],RightArm:[0.0f,0.0f,0.0f],RightLeg:[0.0f,0.0f,0.0f]},Rotation:[0.0f],ShowArms:1b,Small:0b}";
}