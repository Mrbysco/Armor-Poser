package com.mrbysco.armorposer;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class Reference {
	public static final String MOD_ID = "armorposer";
	public static final String MOD_NAME = "Armor Poser";
	public static final Logger LOGGER = LogUtils.getLogger();


	public static final ResourceLocation SYNC_PACKET_ID = new ResourceLocation(Reference.MOD_ID, "sync_packet");
	public static final ResourceLocation SCREEN_PACKET_ID = new ResourceLocation(Reference.MOD_ID, "screen_packet");
}