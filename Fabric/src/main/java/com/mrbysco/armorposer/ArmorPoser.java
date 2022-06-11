package com.mrbysco.armorposer;

import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.handlers.EventHandler;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.UUID;

public class ArmorPoser implements ModInitializer {

	@Override
	public void onInitialize() {
		AutoConfig.register(PoserConfig.class, Toml4jConfigSerializer::new);

		CommonClass.init();

		UseItemCallback.EVENT.register((player, world, hand) -> EventHandler.onPlayerRightClickItem(player, hand));

		ServerPlayNetworking.registerGlobalReceiver(Reference.SYNC_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			final ServerLevel world = player.getLevel();

			UUID standUUID = buf.readUUID();
			CompoundTag data = buf.readNbt();

			server.execute(() -> {
				Entity entity = world.getEntity(standUUID);
				if (entity instanceof ArmorStand armorStandEntity) {

					CompoundTag entityTag = armorStandEntity.saveWithoutId(new CompoundTag());
					CompoundTag entityTagCopy = entityTag.copy();

					if (!data.isEmpty()) {
						entityTagCopy.merge(data);
						UUID uuid = armorStandEntity.getUUID();
						armorStandEntity.load(entityTagCopy);
						armorStandEntity.setUUID(uuid);
					}
				}
			});
		});
	}
}
