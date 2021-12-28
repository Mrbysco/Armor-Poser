package com.mrbysco.armorposer;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	public static final String MOD_ID = "armorposer";
	public static final String MOD_NAME = "Armor Poser";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);


	public static ResourceLocation SYNC_PACKET_ID = new ResourceLocation(Reference.MOD_ID, "sync_packet");
	public static ResourceLocation SCREEN_PACKET_ID = new ResourceLocation(Reference.MOD_ID, "screen_packet");
}