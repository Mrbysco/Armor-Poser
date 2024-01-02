package com.mrbysco.armorposer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorPoserClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(Reference.SCREEN_PACKET_ID, (client, handler, buf, responseSender) -> {
			int entityID = buf.readInt();

			Minecraft mc = Minecraft.getInstance();
			Entity entity = null;
			if (mc.level != null) {
				entity = mc.level.getEntity(entityID);
			}
			if (entity instanceof ArmorStand armorStandEntity) {
				client.execute(() -> {
					com.mrbysco.armorposer.client.gui.ArmorStandScreen.openScreen(armorStandEntity);
				});
			}
		});
	}
}
