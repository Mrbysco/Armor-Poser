package com.mrbysco.armorposer.client;

import com.mrbysco.armorposer.platform.Services;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
	public static final KeyMapping DEFER_CONTROL_KEY = Services.PLATFORM.registerKeyMapping(
			new KeyMapping("armorposer.keybind.deferControl", GLFW.GLFW_KEY_LEFT_ALT, "armorposer.keybinds"));

	public static void loadClass() {
	}

}
