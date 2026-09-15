package com.mrbysco.armorposer.packets.handler;

import com.mrbysco.armorposer.client.GroupHelper;
import com.mrbysco.armorposer.packets.v1.ArmorStandLockedPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandScreenPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSyncGroupsPayload;
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

	public void handleScreenData(final ArmorStandScreenPayload payload, final IPayloadContext context) {
		context.enqueueWork(() -> {
					//Open Armor Poser Screen
					Minecraft mc = Minecraft.getInstance();
					Entity entity = null;
					if (mc.level != null) {
						entity = mc.level.getEntity(payload.entityID());
					}
					if (entity instanceof ArmorStand armorStandEntity) {
						com.mrbysco.armorposer.client.gui.ArmorStandScreen.openScreen(armorStandEntity,
								payload.disabledFeatures(), payload.minScale(), payload.maxScale()
						);
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.disconnect(Component.translatable("armorposer.networking.screen.failed", e.getMessage()));
					return null;
				});
	}

	public void handleLockedData(ArmorStandLockedPayload payload, IPayloadContext context) {
		context.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = null;
			if (mc.level != null) {
				entity = mc.level.getEntity(payload.entityID());
			}
			if (entity instanceof ArmorStand armorStandEntity) {
				armorStandEntity.setPermanentlyInvulnerable(payload.isLocked());
			}
		});
	}

	public void handleSyncGroupData(ArmorStandSyncGroupsPayload armorStandSyncGroupsPayload, IPayloadContext context) {
		context.enqueueWork(() -> {
			GroupHelper.syncGroups(armorStandSyncGroupsPayload.groupData());
		});
	}
}
