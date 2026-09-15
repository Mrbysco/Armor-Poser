package com.mrbysco.armorposer;

import com.mrbysco.armorposer.client.GroupHelper;
import com.mrbysco.armorposer.client.debug.DebugHandler;
import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.packets.v1.ArmorStandLockedPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandScreenPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSyncGroupsPayload;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.fml.config.ModConfig;

public class ArmorPoserClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ConfigRegistry.INSTANCE.register("armorposer", ModConfig.Type.CLIENT, PoserConfig.clientSpec);

		ClientPlayNetworking.registerGlobalReceiver(ArmorStandScreenPayload.ID, (payload, context) -> {
			int entityID = payload.entityID();

			Minecraft mc = Minecraft.getInstance();
			Entity entity = null;
			if (mc.level != null) {
				entity = mc.level.getEntity(entityID);
			}
			if (entity instanceof ArmorStand armorStandEntity) {
				com.mrbysco.armorposer.client.gui.ArmorStandScreen.openScreen(armorStandEntity,
						payload.disabledFeatures(), payload.minScale(), payload.maxScale()
				);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(ArmorStandLockedPayload.ID, (payload, context) -> {
			int entityID = payload.entityID();

			Minecraft mc = Minecraft.getInstance();
			Entity entity = null;
			if (mc.level != null) {
				entity = mc.level.getEntity(entityID);
			}
			if (entity instanceof ArmorStand armorStandEntity) {
				armorStandEntity.setPermanentlyInvulnerable(payload.isLocked());
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(ArmorStandSyncGroupsPayload.ID, (payload, context) -> {
			GroupHelper.syncGroups(payload.groupData());
		});
		DebugHandler.init();
	}
}
