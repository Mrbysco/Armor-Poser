package com.mrbysco.armorposer.packets.handler;

import com.mrbysco.armorposer.packets.v1.ArmorStandRenamePayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSyncPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandUpdateGroupsPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
	private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

	public static ServerPayloadHandler getInstance() {
		return INSTANCE;
	}

	public void handleSwapData(final ArmorStandSwapPayload swapData, final IPayloadContext context) {
		context.enqueueWork(() -> {
					if (context.player() != null && context.player().level() instanceof ServerLevel serverLevel) {
						Entity entity = serverLevel.getEntity(swapData.data().entityUUID());
						if (entity instanceof ArmorStand armorStandEntity) {
							swapData.data().handleData(armorStandEntity, context.player());
						}
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.disconnect(Component.translatable("armorposer.networking.swap.failed", e.getMessage()));
					return null;
				});
	}

	public void handleSyncData(final ArmorStandSyncPayload syncData, final IPayloadContext context) {
		context.enqueueWork(() -> {
					if (context.player() != null && context.player().level() instanceof ServerLevel serverLevel) {
						Entity entity = serverLevel.getEntity(syncData.data().entityUUID());
						if (entity instanceof ArmorStand armorStandEntity) {
							syncData.data().handleData(armorStandEntity, context.player());
						}
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.disconnect(Component.translatable("armorposer.networking.sync.failed", e.getMessage()));
					return null;
				});
	}

	public void handleRenameData(final ArmorStandRenamePayload renameData, final IPayloadContext context) {
		context.enqueueWork(() -> {
					if (context.player() != null && context.player().level() instanceof ServerLevel serverLevel) {
						Entity entity = serverLevel.getEntity(renameData.data().entityUUID());
						if (entity instanceof ArmorStand armorStandEntity) {
							renameData.data().handleData(armorStandEntity, context.player());
						}
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.disconnect(Component.translatable("armorposer.networking.rename.failed", e.getMessage()));
					return null;
				});
	}

	public void handleUpdateGroupData(final ArmorStandUpdateGroupsPayload updateGroupData, final IPayloadContext context) {
		context.enqueueWork(() -> {
					if (context.player() != null && context.player().level() instanceof ServerLevel serverLevel) {
						Entity entity = serverLevel.getEntity(updateGroupData.data().entityUUID());
						if (entity instanceof ArmorStand armorStandEntity) {
							updateGroupData.data().handleData(armorStandEntity, context.player());
						}
					}
				})
				.exceptionally(e -> {
					// Handle exception
					context.disconnect(Component.translatable("armorposer.networking.update_group.failed", e.getMessage()));
					return null;
				});
	}
}
