package com.mrbysco.armorposer.platform;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.config.PoserConfig;
import com.mrbysco.armorposer.platform.services.IPlatformHelper;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.decoration.ArmorStand;

public class FabricPlatformHelper implements IPlatformHelper {
	@Override
	public void updateEntity(ArmorStand armorStand, CompoundTag compound) {
		CompoundTag CompoundNBT = armorStand.saveWithoutId(new CompoundTag()).copy();
		CompoundNBT.merge(compound);
		armorStand.load(CompoundNBT);

		FriendlyByteBuf buf = PacketByteBufs.create();
		buf.writeUUID(armorStand.getUUID());
		buf.writeNbt(compound);
		ClientPlayNetworking.send(Reference.SYNC_PACKET_ID, buf);
	}

	@Override
	public boolean allowScrolling() {
		PoserConfig config = AutoConfig.getConfigHolder(PoserConfig.class).getConfig();
		return config.general.allowScrolling;
	}
}
