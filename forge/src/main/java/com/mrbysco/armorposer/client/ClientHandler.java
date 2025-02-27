package com.mrbysco.armorposer.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.ArrayList;
import java.util.List;

public class ClientHandler {
	public static final List<KeyMapping> KEY_MAPPINGS = new ArrayList<>();

	public static void setupKeyMappings(RegisterKeyMappingsEvent event) {
		KEY_MAPPINGS.forEach(event::register);
		KEY_MAPPINGS.clear();
	}
}
