package com.mrbysco.armorposer.packets.handler;

import com.mrbysco.armorposer.packets.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class ServerPayloadHandler {
	private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

	public static ServerPayloadHandler getInstance() {
		return INSTANCE;
	}

	public void handleSwapData(final ArmorStandSwapPayload swapData, final PlayPayloadContext context) {
		// Do something with the data, on the main thread
		context.workHandler().submitAsync(() -> {
					if (context.level().isPresent() && context.level().get() instanceof ServerLevel serverLevel) {
						Entity entity = serverLevel.getEntity(swapData.data().entityUUID());
						if (entity instanceof ArmorStand armorStandEntity) {
							swapData.data().handleData(armorStandEntity);
						}
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("captcha.networking.swap.failed", e.getMessage()));
					return null;
				});
	}

	public void handleSyncData(final ArmorStandSyncPayload syncData, final PlayPayloadContext context) {
		// Do something with the data, on the main thread
		context.workHandler().submitAsync(() -> {
					if (context.level().isPresent() && context.level().get() instanceof ServerLevel serverLevel) {
						Entity entity = serverLevel.getEntity(syncData.data().entityUUID());
						if (entity instanceof ArmorStand armorStandEntity) {
							syncData.data().handleData(armorStandEntity);
						}
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.packetHandler().disconnect(Component.translatable("captcha.networking.sync.failed", e.getMessage()));
					return null;
				});
	}
}
