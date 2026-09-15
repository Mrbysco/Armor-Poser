package com.mrbysco.armorposer.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class ChannelHelper {
	public static boolean hasChannel(Identifier packetIdentifier) {
		return Minecraft.getInstance().getConnection().hasChannel(packetIdentifier);
	}
}
