package com.mrbysco.armorposer.packets.handler;

import com.mrbysco.armorposer.packets.ArmorStandScreenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
	private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

	public static ClientPayloadHandler getInstance() {
		return INSTANCE;
	}

	public void handleScreenData(final ArmorStandScreenPayload screenMessage, final IPayloadContext context) {
		context.enqueueWork(() -> {
					//Open Captcha Screen
					Minecraft mc = Minecraft.getInstance();
					Entity entity = null;
					if (mc.level != null) {
						entity = mc.level.getEntity(screenMessage.entityID());
					}
					if (entity instanceof ArmorStand armorStandEntity) {
						com.mrbysco.armorposer.client.gui.ArmorStandScreen.openScreen(armorStandEntity);
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.disconnect(Component.translatable("armorposer.networking.screen.failed", e.getMessage()));
					return null;
				});
	}
}
