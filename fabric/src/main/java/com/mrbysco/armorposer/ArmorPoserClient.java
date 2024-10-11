package com.mrbysco.armorposer;

import com.mrbysco.armorposer.client.gui.MoveableScreen;
import com.mrbysco.armorposer.packets.ArmorStandScreenPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorPoserClient implements ClientModInitializer {
	static {
		MoveableScreen.earlyInit();
	}

	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(ArmorStandScreenPayload.ID, (payload, context) -> {
			int entityID = payload.entityID();

			Minecraft mc = Minecraft.getInstance();
			Entity entity = null;
			if (mc.level != null) {
				entity = mc.level.getEntity(entityID);
			}
			if (entity instanceof ArmorStand armorStandEntity) {
				com.mrbysco.armorposer.client.gui.ArmorStandScreen.openScreen(armorStandEntity);
			}
		});
	}
}
