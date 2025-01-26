package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.data.BookCopyData;
import com.mrbysco.armorposer.data.RenameData;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.handlers.EventHandler;
import com.mrbysco.armorposer.packets.ArmorStandCopyToBookPayload;
import com.mrbysco.armorposer.packets.ArmorStandRenamePayload;
import com.mrbysco.armorposer.packets.ArmorStandScreenPayload;
import com.mrbysco.armorposer.packets.ArmorStandSwapPayload;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorPoser implements ModInitializer {
	public static ConfigHolder<PoserConfig> config;

	@Override
	public void onInitialize() {
		config = AutoConfig.register(PoserConfig.class, Toml4jConfigSerializer::new);
		config.registerLoadListener((holder, config) -> {
			Reference.setAnimationEnabled(config.general.enableAnimation);
			return InteractionResult.PASS;
		});
		config.registerSaveListener((holder, config) -> {
			Reference.setAnimationEnabled(config.general.enableAnimation);
			return InteractionResult.PASS;
		});

		ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
			Reference.setAnimationEnabled(config.get().general.enableAnimation);
		});

		UseItemCallback.EVENT.register((player, world, hand) -> EventHandler.onPlayerRightClickItem(player, hand));

		PayloadTypeRegistry.playS2C().register(ArmorStandScreenPayload.ID, ArmorStandScreenPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ArmorStandSyncPayload.ID, ArmorStandSyncPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandSyncPayload.ID, (payload, context) -> {
			final ServerLevel serverLevel = context.player().serverLevel();

			SyncData syncData = payload.data();
			context.player().server.execute(() -> {
				Entity entity = serverLevel.getEntity(syncData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					syncData.handleData(armorStandEntity, context.player());
				}
			});
		});

		PayloadTypeRegistry.playC2S().register(ArmorStandSwapPayload.ID, ArmorStandSwapPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandSwapPayload.ID, (payload, context) -> {
			final ServerLevel serverLevel = context.player().serverLevel();

			SwapData swapData = payload.data();
			context.player().server.execute(() -> {
				Entity entity = serverLevel.getEntity(swapData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					swapData.handleData(armorStandEntity);
				}
			});
		});
		PayloadTypeRegistry.playC2S().register(ArmorStandRenamePayload.ID, ArmorStandRenamePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandRenamePayload.ID, (payload, context) -> {
			final ServerLevel serverLevel = context.player().serverLevel();

			RenameData renameData = payload.data();
			context.player().server.execute(() -> {
				Entity entity = serverLevel.getEntity(renameData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					renameData.handleData(armorStandEntity, context.player());
				}
			});
		});
		PayloadTypeRegistry.playC2S().register(ArmorStandCopyToBookPayload.ID, ArmorStandCopyToBookPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ArmorStandCopyToBookPayload.ID, (payload, context) -> {
			BookCopyData bookData = payload.data();
			context.player().server.execute(() -> {
				bookData.handleData(context.player());
			});
		});
	}
}
