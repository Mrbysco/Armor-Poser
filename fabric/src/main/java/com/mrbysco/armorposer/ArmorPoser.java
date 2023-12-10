package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.data.SwapData;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.handlers.EventHandler;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorPoser implements ModInitializer {
	public static ConfigHolder<PoserConfig> config;

	@Override
	public void onInitialize() {
		config = AutoConfig.register(PoserConfig.class, Toml4jConfigSerializer::new);

		UseItemCallback.EVENT.register((player, world, hand) -> EventHandler.onPlayerRightClickItem(player, hand));

		ServerPlayNetworking.registerGlobalReceiver(Reference.SYNC_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			final ServerLevel serverLevel = player.serverLevel();

			SyncData syncData = SyncData.decode(buf);

			server.execute(() -> {
				Entity entity = serverLevel.getEntity(syncData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					syncData.handleData(armorStandEntity);
				}
			});
		});


		ServerPlayNetworking.registerGlobalReceiver(Reference.SWAP_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			final ServerLevel serverLevel = player.serverLevel();

			SwapData swapData = SwapData.decode(buf);

			server.execute(() -> {
				Entity entity = serverLevel.getEntity(swapData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {
					swapData.handleData(armorStandEntity);
				}
			});
		});
	}
}
