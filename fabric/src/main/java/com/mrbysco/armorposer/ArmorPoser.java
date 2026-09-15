package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.data.GroupData;
import com.mrbysco.armorposer.data.RenameData;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.handlers.EventHandler;
import com.mrbysco.armorposer.packets.v1.ArmorStandLockedPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandRenamePayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandScreenPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSyncGroupsPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandSyncPayload;
import com.mrbysco.armorposer.packets.v1.ArmorStandUpdateGroupsPayload;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.fml.config.ModConfig;

public class ArmorPoser implements ModInitializer {

	@Override
	public void onInitialize() {
		ConfigRegistry.INSTANCE.register("armorposer", ModConfig.Type.COMMON, PoserConfig.commonSpec);

		UseItemCallback.EVENT.register((player, world, hand) -> EventHandler.onPlayerRightClickItem(player, hand));

		PayloadTypeRegistry.clientboundPlay().register(ArmorStandScreenPayload.ID, ArmorStandScreenPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ArmorStandLockedPayload.ID, ArmorStandLockedPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ArmorStandSyncGroupsPayload.ID, ArmorStandSyncGroupsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ArmorStandSyncPayload.ID, ArmorStandSyncPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandSyncPayload.ID, (payload, context) -> {
			final ServerLevel serverLevel = context.player().level();

			SyncData syncData = payload.data();
			serverLevel.getServer().execute(() -> {
				Entity entity = serverLevel.getEntity(syncData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					syncData.handleData(armorStandEntity, context.player());
				}
			});
		});

		PayloadTypeRegistry.serverboundPlay().register(ArmorStandSwapPayload.ID, ArmorStandSwapPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandSwapPayload.ID, (payload, context) -> {
			final ServerLevel serverLevel = context.player().level();

			SwapData swapData = payload.data();
			serverLevel.getServer().execute(() -> {
				Entity entity = serverLevel.getEntity(swapData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					swapData.handleData(armorStandEntity, context.player());
				}
			});
		});
		PayloadTypeRegistry.serverboundPlay().register(ArmorStandRenamePayload.ID, ArmorStandRenamePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandRenamePayload.ID, (payload, context) -> {
			final ServerLevel serverLevel = context.player().level();

			RenameData renameData = payload.data();
			serverLevel.getServer().execute(() -> {
				Entity entity = serverLevel.getEntity(renameData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					renameData.handleData(armorStandEntity, context.player());
				}
			});
		});
		PayloadTypeRegistry.serverboundPlay().register(ArmorStandUpdateGroupsPayload.ID, ArmorStandUpdateGroupsPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandUpdateGroupsPayload.ID, (payload, context) -> {
			final ServerLevel serverLevel = context.player().level();

			GroupData groupData = payload.data();
			serverLevel.getServer().execute(() -> {
				Entity entity = serverLevel.getEntity(groupData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					groupData.handleData(armorStandEntity, context.player());
				}
			});
		});

		EntityTrackingEvents.START_TRACKING.register(((entity, player) -> {
			if (ServerPlayNetworking.canSend(player, Reference.LOCKED_PACKET_ID_V1))
				ServerPlayNetworking.send(player, new ArmorStandLockedPayload(entity.getId(), entity.isInvulnerable()));
		}));
	}
}
