package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.handlers.EventHandler;
import com.mrbysco.armorposer.data.SyncData;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.UUID;

public class ArmorPoser implements ModInitializer {
	public static ConfigHolder<PoserConfig> config;

	@Override
	public void onInitialize() {
		config = AutoConfig.register(PoserConfig.class, Toml4jConfigSerializer::new);

		UseItemCallback.EVENT.register((player, world, hand) -> EventHandler.onPlayerRightClickItem(player, hand));

		ServerPlayNetworking.registerGlobalReceiver(Reference.SYNC_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			final ServerLevel world = player.serverLevel();

			SyncData syncData = SyncData.decode(buf);

			server.execute(() -> {
				Entity entity = world.getEntity(syncData.entityUUID());
				if (entity instanceof ArmorStand armorStandEntity) {

					CompoundTag entityTag = armorStandEntity.saveWithoutId(new CompoundTag());
					CompoundTag entityTagCopy = entityTag.copy();

					CompoundTag tag = syncData.tag();
					if (!tag.isEmpty()) {
						entityTagCopy.merge(tag);
						UUID uuid = armorStandEntity.getUUID();
						armorStandEntity.load(entityTagCopy);
						armorStandEntity.setUUID(uuid);
					}
				}
			});
		});
	}
}
